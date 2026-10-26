import ctypes
import io
import os
import threading
import unittest
from unittest.mock import MagicMock, call, patch

if os.name == "nt":
    import ssms_daemon as daemon


@unittest.skipUnless(os.name == "nt", "The SSMS assistant uses Windows APIs")
class TestSsmsLifecycle(unittest.TestCase):
    def setUp(self):
        self.win32 = MagicMock()
        self.win32.GetAsyncKeyState.return_value = 0
        self.win32.GetMessageW.return_value = 0
        self.kernel32 = MagicMock()
        self.kernel32.GetCurrentThreadId.return_value = 101
        self.kernel32.CreateMutexW.return_value = 701
        self.kernel32.GetLastError.return_value = 0
        patches = {
            "user32": self.win32,
            "kernel32": self.kernel32,
            "STOP_REQUESTED": threading.Event(),
            "KEYBOARD_LOCKED": False,
            "ABORT_TYPING": False,
            "FAST_MODE": False,
            "_STATE_LOCK": threading.Lock(),
            "_OPERATION_LOCK": threading.Lock(),
            "_INPUT_LOCK": threading.RLock(),
            "_MAIN_THREAD_ID": 101,
            "_HOOK_THREAD_ID": 202,
            "_HOOK_THREAD": None,
            "_HOOK_PROC_REF": None,
            "_HOOK_READY": threading.Event(),
            "_PHYSICAL_KEYS": set(),
            "_INJECTED_KEYS": set(),
            "get_active_window_title": MagicMock(return_value="SQLQuery1.sql - SERVER.ExistingPayroll (user (51)) - Microsoft SQL Server Management Studio"),
            "get_clipboard_text": MagicMock(return_value="old clipboard"),
            "set_clipboard_text": MagicMock(return_value=True),
            "_copy_current_line_text": MagicMock(return_value="-- xem dữ liệu có sẵn"),
            "_wait_modifiers_released": MagicMock(),
            "_tap_key": MagicMock(),
            "_key_down": MagicMock(),
            "_key_up": MagicMock(),
            "_send_unicode_char": MagicMock(),
            "set_database_context": MagicMock(),
            "generate_sql": MagicMock(return_value="select MaNV from dbo.NHANVIEN;"),
            "type_sql_humanlike": MagicMock(),
        }
        for name, value in patches.items():
            test_patch = patch.object(daemon, name, value)
            test_patch.start()
            self.addCleanup(test_patch.stop)
        self.sleep_patch = patch.object(daemon.time, "sleep")
        self.sleep_patch.start()
        self.addCleanup(self.sleep_patch.stop)
        error_patch = patch.object(daemon.ctypes, "get_last_error", return_value=0)
        error_patch.start()
        self.addCleanup(error_patch.stop)

    def test_shutdown_unlocks_keyboard_and_cancels_typing_when_idle_or_busy(self):
        for locked in (False, True):
            with self.subTest(locked=locked):
                daemon.STOP_REQUESTED.clear()
                daemon.ABORT_TYPING = False
                daemon.KEYBOARD_LOCKED = locked
                daemon.request_shutdown()
                self.assertTrue(daemon.STOP_REQUESTED.is_set())
                self.assertTrue(daemon.ABORT_TYPING)
                self.assertFalse(daemon.KEYBOARD_LOCKED)
                self.win32.SendInput.assert_not_called()
                self.win32.PostThreadMessageW.assert_has_calls([
                    call(101, daemon.WM_QUIT, 0, 0),
                    call(202, daemon.WM_QUIT, 0, 0),
                ], any_order=True)

    def test_stopped_assistant_does_not_process_more_generation_requests(self):
        daemon.request_shutdown()
        daemon.handle_hotkey_trigger(daemon.HOTKEY_CTRL_Q)
        daemon.generate_sql.assert_not_called()
        daemon.type_sql_humanlike.assert_not_called()
        daemon._tap_key.assert_not_called()

    def test_shutdown_during_blocked_generation_unlocks_now_and_prevents_later_typing(self):
        generation_entered = threading.Event()
        release_generation = threading.Event()

        def blocked_generation(*args, **kwargs):
            generation_entered.set()
            release_generation.wait(timeout=2)
            return "select MaNV from dbo.NHANVIEN;"

        daemon.generate_sql.side_effect = blocked_generation
        action = threading.Thread(target=daemon.handle_hotkey_trigger, args=(daemon.HOTKEY_CTRL_Q,), daemon=True)
        action.start()
        try:
            self.assertTrue(generation_entered.wait(timeout=1), "Generation worker did not start")
            daemon.request_shutdown()
            self.assertFalse(daemon.KEYBOARD_LOCKED)
            self.assertTrue(daemon.ABORT_TYPING)
            self.assertTrue(daemon.STOP_REQUESTED.is_set())
        finally:
            release_generation.set()
            action.join(timeout=1)
        self.assertFalse(action.is_alive())
        daemon.type_sql_humanlike.assert_not_called()
        self.win32.SendInput.assert_not_called()

    def test_quit_key_combination_is_available_while_keyboard_is_locked(self):
        for control, alt in (
            (daemon.VK_CONTROL, daemon.VK_MENU),
            (daemon.VK_LCONTROL, daemon.VK_LMENU),
            (daemon.VK_RCONTROL, daemon.VK_RMENU),
            (daemon.VK_LCONTROL, daemon.VK_RMENU),
            (daemon.VK_RCONTROL, daemon.VK_LMENU),
        ):
            with self.subTest(control=control, alt=alt):
                daemon.STOP_REQUESTED.clear()
                daemon._PHYSICAL_KEYS.clear()
                daemon.KEYBOARD_LOCKED = True
                daemon.ABORT_TYPING = False
                for key in (control, alt, daemon.VK_Q):
                    keyboard_event = daemon.KBDLLHOOKSTRUCT(vkCode=key, flags=0)
                    message = daemon.WM_KEYDOWN if key == control else daemon.WM_SYSKEYDOWN
                    daemon._low_level_keyboard_proc(0, message, ctypes.addressof(keyboard_event))
                self.assertTrue(daemon.STOP_REQUESTED.is_set())
                self.assertFalse(daemon.KEYBOARD_LOCKED)
                self.assertTrue(daemon.ABORT_TYPING)
        self.assertEqual(0, self.win32.GetAsyncKeyState.return_value)

    def test_injected_q_does_not_trigger_shutdown(self):
        daemon.KEYBOARD_LOCKED = True
        daemon._PHYSICAL_KEYS.update((daemon.VK_LCONTROL, daemon.VK_RMENU))
        keyboard_event = daemon.KBDLLHOOKSTRUCT(vkCode=daemon.VK_Q, flags=daemon.LLKHF_INJECTED)
        daemon._low_level_keyboard_proc(0, daemon.WM_KEYDOWN, ctypes.addressof(keyboard_event))
        self.assertFalse(daemon.STOP_REQUESTED.is_set())
        self.assertNotIn(daemon.VK_Q, daemon._PHYSICAL_KEYS)
        self.win32.CallNextHookEx.assert_called_once()

    def test_injected_control_does_not_turn_alt_q_into_quit(self):
        daemon.KEYBOARD_LOCKED = True
        daemon._PHYSICAL_KEYS.add(daemon.VK_LMENU)
        daemon._INJECTED_KEYS.add(daemon.VK_CONTROL)
        self.win32.GetAsyncKeyState.side_effect = lambda key: 0x8000 if key == daemon.VK_CONTROL else 0
        keyboard_event = daemon.KBDLLHOOKSTRUCT(vkCode=daemon.VK_Q, flags=0)
        daemon._low_level_keyboard_proc(0, daemon.WM_KEYDOWN, ctypes.addressof(keyboard_event))
        self.assertFalse(daemon.STOP_REQUESTED.is_set())

    def test_released_modifiers_do_not_leave_a_stale_quit_combination(self):
        daemon.KEYBOARD_LOCKED = True
        for key in (daemon.VK_LCONTROL, daemon.VK_RMENU):
            keyboard_event = daemon.KBDLLHOOKSTRUCT(vkCode=key, flags=0)
            daemon._low_level_keyboard_proc(0, daemon.WM_KEYDOWN, ctypes.addressof(keyboard_event))
            daemon._low_level_keyboard_proc(0, daemon.WM_KEYUP, ctypes.addressof(keyboard_event))
        keyboard_event = daemon.KBDLLHOOKSTRUCT(vkCode=daemon.VK_Q, flags=0)
        daemon._low_level_keyboard_proc(0, daemon.WM_KEYDOWN, ctypes.addressof(keyboard_event))
        self.assertFalse(daemon.STOP_REQUESTED.is_set())

    def test_background_tasks_are_single_flight_and_stopped_tasks_are_rejected(self):
        generation_entered = threading.Event()
        release_generation = threading.Event()

        def blocked_generation(*args, **kwargs):
            generation_entered.set()
            release_generation.wait(timeout=2)
            return "select MaNV from dbo.NHANVIEN;"

        daemon.generate_sql.side_effect = blocked_generation
        try:
            self.assertTrue(daemon.start_hotkey_task(daemon.HOTKEY_CTRL_Q))
            self.assertTrue(generation_entered.wait(timeout=1))
            self.assertFalse(daemon.start_hotkey_task(daemon.HOTKEY_F8))
            daemon.request_shutdown()
            self.assertFalse(daemon.start_hotkey_task(daemon.HOTKEY_ALT_Q))
        finally:
            release_generation.set()
            acquired = daemon._OPERATION_LOCK.acquire(timeout=1)
            if acquired:
                daemon._OPERATION_LOCK.release()
        self.assertTrue(acquired, "Background action did not release its lock")
        daemon.generate_sql.assert_called_once()
        daemon.type_sql_humanlike.assert_not_called()

    def test_main_message_pump_accepts_quit_while_generation_is_blocked(self):
        generation_entered = threading.Event()
        generation_exited = threading.Event()
        release_generation = threading.Event()

        def blocked_generation(*args, **kwargs):
            generation_entered.set()
            release_generation.wait(timeout=3)
            generation_exited.set()
            return "select MaNV from dbo.NHANVIEN;"

        message_count = 0

        def next_message(message_pointer, *args):
            nonlocal message_count
            message_count += 1
            if message_count > 2:
                return 0
            if message_count == 2:
                self.assertTrue(generation_entered.wait(timeout=1))
                self.assertFalse(generation_exited.is_set(), "Message loop waited for generation to finish")
            message = ctypes.cast(message_pointer, ctypes.POINTER(daemon.wintypes.MSG)).contents
            message.message = daemon.WM_HOTKEY
            message.wParam = daemon.HOTKEY_CTRL_Q if message_count == 1 else daemon.HOTKEY_QUIT
            return 1

        daemon.generate_sql.side_effect = blocked_generation
        self.win32.GetMessageW.side_effect = next_message
        self.win32.RegisterHotKey.return_value = True
        try:
            with patch.object(daemon, "switch_to_default_desktop", return_value=True), \
                    patch.object(daemon, "start_keyboard_shield_thread") as start_hook, \
                    patch.object(daemon, "stop_keyboard_shield_thread") as stop_hook:
                self.assertEqual(0, daemon.run_daemon())
                start_hook.assert_called_once()
                stop_hook.assert_called_once()
            self.assertTrue(daemon.STOP_REQUESTED.is_set())
            self.assertFalse(daemon.KEYBOARD_LOCKED)
            daemon.type_sql_humanlike.assert_not_called()
            self.win32.RegisterHotKey.assert_any_call(
                None, daemon.HOTKEY_QUIT,
                daemon.MOD_CONTROL | daemon.MOD_ALT | daemon.MOD_NOREPEAT, daemon.VK_Q,
            )
            registered_ids = {invocation.args[1] for invocation in self.win32.RegisterHotKey.call_args_list}
            unregistered_ids = {invocation.args[1] for invocation in self.win32.UnregisterHotKey.call_args_list}
            self.assertEqual(registered_ids, unregistered_ids)
            self.kernel32.CloseHandle.assert_any_call(701)
        finally:
            release_generation.set()
            acquired = daemon._OPERATION_LOCK.acquire(timeout=1)
            if acquired:
                daemon._OPERATION_LOCK.release()
        self.assertTrue(acquired, "Background action did not finish during cleanup")
        daemon.type_sql_humanlike.assert_not_called()

    def test_failed_quit_hotkey_registration_does_not_leave_a_hidden_daemon(self):
        self.win32.RegisterHotKey.side_effect = lambda hwnd, hotkey, modifiers, key: hotkey != daemon.HOTKEY_QUIT
        with patch.object(daemon, "switch_to_default_desktop", return_value=True), \
                patch.object(daemon, "start_keyboard_shield_thread"), \
                patch.object(daemon, "stop_keyboard_shield_thread"):
            self.assertNotEqual(0, daemon.run_daemon())
        self.win32.GetMessageW.assert_not_called()
        self.assertTrue(daemon.STOP_REQUESTED.is_set())
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        successful_ids = {
            invocation.args[1] for invocation in self.win32.RegisterHotKey.call_args_list
            if invocation.args[1] != daemon.HOTKEY_QUIT
        }
        unregistered_ids = {invocation.args[1] for invocation in self.win32.UnregisterHotKey.call_args_list}
        self.assertEqual(successful_ids, unregistered_ids)
        self.kernel32.CloseHandle.assert_any_call(701)

    def test_partial_hotkey_registration_only_unregisters_successful_keys(self):
        self.win32.RegisterHotKey.side_effect = lambda hwnd, hotkey, modifiers, key: hotkey != daemon.HOTKEY_F8
        with patch.object(daemon, "switch_to_default_desktop", return_value=True), \
                patch.object(daemon, "start_keyboard_shield_thread"), \
                patch.object(daemon, "stop_keyboard_shield_thread"):
            self.assertEqual(0, daemon.run_daemon())
        unregistered_ids = {invocation.args[1] for invocation in self.win32.UnregisterHotKey.call_args_list}
        self.assertEqual({daemon.HOTKEY_ALT_Q, daemon.HOTKEY_CTRL_Q, daemon.HOTKEY_QUIT}, unregistered_ids)
        self.assertTrue(daemon.STOP_REQUESTED.is_set())
        self.kernel32.CloseHandle.assert_any_call(701)

    def test_message_loop_error_still_cleans_up_registered_hotkeys_and_hook(self):
        self.win32.RegisterHotKey.return_value = True
        self.win32.GetMessageW.return_value = -1
        with patch.object(daemon, "switch_to_default_desktop", return_value=True), \
                patch.object(daemon, "start_keyboard_shield_thread"), \
                patch.object(daemon, "stop_keyboard_shield_thread") as stop_hook, \
                patch.object(daemon.ctypes, "WinError", return_value=OSError("Mock message loop failure")):
            with self.assertRaisesRegex(OSError, "Mock message loop failure"):
                daemon.run_daemon()
        stop_hook.assert_called_once()
        self.assertTrue(daemon.STOP_REQUESTED.is_set())
        registered_ids = {invocation.args[1] for invocation in self.win32.RegisterHotKey.call_args_list}
        unregistered_ids = {invocation.args[1] for invocation in self.win32.UnregisterHotKey.call_args_list}
        self.assertEqual(registered_ids, unregistered_ids)
        self.kernel32.CloseHandle.assert_any_call(701)

    def test_duplicate_start_does_not_stop_the_existing_instance(self):
        daemon.KEYBOARD_LOCKED = True
        with patch.object(daemon.ctypes, "get_last_error", return_value=183), \
                patch.object(daemon, "start_keyboard_shield_thread") as start_hook, \
                patch.object(daemon, "stop_keyboard_shield_thread") as stop_hook:
            self.assertEqual(0, daemon.run_daemon())
        self.assertFalse(daemon.STOP_REQUESTED.is_set())
        self.assertFalse(daemon.ABORT_TYPING)
        self.assertTrue(daemon.KEYBOARD_LOCKED)
        self.win32.RegisterHotKey.assert_not_called()
        self.win32.UnregisterHotKey.assert_not_called()
        self.win32.PostThreadMessageW.assert_not_called()
        start_hook.assert_not_called()
        stop_hook.assert_not_called()
        self.kernel32.CloseHandle.assert_called_once_with(701)

    def test_hook_thread_unhooks_after_its_message_loop_exits(self):
        self.win32.SetWindowsHookExW.return_value = 501
        self.win32.GetMessageW.return_value = 0
        with patch.object(daemon, "switch_to_default_desktop", return_value=True):
            daemon.start_keyboard_shield_thread()
            hook_thread = daemon._HOOK_THREAD
            self.assertIsNotNone(hook_thread)
            hook_thread.join(timeout=1)
        self.assertFalse(hook_thread.is_alive())
        self.win32.UnhookWindowsHookEx.assert_called_once_with(501)

    def test_stopping_hook_posts_quit_and_waits_for_cleanup(self):
        hook_thread = MagicMock()
        daemon._HOOK_THREAD = hook_thread
        daemon.stop_keyboard_shield_thread()
        self.win32.PostThreadMessageW.assert_any_call(202, daemon.WM_QUIT, 0, 0)
        hook_thread.join.assert_called_once_with(timeout=0.5)

    def test_hook_startup_failure_or_cancellation_clears_native_thread_id(self):
        for cancelled in (False, True):
            with self.subTest(cancelled=cancelled):
                daemon.STOP_REQUESTED.clear()
                if cancelled:
                    daemon.STOP_REQUESTED.set()
                self.win32.SetWindowsHookExW.return_value = 0
                with patch.object(daemon, "switch_to_default_desktop", return_value=True):
                    daemon.start_keyboard_shield_thread()
                    daemon._HOOK_THREAD.join(timeout=1)
                self.assertFalse(daemon._HOOK_THREAD.is_alive())
                self.assertEqual(0, daemon._HOOK_THREAD_ID)
                self.assertTrue(daemon._HOOK_READY.is_set())

    def test_check_mode_does_not_start_hooks_hotkeys_or_a_daemon(self):
        with patch.object(daemon, "run_daemon") as run_daemon, \
                patch.object(daemon, "start_keyboard_shield_thread") as start_hook, \
                patch.object(daemon.Path, "open", return_value=io.StringIO()), \
                patch.object(daemon.sys, "stdout", io.StringIO()), \
                patch.object(daemon.sys, "stderr", io.StringIO()):
            self.assertEqual(0, daemon.main(["--hidden", "--check"]))
        run_daemon.assert_not_called()
        start_hook.assert_not_called()
        self.win32.RegisterHotKey.assert_not_called()
        self.win32.SetWindowsHookExW.assert_not_called()
        self.kernel32.CreateMutexW.assert_not_called()
        daemon.generate_sql.assert_not_called()


if __name__ == "__main__":
    unittest.main()
