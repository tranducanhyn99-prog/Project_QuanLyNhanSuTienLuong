import ctypes
import os
import threading
import unittest
from unittest.mock import MagicMock, call, patch

if os.name == "nt":
    import ssms_daemon as daemon


@unittest.skipUnless(os.name == "nt", "The SSMS assistant uses Windows APIs")
class TestSsmsPause(unittest.TestCase):
    QUERY_TITLE = (
        "SQLQuery1.sql - SERVER.ExistingPayroll (user (51)) "
        "- Microsoft SQL Server Management Studio"
    )

    def setUp(self):
        self.win32 = MagicMock()
        self.win32.GetAsyncKeyState.return_value = 0
        self.win32.CallNextHookEx.return_value = 77
        self.win32.GetMessageW.return_value = 0
        self.kernel32 = MagicMock()
        self.kernel32.GetCurrentThreadId.return_value = 101
        self.kernel32.CreateMutexW.return_value = 701
        patches = {
            "user32": self.win32,
            "kernel32": self.kernel32,
            "STOP_REQUESTED": threading.Event(),
            "PAUSED": threading.Event(),
            "KEYBOARD_LOCKED": False,
            "ABORT_TYPING": False,
            "FAST_MODE": False,
            "_STATE_LOCK": threading.Lock(),
            "_OPERATION_LOCK": threading.Lock(),
            "_INPUT_LOCK": threading.RLock(),
            "_MAIN_THREAD_ID": 101,
            "_HOOK_THREAD_ID": 202,
            "_SHIELD_REQUESTED": False,
            "_RESUME_PENDING": False,
            "_ACTIVE_QUERY_TITLE": "",
            "_CAPTURING_PROMPT": False,
            "_PHYSICAL_KEYS": set(),
            "_INJECTED_KEYS": set(),
            "get_active_window_title": MagicMock(return_value=self.QUERY_TITLE),
            "get_clipboard_text": MagicMock(return_value="old clipboard"),
            "set_clipboard_text": MagicMock(return_value=True),
            "_copy_current_line_text": MagicMock(return_value="-- xem dữ liệu có sẵn"),
            "_wait_modifiers_released": MagicMock(),
            "set_database_context": MagicMock(),
            "generate_sql": MagicMock(return_value="select MaNV from dbo.NHANVIEN;"),
        }
        for name, value in patches.items():
            active_patch = patch.object(daemon, name, value)
            active_patch.start()
            self.addCleanup(active_patch.stop)
        error_patch = patch.object(daemon.ctypes, "get_last_error", return_value=0)
        error_patch.start()
        self.addCleanup(error_patch.stop)

    def start_worker(self, operation):
        entered = threading.Event()
        finished = threading.Event()
        errors = []

        def run():
            entered.set()
            try:
                operation()
            except BaseException as error:
                errors.append(error)
            finally:
                finished.set()

        worker = threading.Thread(target=run, daemon=True)
        worker.start()
        self.assertTrue(entered.wait(timeout=1))

        def cleanup():
            daemon.request_shutdown()
            worker.join(timeout=1)
            self.assertFalse(worker.is_alive(), "Paused input worker did not stop")
            self.assertEqual([], errors)

        self.addCleanup(cleanup)
        return finished

    def key_event(self, key, message=None, flags=0):
        keyboard_event = daemon.KBDLLHOOKSTRUCT(vkCode=key, flags=flags)
        return daemon._low_level_keyboard_proc(
            0, daemon.WM_KEYDOWN if message is None else message,
            ctypes.addressof(keyboard_event),
        )

    def activate_shield(self):
        daemon._SHIELD_REQUESTED = True
        daemon.KEYBOARD_LOCKED = True
        daemon._ACTIVE_QUERY_TITLE = self.QUERY_TITLE

    def test_paused_keyboard_allows_typing_backspace_and_delete(self):
        self.activate_shield()
        self.assertTrue(daemon.toggle_pause())
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        for key in (0x41, daemon.VK_BACK, 0x2E):
            with self.subTest(key=key):
                self.assertEqual(77, self.key_event(key))
                self.assertEqual(77, self.key_event(key, daemon.WM_KEYUP))
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.assertFalse(daemon.ABORT_TYPING)
        self.win32.SendInput.assert_not_called()

    def test_idle_pause_rejects_generation_without_touching_editor(self):
        daemon.toggle_pause()
        with patch.object(daemon, "_tap_key") as tap_key, \
                patch.object(daemon, "type_sql_humanlike") as type_sql:
            self.assertFalse(daemon.start_hotkey_task(daemon.HOTKEY_F8))
            daemon.handle_hotkey_trigger(daemon.HOTKEY_CTRL_Q)
        tap_key.assert_not_called()
        type_sql.assert_not_called()
        daemon.generate_sql.assert_not_called()
        daemon._copy_current_line_text.assert_not_called()
        daemon.set_clipboard_text.assert_not_called()
        self.assertFalse(daemon.KEYBOARD_LOCKED)

    def test_pause_shortcut_posts_once_per_physical_press_even_when_locked(self):
        self.activate_shield()
        for key in (daemon.VK_LCONTROL, daemon.VK_RMENU):
            self.key_event(key)
        self.assertEqual(1, self.key_event(daemon.VK_P))
        self.assertEqual(1, self.key_event(daemon.VK_P))
        pause_message = call(101, daemon.WM_HOTKEY, daemon.HOTKEY_PAUSE, 0)
        self.assertEqual([pause_message], self.win32.PostThreadMessageW.call_args_list)
        self.assertTrue(daemon.toggle_pause())
        self.key_event(daemon.VK_P, daemon.WM_KEYUP)
        self.assertEqual(1, self.key_event(daemon.VK_P))
        self.assertEqual([pause_message, pause_message], self.win32.PostThreadMessageW.call_args_list)
        self.assertFalse(daemon.toggle_pause())

    def test_injected_p_and_injected_control_do_not_activate_pause(self):
        daemon._PHYSICAL_KEYS.update((daemon.VK_LCONTROL, daemon.VK_LMENU))
        self.key_event(daemon.VK_P, flags=daemon.LLKHF_INJECTED)
        self.win32.PostThreadMessageW.assert_not_called()
        self.assertFalse(daemon.PAUSED.is_set())

        daemon._PHYSICAL_KEYS.clear()
        daemon._PHYSICAL_KEYS.add(daemon.VK_LMENU)
        daemon._INJECTED_KEYS.add(daemon.VK_CONTROL)
        self.win32.GetAsyncKeyState.side_effect = lambda key: 0x8000 if key == daemon.VK_CONTROL else 0
        self.key_event(daemon.VK_P)
        self.win32.PostThreadMessageW.assert_not_called()
        self.assertFalse(daemon.PAUSED.is_set())

    def test_pending_character_waits_for_resume_and_is_sent_exactly_once(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        self.assertFalse(finished.wait(timeout=0.08))
        self.win32.SendInput.assert_not_called()
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        daemon.toggle_pause()
        self.assertTrue(finished.wait(timeout=1))
        self.win32.SendInput.assert_called_once()
        sent_inputs = self.win32.SendInput.call_args.args[1]
        self.assertEqual(ord("x"), sent_inputs[0].union.ki.wScan)
        self.assertTrue(daemon.KEYBOARD_LOCKED)

    def test_pending_newline_waits_for_resume(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._tap_key(daemon.VK_RETURN))
        self.assertFalse(finished.wait(timeout=0.08))
        self.win32.SendInput.assert_not_called()
        self.win32.keybd_event.assert_not_called()
        daemon.toggle_pause()
        self.assertTrue(finished.wait(timeout=1))
        self.assertTrue(self.win32.SendInput.called or self.win32.keybd_event.called)

    def test_shift_tab_is_sent_as_one_complete_chord_after_resume(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._send_key_chord(daemon.VK_SHIFT, daemon.VK_TAB))
        self.assertFalse(finished.wait(timeout=0.08))
        self.win32.SendInput.assert_not_called()
        self.win32.keybd_event.assert_not_called()
        daemon.toggle_pause()
        self.assertTrue(finished.wait(timeout=1))
        self.win32.SendInput.assert_called_once()
        count, batch, size = self.win32.SendInput.call_args.args
        self.assertEqual(4, count)
        self.assertEqual([
            (daemon.VK_SHIFT, 0), (daemon.VK_TAB, 0),
            (daemon.VK_TAB, daemon.KEYEVENTF_KEYUP),
            (daemon.VK_SHIFT, daemon.KEYEVENTF_KEYUP),
        ], [(batch[index].union.ki.wVk, batch[index].union.ki.dwFlags) for index in range(count)])
        self.assertFalse(daemon._INJECTED_KEYS)

    def test_pause_during_prompt_capture_cancels_instead_of_reusing_a_selection(self):
        self.activate_shield()
        daemon._CAPTURING_PROMPT = True
        daemon.toggle_pause()
        self.assertTrue(daemon.ABORT_TYPING)
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.assertEqual(77, self.key_event(daemon.VK_BACK))
        daemon.toggle_pause()
        daemon._send_unicode_char("x")
        self.win32.SendInput.assert_not_called()

    def test_pause_mid_typing_resumes_remaining_characters_without_loss(self):
        self.activate_shield()
        daemon.FAST_MODE = True
        prefix_ready = threading.Event()
        release_prefix = threading.Event()
        sent_characters = []
        send_character = daemon._send_unicode_char

        def record_send(count, inputs, size):
            sent_characters.append(chr(inputs[0].union.ki.wScan))
            return count

        def stop_after_prefix(character):
            send_character(character)
            if len(sent_characters) == 2:
                prefix_ready.set()
                release_prefix.wait(timeout=2)

        self.win32.SendInput.side_effect = record_send
        with patch.object(daemon, "_send_unicode_char", side_effect=stop_after_prefix):
            finished = self.start_worker(lambda: daemon.type_sql_humanlike("abcd;"))
            try:
                self.assertTrue(prefix_ready.wait(timeout=1))
                daemon.toggle_pause()
                release_prefix.set()
                self.assertFalse(finished.wait(timeout=0.08))
                self.assertEqual("ab", "".join(sent_characters))
                self.assertEqual(77, self.key_event(daemon.VK_BACK))
                daemon.toggle_pause()
                self.assertTrue(finished.wait(timeout=1))
                self.assertEqual("abcd;", "".join(sent_characters))
            finally:
                release_prefix.set()

    def test_api_result_arriving_during_pause_waits_until_resume(self):
        generation_entered = threading.Event()
        release_generation = threading.Event()
        generation_finished = threading.Event()
        typing_started = threading.Event()

        def generate(*args, **kwargs):
            generation_entered.set()
            release_generation.wait(timeout=2)
            generation_finished.set()
            return "select MaNV from dbo.NHANVIEN;"

        daemon.generate_sql.side_effect = generate
        with patch.object(daemon, "_tap_key"), \
                patch.object(daemon, "type_sql_humanlike", side_effect=lambda sql: typing_started.set()) as type_sql:
            finished = self.start_worker(lambda: daemon.handle_hotkey_trigger(daemon.HOTKEY_CTRL_Q))
            try:
                self.assertTrue(generation_entered.wait(timeout=1))
                daemon.toggle_pause()
                self.assertFalse(daemon.KEYBOARD_LOCKED)
                release_generation.set()
                self.assertTrue(generation_finished.wait(timeout=1))
                self.assertFalse(typing_started.wait(timeout=0.08))
                self.assertFalse(finished.is_set())
                daemon.toggle_pause()
                self.assertTrue(typing_started.wait(timeout=1))
                self.assertTrue(finished.wait(timeout=1))
                type_sql.assert_called_once_with("select MaNV from dbo.NHANVIEN;")
            finally:
                release_generation.set()

    def test_resume_waits_for_physical_shortcut_modifiers_to_be_released(self):
        self.activate_shield()
        daemon.toggle_pause()
        for key in (daemon.VK_LCONTROL, daemon.VK_RMENU, daemon.VK_P):
            daemon._PHYSICAL_KEYS.add(key)
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        daemon.toggle_pause()
        self.assertFalse(finished.wait(timeout=0.08))
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.win32.SendInput.assert_not_called()
        for key in (daemon.VK_P, daemon.VK_RMENU, daemon.VK_LCONTROL):
            self.key_event(key, daemon.WM_KEYUP)
        self.assertTrue(finished.wait(timeout=1))
        self.win32.SendInput.assert_called_once()

    def test_esc_while_paused_cancels_the_pending_character(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        self.assertFalse(finished.wait(timeout=0.08))
        self.key_event(daemon.VK_ESCAPE)
        self.assertTrue(daemon.ABORT_TYPING)
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.assertTrue(finished.wait(timeout=1))
        self.win32.SendInput.assert_not_called()
        daemon.toggle_pause()
        self.win32.SendInput.assert_not_called()

    def test_shutdown_while_paused_wakes_pending_input_without_typing(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        self.assertFalse(finished.wait(timeout=0.08))
        daemon.request_shutdown()
        self.assertTrue(finished.wait(timeout=1))
        self.assertTrue(daemon.STOP_REQUESTED.is_set())
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.win32.SendInput.assert_not_called()

    def test_esc_cancels_while_resume_waits_for_shortcut_release(self):
        self.activate_shield()
        daemon.toggle_pause()
        daemon._PHYSICAL_KEYS.update((daemon.VK_LCONTROL, daemon.VK_LMENU, daemon.VK_P))
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        daemon.toggle_pause()
        self.assertFalse(finished.wait(timeout=0.08))
        self.key_event(daemon.VK_ESCAPE)
        self.assertTrue(daemon.ABORT_TYPING)
        self.assertTrue(finished.wait(timeout=1))
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.win32.SendInput.assert_not_called()

    def test_resume_in_another_query_cancels_instead_of_typing_there(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        daemon.get_active_window_title.return_value = self.QUERY_TITLE.replace("SQLQuery1", "SQLQuery2")
        daemon.toggle_pause()
        self.assertTrue(finished.wait(timeout=1))
        self.assertTrue(daemon.ABORT_TYPING)
        self.assertFalse(daemon.KEYBOARD_LOCKED)
        self.win32.SendInput.assert_not_called()

    def test_modified_marker_does_not_prevent_resuming_the_same_query(self):
        self.activate_shield()
        daemon.toggle_pause()
        finished = self.start_worker(lambda: daemon._send_unicode_char("x"))
        daemon.get_active_window_title.return_value = self.QUERY_TITLE.replace(") -", ")* -")
        daemon.toggle_pause()
        self.assertTrue(finished.wait(timeout=1))
        self.assertFalse(daemon.ABORT_TYPING)
        self.win32.SendInput.assert_called_once()

    def test_main_loop_registers_and_dispatches_pause_without_stopping_daemon(self):
        message_count = 0

        def next_message(message_pointer, *args):
            nonlocal message_count
            message_count += 1
            if message_count == 2:
                self.assertTrue(daemon.PAUSED.is_set())
                self.assertFalse(daemon.KEYBOARD_LOCKED)
            elif message_count == 3:
                self.assertFalse(daemon.PAUSED.is_set())
            elif message_count > 3:
                return 0
            message = ctypes.cast(message_pointer, ctypes.POINTER(daemon.wintypes.MSG)).contents
            message.message = daemon.WM_HOTKEY
            message.wParam = daemon.HOTKEY_QUIT if message_count == 3 else daemon.HOTKEY_PAUSE
            return 1

        self.win32.GetMessageW.side_effect = next_message
        self.win32.RegisterHotKey.return_value = True
        with patch.object(daemon, "switch_to_default_desktop", return_value=True), \
                patch.object(daemon, "start_keyboard_shield_thread"), \
                patch.object(daemon, "stop_keyboard_shield_thread"):
            self.assertEqual(0, daemon.run_daemon())
        self.win32.RegisterHotKey.assert_any_call(
            None, daemon.HOTKEY_PAUSE,
            daemon.MOD_CONTROL | daemon.MOD_ALT | daemon.MOD_NOREPEAT, daemon.VK_P,
        )
        self.win32.UnregisterHotKey.assert_any_call(None, daemon.HOTKEY_PAUSE)
        self.assertTrue(daemon.STOP_REQUESTED.is_set())

    def test_pause_shortcut_conflict_cleans_up_instead_of_starting_without_pause(self):
        self.win32.RegisterHotKey.side_effect = (
            lambda hwnd, hotkey, modifiers, key: hotkey != daemon.HOTKEY_PAUSE
        )
        with patch.object(daemon, "switch_to_default_desktop", return_value=True), \
                patch.object(daemon, "start_keyboard_shield_thread") as start_hook, \
                patch.object(daemon, "stop_keyboard_shield_thread"):
            self.assertNotEqual(0, daemon.run_daemon())
        start_hook.assert_not_called()
        self.win32.GetMessageW.assert_not_called()
        self.win32.UnregisterHotKey.assert_called_once_with(None, daemon.HOTKEY_QUIT)
        self.kernel32.CloseHandle.assert_any_call(701)
        self.assertTrue(daemon.STOP_REQUESTED.is_set())
        self.assertFalse(daemon.KEYBOARD_LOCKED)


if __name__ == "__main__":
    unittest.main()
