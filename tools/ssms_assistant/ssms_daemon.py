# -*- coding: utf-8 -*-
"""
Stealth SSMS Inline SQL Assistant Daemon (Biometric Human Typing + Fake-Typing Keyboard Shield).
Hotkeys:
  - F8        : Smart suggest (Offline TV5 templates first -> auto fallback to Gemini AI)
  - Alt + Q   : Smart suggest (same as F8)
  - Ctrl + Q  : Force Gemini AI generation for any SQL comment line
While typing (Physical Keyboard Shield active - mash any keys to pretend typing!):
  - Esc       : Stop typing immediately & unlock keyboard
  - F9        : Speed up remaining characters
"""
import ctypes
from ctypes import wintypes
import argparse
from pathlib import Path
import random
import re
import sys
import threading
import time
import traceback
from sql_brain import generate_sql, set_database_context

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

user32 = ctypes.WinDLL("user32", use_last_error=True)
kernel32 = ctypes.WinDLL("kernel32", use_last_error=True)

# Win32 Constants
WM_HOTKEY = 0x0312
WM_QUIT = 0x0012
WM_KEYDOWN = 0x0100
WM_KEYUP = 0x0101
WM_SYSKEYDOWN = 0x0104
WM_SYSKEYUP = 0x0105
PM_NOREMOVE = 0x0000
WH_KEYBOARD_LL = 13
LLKHF_INJECTED = 0x00000010

MOD_ALT = 0x0001
MOD_CONTROL = 0x0002
MOD_NOREPEAT = 0x4000

HOTKEY_ALT_Q = 1
HOTKEY_F8 = 2
HOTKEY_CTRL_Q = 3
HOTKEY_QUIT = 4

VK_BACK = 0x08
VK_TAB = 0x09
VK_RETURN = 0x0D
VK_SHIFT = 0x10
VK_CONTROL = 0x11
VK_MENU = 0x12  # Alt key
VK_ESCAPE = 0x1B
VK_END = 0x23
VK_HOME = 0x24
VK_UP = 0x26
VK_DOWN = 0x28
VK_C = 0x43
VK_Q = 0x51
VK_V = 0x56
VK_F8 = 0x77
VK_F9 = 0x78
VK_LCONTROL, VK_RCONTROL = 0xA2, 0xA3
VK_LMENU, VK_RMENU = 0xA4, 0xA5

INPUT_KEYBOARD = 1
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_UNICODE = 0x0004
CF_UNICODETEXT = 13
GMEM_MOVEABLE = 0x0002
GENERIC_ALL = 0x10000000

# 64-bit safe Win32 structures
ULONG_PTR = ctypes.c_uint64 if ctypes.sizeof(ctypes.c_void_p) == 8 else ctypes.c_uint32
LRESULT = ctypes.c_ssize_t


class KEYBDINPUT(ctypes.Structure):
    _fields_ = [
        ("wVk", wintypes.WORD),
        ("wScan", wintypes.WORD),
        ("dwFlags", wintypes.DWORD),
        ("time", wintypes.DWORD),
        ("dwExtraInfo", ULONG_PTR),
    ]


class MOUSEINPUT(ctypes.Structure):
    _fields_ = [
        ("dx", wintypes.LONG),
        ("dy", wintypes.LONG),
        ("mouseData", wintypes.DWORD),
        ("dwFlags", wintypes.DWORD),
        ("time", wintypes.DWORD),
        ("dwExtraInfo", ULONG_PTR),
    ]


class HARDWAREINPUT(ctypes.Structure):
    _fields_ = [
        ("uMsg", wintypes.DWORD),
        ("wParamL", wintypes.WORD),
        ("wParamH", wintypes.WORD),
    ]


class _INPUTunion(ctypes.Union):
    _fields_ = [
        ("mi", MOUSEINPUT),
        ("ki", KEYBDINPUT),
        ("hi", HARDWAREINPUT),
    ]


class INPUT(ctypes.Structure):
    _fields_ = [
        ("type", wintypes.DWORD),
        ("union", _INPUTunion),
    ]


class KBDLLHOOKSTRUCT(ctypes.Structure):
    _fields_ = [
        ("vkCode", wintypes.DWORD),
        ("scanCode", wintypes.DWORD),
        ("flags", wintypes.DWORD),
        ("time", wintypes.DWORD),
        ("dwExtraInfo", ULONG_PTR),
    ]


HOOKPROC = ctypes.WINFUNCTYPE(LRESULT, ctypes.c_int, wintypes.WPARAM, wintypes.LPARAM)

# Configure 64-bit safe Win32 argtypes/restypes
user32.OpenDesktopW.argtypes = [wintypes.LPCWSTR, wintypes.DWORD, wintypes.BOOL, wintypes.DWORD]
user32.OpenDesktopW.restype = wintypes.HANDLE
user32.SetThreadDesktop.argtypes = [wintypes.HANDLE]
user32.SetThreadDesktop.restype = wintypes.BOOL

user32.OpenClipboard.argtypes = [wintypes.HWND]
user32.OpenClipboard.restype = wintypes.BOOL
user32.CloseClipboard.argtypes = []
user32.CloseClipboard.restype = wintypes.BOOL
user32.EmptyClipboard.argtypes = []
user32.EmptyClipboard.restype = wintypes.BOOL
user32.GetClipboardData.argtypes = [wintypes.UINT]
user32.GetClipboardData.restype = wintypes.HANDLE
user32.SetClipboardData.argtypes = [wintypes.UINT, wintypes.HANDLE]
user32.SetClipboardData.restype = wintypes.HANDLE
user32.SendInput.argtypes = [wintypes.UINT, ctypes.POINTER(INPUT), ctypes.c_int]
user32.SendInput.restype = wintypes.UINT

user32.SetWindowsHookExW.argtypes = [ctypes.c_int, HOOKPROC, wintypes.HINSTANCE, wintypes.DWORD]
user32.SetWindowsHookExW.restype = wintypes.HANDLE
user32.CallNextHookEx.argtypes = [wintypes.HANDLE, ctypes.c_int, wintypes.WPARAM, wintypes.LPARAM]
user32.CallNextHookEx.restype = LRESULT
user32.UnhookWindowsHookEx.argtypes = [wintypes.HANDLE]
user32.UnhookWindowsHookEx.restype = wintypes.BOOL
user32.PostThreadMessageW.argtypes = [wintypes.DWORD, wintypes.UINT, wintypes.WPARAM, wintypes.LPARAM]
user32.PostThreadMessageW.restype = wintypes.BOOL
user32.PeekMessageW.argtypes = [ctypes.POINTER(wintypes.MSG), wintypes.HWND, wintypes.UINT, wintypes.UINT, wintypes.UINT]
user32.PeekMessageW.restype = wintypes.BOOL
user32.GetMessageW.argtypes = [ctypes.POINTER(wintypes.MSG), wintypes.HWND, wintypes.UINT, wintypes.UINT]
user32.GetMessageW.restype = ctypes.c_int
kernel32.GetCurrentThreadId.restype = wintypes.DWORD
kernel32.CreateMutexW.argtypes = [ctypes.c_void_p, wintypes.BOOL, wintypes.LPCWSTR]
kernel32.CreateMutexW.restype = wintypes.HANDLE
kernel32.CloseHandle.argtypes = [wintypes.HANDLE]
kernel32.CloseHandle.restype = wintypes.BOOL

kernel32.GlobalAlloc.argtypes = [wintypes.UINT, ctypes.c_size_t]
kernel32.GlobalAlloc.restype = wintypes.HGLOBAL
kernel32.GlobalLock.argtypes = [wintypes.HGLOBAL]
kernel32.GlobalLock.restype = ctypes.c_void_p
kernel32.GlobalUnlock.argtypes = [wintypes.HGLOBAL]
kernel32.GlobalUnlock.restype = wintypes.BOOL

# Shared state for Physical Keyboard Shield during auto-typing
KEYBOARD_LOCKED = False
ABORT_TYPING = False
FAST_MODE = False
_HOOK_PROC_REF = None
STOP_REQUESTED = threading.Event()
_STATE_LOCK = threading.Lock()
_OPERATION_LOCK = threading.Lock()
_INPUT_LOCK = threading.RLock()
_MAIN_THREAD_ID = 0
_HOOK_THREAD_ID = 0
_HOOK_THREAD = None
_HOOK_READY = threading.Event()
_PHYSICAL_KEYS = set()
_INJECTED_KEYS = set()


def request_shutdown() -> None:
    """Stop from any thread, including while a background API request is waiting."""
    global ABORT_TYPING, KEYBOARD_LOCKED
    STOP_REQUESTED.set()
    with _STATE_LOCK:
        ABORT_TYPING = True
        KEYBOARD_LOCKED = False
    for thread_id in (_MAIN_THREAD_ID, _HOOK_THREAD_ID):
        if thread_id:
            user32.PostThreadMessageW(thread_id, WM_QUIT, 0, 0)


def _release_injected_keys() -> None:
    with _INPUT_LOCK:
        for vk in tuple(_INJECTED_KEYS):
            _key_up(vk)


def switch_to_default_desktop() -> bool:
    """Attach current thread to interactive WinSta0\\Default desktop."""
    h_desk = user32.OpenDesktopW("Default", 0, False, GENERIC_ALL)
    if not h_desk:
        return False
    return bool(user32.SetThreadDesktop(h_desk))


def _low_level_keyboard_proc(n_code: int, w_param: int, l_param: int) -> int:
    """
    WH_KEYBOARD_LL callback:
      - When KEYBOARD_LOCKED is True, blocks all physical hardware keystrokes (so user can
        mash keys freely to pretend typing!) while letting injected SendInput/keybd_event
        characters pass straight into SSMS.
      - Physical Esc cancels typing immediately; physical F9 enables turbo speed.
    """
    global ABORT_TYPING, FAST_MODE, KEYBOARD_LOCKED
    if n_code >= 0:
        kb = ctypes.cast(l_param, ctypes.POINTER(KBDLLHOOKSTRUCT)).contents
        is_injected = bool(kb.flags & LLKHF_INJECTED)
        if not is_injected:
            if w_param in (WM_KEYDOWN, WM_SYSKEYDOWN):
                _PHYSICAL_KEYS.add(kb.vkCode)
            elif w_param in (WM_KEYUP, WM_SYSKEYUP):
                _PHYSICAL_KEYS.discard(kb.vkCode)
            ctrl = bool(_PHYSICAL_KEYS.intersection((VK_CONTROL, VK_LCONTROL, VK_RCONTROL)))
            alt = bool(_PHYSICAL_KEYS.intersection((VK_MENU, VK_LMENU, VK_RMENU)))
            if not _INJECTED_KEYS.intersection((VK_CONTROL, VK_LCONTROL, VK_RCONTROL)):
                ctrl = ctrl or bool(user32.GetAsyncKeyState(VK_CONTROL) & 0x8000)
            if not _INJECTED_KEYS.intersection((VK_MENU, VK_LMENU, VK_RMENU)):
                alt = alt or bool(user32.GetAsyncKeyState(VK_MENU) & 0x8000)
            if kb.vkCode == VK_Q and w_param in (WM_KEYDOWN, WM_SYSKEYDOWN) and ctrl and alt:
                request_shutdown()
                return 1
            if not KEYBOARD_LOCKED or STOP_REQUESTED.is_set():
                return user32.CallNextHookEx(None, n_code, w_param, l_param)
            if w_param in (WM_KEYDOWN, WM_SYSKEYDOWN):
                if kb.vkCode == VK_ESCAPE:
                    ABORT_TYPING = True
                    KEYBOARD_LOCKED = False
                elif kb.vkCode == VK_F9:
                    FAST_MODE = True
            # Swallow 100% of physical keystrokes while auto-typing is active
            return 1
    return user32.CallNextHookEx(None, n_code, w_param, l_param)


def start_keyboard_shield_thread() -> None:
    """Run dedicated non-blocking message loop thread for WH_KEYBOARD_LL."""
    global _HOOK_PROC_REF, _HOOK_THREAD
    _HOOK_PROC_REF = HOOKPROC(_low_level_keyboard_proc)

    def _hook_worker() -> None:
        global _HOOK_THREAD_ID
        h_hook = None
        try:
            switch_to_default_desktop()
            msg = wintypes.MSG()
            user32.PeekMessageW(ctypes.byref(msg), None, 0, 0, PM_NOREMOVE)
            _HOOK_THREAD_ID = kernel32.GetCurrentThreadId()
            if STOP_REQUESTED.is_set():
                return
            h_hook = user32.SetWindowsHookExW(WH_KEYBOARD_LL, _HOOK_PROC_REF, None, 0)
            if not h_hook:
                print("[WARN] Khong the khoi tao Keyboard Shield (WH_KEYBOARD_LL).", flush=True)
                return
            _HOOK_READY.set()
            while not STOP_REQUESTED.is_set() and user32.GetMessageW(ctypes.byref(msg), None, 0, 0) > 0:
                user32.TranslateMessage(ctypes.byref(msg))
                user32.DispatchMessageW(ctypes.byref(msg))
        finally:
            if h_hook:
                user32.UnhookWindowsHookEx(h_hook)
            _HOOK_THREAD_ID = 0
            _HOOK_READY.set()

    _HOOK_READY.clear()
    _HOOK_THREAD = threading.Thread(target=_hook_worker, name="SSMSKeyboardHook", daemon=True)
    _HOOK_THREAD.start()
    _HOOK_READY.wait(timeout=1)


def stop_keyboard_shield_thread() -> None:
    if _HOOK_THREAD_ID:
        user32.PostThreadMessageW(_HOOK_THREAD_ID, WM_QUIT, 0, 0)
    if _HOOK_THREAD is not None and _HOOK_THREAD is not threading.current_thread():
        _HOOK_THREAD.join(timeout=0.5)


def get_active_window_title() -> str:
    hwnd = user32.GetForegroundWindow()
    if not hwnd:
        return ""
    buf = ctypes.create_unicode_buffer(256)
    user32.GetWindowTextW(hwnd, buf, 256)
    return buf.value


def get_clipboard_text() -> str:
    for _ in range(15):
        if user32.OpenClipboard(None):
            break
        time.sleep(0.015)
    else:
        return ""
    try:
        handle = user32.GetClipboardData(CF_UNICODETEXT)
        if not handle:
            return ""
        ptr = kernel32.GlobalLock(handle)
        if not ptr:
            return ""
        try:
            return ctypes.wstring_at(ptr)
        finally:
            kernel32.GlobalUnlock(handle)
    finally:
        user32.CloseClipboard()


def set_clipboard_text(text: str) -> bool:
    data = (text + "\0").encode("utf-16-le")
    for _ in range(15):
        if user32.OpenClipboard(None):
            break
        time.sleep(0.015)
    else:
        return False
    try:
        user32.EmptyClipboard()
        h_mem = kernel32.GlobalAlloc(GMEM_MOVEABLE, len(data))
        if not h_mem:
            return False
        ptr = kernel32.GlobalLock(h_mem)
        if not ptr:
            return False
        ctypes.memmove(ptr, data, len(data))
        kernel32.GlobalUnlock(h_mem)
        user32.SetClipboardData(CF_UNICODETEXT, h_mem)
        return True
    finally:
        user32.CloseClipboard()


def _key_down(vk: int) -> None:
    with _INPUT_LOCK:
        if STOP_REQUESTED.is_set():
            return
        user32.keybd_event(vk, 0, 0, 0)
        _INJECTED_KEYS.add(vk)


def _key_up(vk: int) -> None:
    with _INPUT_LOCK:
        user32.keybd_event(vk, 0, KEYEVENTF_KEYUP, 0)
        _INJECTED_KEYS.discard(vk)


def _tap_key(vk: int, hold: float = 0.015) -> None:
    if STOP_REQUESTED.is_set() or ABORT_TYPING:
        return
    try:
        _key_down(vk)
        time.sleep(hold)
    finally:
        _key_up(vk)


def _send_unicode_char(ch: str) -> None:
    """Send a single Unicode character via Win32 SendInput(KEYEVENTF_UNICODE)."""
    code = ord(ch)
    inp_down = INPUT(
        type=INPUT_KEYBOARD,
        union=_INPUTunion(
            ki=KEYBDINPUT(wVk=0, wScan=code, dwFlags=KEYEVENTF_UNICODE, time=0, dwExtraInfo=0)
        ),
    )
    inp_up = INPUT(
        type=INPUT_KEYBOARD,
        union=_INPUTunion(
            ki=KEYBDINPUT(
                wVk=0,
                wScan=code,
                dwFlags=KEYEVENTF_UNICODE | KEYEVENTF_KEYUP,
                time=0,
                dwExtraInfo=0,
            )
        ),
    )
    arr = (INPUT * 2)(inp_down, inp_up)
    with _INPUT_LOCK:
        if not STOP_REQUESTED.is_set() and not ABORT_TYPING:
            user32.SendInput(2, arr, ctypes.sizeof(INPUT))


def _unindent_ssms_line(tabs_to_unindent: int = 1) -> None:
    """Use Shift+Tab to unindent in SSMS safely (NEVER deletes a newline!)."""
    for _ in range(max(1, tabs_to_unindent)):
        _key_down(VK_SHIFT)
        time.sleep(0.01)
        _tap_key(VK_TAB, 0.012)
        time.sleep(0.01)
        _key_up(VK_SHIFT)
        time.sleep(0.015)


QWERTY_NEIGHBORS = {
    "A": "S", "B": "V", "C": "X", "D": "F", "E": "R", "F": "G", "G": "H",
    "H": "J", "I": "O", "J": "K", "K": "L", "L": "K", "M": "N", "N": "M",
    "O": "R", "P": "O", "R": "O", "S": "D", "T": "R", "U": "I", "V": "C",
    "W": "E", "Y": "T",
    "a": "s", "c": "x", "d": "f", "e": "r", "f": "g", "g": "h", "h": "j",
    "i": "o", "l": "k", "m": "n", "n": "m", "o": "i", "r": "e", "s": "d",
    "t": "r", "u": "i", "v": "b",
}


def type_sql_humanlike(sql_text: str) -> None:
    """
    Type SQL matching the user's exact measured biometric profile (typing_sample.json)
    while physical keyboard input is shielded so user can freely pretend-type on keyboard.
    """
    global ABORT_TYPING, FAST_MODE
    lines = [line.rstrip() for line in sql_text.replace("\r\n", "\n").split("\n") if line.strip()]
    if not lines:
        return

    prev_spaces = 0
    typos_made = 0

    for line_idx, raw_line in enumerate(lines):
        if STOP_REQUESTED.is_set() or ABORT_TYPING or (user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000):
            print("[SSMS-Assistant] Da dung go theo lenh Esc!", flush=True)
            return

        stripped = raw_line.lstrip(" \t")
        cur_spaces = len(raw_line) - len(stripped)

        if line_idx > 0:
            time.sleep(0.02 if FAST_MODE else random.uniform(0.35, 0.70))
            _tap_key(VK_RETURN, 0.025)
            time.sleep(0.03 if FAST_MODE else random.uniform(0.75, 1.35))

            if cur_spaces == prev_spaces:
                text_to_type = stripped
            elif cur_spaces > prev_spaces:
                text_to_type = (" " * (cur_spaces - prev_spaces)) + stripped
            else:
                tabs = max(1, (prev_spaces - cur_spaces + 3) // 4)
                _unindent_ssms_line(tabs)
                text_to_type = stripped
        else:
            text_to_type = (" " * cur_spaces) + stripped

        prev_spaces = cur_spaces
        word_char_pos = 0

        for ch in text_to_type:
            if STOP_REQUESTED.is_set() or ABORT_TYPING or (user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000):
                print("[SSMS-Assistant] Da dung go theo lenh Esc!", flush=True)
                return
            if user32.GetAsyncKeyState(VK_F9) & 0x8000:
                FAST_MODE = True

            if FAST_MODE:
                _send_unicode_char(ch)
                time.sleep(0.015)
                continue

            if ch == " " and word_char_pos > 0:
                time.sleep(random.uniform(0.18, 0.42))

            if (
                word_char_pos >= 2
                and ch in QWERTY_NEIGHBORS
                and typos_made < 2
                and random.random() < 0.06
            ):
                wrong_ch = QWERTY_NEIGHBORS[ch]
                _send_unicode_char(wrong_ch)
                time.sleep(random.uniform(0.42, 0.64))
                _tap_key(VK_BACK, 0.02)
                time.sleep(random.uniform(0.22, 0.36))
                typos_made += 1

            _send_unicode_char(ch)

            if ch == " ":
                word_char_pos = 0
                time.sleep(random.uniform(0.65, 1.30))
            elif ch in (",", ";", "=", "(", ")"):
                word_char_pos = 0
                time.sleep(random.uniform(0.32, 0.68))
            else:
                word_char_pos += 1
                if word_char_pos == 1:
                    time.sleep(random.uniform(0.26, 0.50))
                else:
                    time.sleep(random.uniform(0.105, 0.285))


def _wait_modifiers_released(hotkey_id: int, timeout: float = 0.8) -> None:
    if hotkey_id == HOTKEY_ALT_Q:
        _tap_key(VK_CONTROL, 0.01)

    deadline = time.time() + timeout
    while time.time() < deadline:
        alt_down = bool(user32.GetAsyncKeyState(VK_MENU) & 0x8000)
        ctrl_down = bool(user32.GetAsyncKeyState(VK_CONTROL) & 0x8000)
        shift_down = bool(user32.GetAsyncKeyState(VK_SHIFT) & 0x8000)
        if not (alt_down or ctrl_down or shift_down):
            break
        time.sleep(0.015)

    _key_up(VK_MENU)
    _key_up(VK_CONTROL)
    _key_up(VK_SHIFT)
    time.sleep(0.025)


def _copy_current_line_text() -> str:
    """Select from End of line to Home and copy via Ctrl+C."""
    set_clipboard_text("")
    time.sleep(0.015)

    _tap_key(VK_END, 0.015)
    time.sleep(0.02)
    _key_down(VK_SHIFT)
    time.sleep(0.015)
    _tap_key(VK_HOME, 0.015)
    time.sleep(0.015)
    _key_up(VK_SHIFT)
    time.sleep(0.02)

    _key_down(VK_CONTROL)
    time.sleep(0.015)
    _tap_key(VK_C, 0.015)
    time.sleep(0.015)
    _key_up(VK_CONTROL)

    for _ in range(15):
        time.sleep(0.015)
        txt = get_clipboard_text()
        if txt:
            return txt
    return ""


def parse_ssms_connection(window_title: str):
    """Read server/database from the SSMS query title, not from sample settings."""
    match = re.search(r"\.sql\s+-\s+(\S+)\.([^\r\n]+?)\s+\(", window_title, re.IGNORECASE)
    if not match:
        return None
    return match.group(1).strip(), match.group(2).strip()


def handle_hotkey_trigger(hotkey_id: int) -> None:
    global KEYBOARD_LOCKED, ABORT_TYPING, FAST_MODE
    with _STATE_LOCK:
        if STOP_REQUESTED.is_set():
            return
        ABORT_TYPING = False
    win_title = get_active_window_title()
    connection = parse_ssms_connection(win_title)
    if connection is None:
        print("[SSMS-Assistant] Hãy chọn cửa sổ Query đã kết nối database trong SSMS.", flush=True)
        return
    set_database_context(*connection)
    _wait_modifiers_released(hotkey_id)

    old_clipboard = get_clipboard_text()

    # Case 1: Try reading the line where the cursor currently sits
    copied_line = _copy_current_line_text()

    if copied_line.strip():
        _tap_key(VK_END, 0.015)
        time.sleep(0.025)
        _tap_key(VK_RETURN, 0.02)
        time.sleep(0.05)
    else:
        # Case 2: User already pressed Enter and cursor is on an empty line below the comment
        _tap_key(VK_UP, 0.015)
        time.sleep(0.025)
        copied_line = _copy_current_line_text()
        _tap_key(VK_END, 0.015)
        time.sleep(0.02)
        _tap_key(VK_DOWN, 0.015)
        time.sleep(0.04)

    if old_clipboard:
        set_clipboard_text(old_clipboard)

    if not copied_line.strip():
        return

    force_ai = (hotkey_id == HOTKEY_CTRL_Q)
    print(
        f"[SSMS-Assistant] Window='{win_title}' | ForceAI={force_ai} | Prompt='{copied_line.strip()}'",
        flush=True,
    )

    # Activate Physical Keyboard Shield right away so user can start pretend-typing on keyboard!
    with _STATE_LOCK:
        if STOP_REQUESTED.is_set():
            return
        FAST_MODE = False
        KEYBOARD_LOCKED = True
    try:
        sql_block = generate_sql(copied_line, force_ai=force_ai, clean_comments=True)
        if STOP_REQUESTED.is_set() or ABORT_TYPING:
            return
        type_sql_humanlike(sql_block)
        print(f"[SSMS-Assistant] -> Da go xong {len(sql_block.splitlines())} dong SQL!", flush=True)
    finally:
        # Always unlock physical keyboard immediately when done or aborted
        KEYBOARD_LOCKED = False
        _release_injected_keys()


def start_hotkey_task(hotkey_id: int) -> bool:
    """Keep the main message loop free to process the exit shortcut during API work."""
    if STOP_REQUESTED.is_set() or not _OPERATION_LOCK.acquire(blocking=False):
        return False

    def _worker() -> None:
        try:
            switch_to_default_desktop()
            handle_hotkey_trigger(hotkey_id)
        except Exception:
            traceback.print_exc()
        finally:
            _OPERATION_LOCK.release()

    try:
        threading.Thread(target=_worker, name="SSMSSuggestion", daemon=True).start()
    except Exception:
        _OPERATION_LOCK.release()
        raise
    return True


def run_daemon() -> int:
    global _MAIN_THREAD_ID
    mutex = kernel32.CreateMutexW(None, False, "Local\\SSMSInlineAssistant")
    if not mutex:
        raise ctypes.WinError(ctypes.get_last_error())
    if ctypes.get_last_error() == 183:  # ERROR_ALREADY_EXISTS
        kernel32.CloseHandle(mutex)
        print("[SSMS-Assistant] Tro ly da chay; Ctrl+Alt+Q de thoat.", flush=True)
        return 0
    registered = []
    try:
        STOP_REQUESTED.clear()
        _PHYSICAL_KEYS.clear()
        switch_to_default_desktop()
        msg = wintypes.MSG()
        user32.PeekMessageW(ctypes.byref(msg), None, 0, 0, PM_NOREMOVE)
        _MAIN_THREAD_ID = kernel32.GetCurrentThreadId()
        shortcuts = (
            (HOTKEY_QUIT, MOD_CONTROL | MOD_ALT | MOD_NOREPEAT, VK_Q),
            (HOTKEY_ALT_Q, MOD_ALT | MOD_NOREPEAT, VK_Q),
            (HOTKEY_F8, MOD_NOREPEAT, VK_F8),
            (HOTKEY_CTRL_Q, MOD_CONTROL | MOD_NOREPEAT, VK_Q),
        )
        for hotkey_id, modifiers, key in shortcuts:
            if user32.RegisterHotKey(None, hotkey_id, modifiers, key):
                registered.append(hotkey_id)
            elif hotkey_id == HOTKEY_QUIT:
                print("[LOI] Ctrl+Alt+Q dang duoc chuong trinh khac su dung.", flush=True)
                return 1
        if not any(key in registered for key in (HOTKEY_ALT_Q, HOTKEY_F8, HOTKEY_CTRL_Q)):
            print("[LOI] Khong the dang ky phim tat goi y SQL.", flush=True)
            return 1
        start_keyboard_shield_thread()
        print("[SSMS-Assistant] San sang. Ctrl+Alt+Q: thoat; Esc: dung go; F9: tang toc.", flush=True)
        while not STOP_REQUESTED.is_set():
            result = user32.GetMessageW(ctypes.byref(msg), None, 0, 0)
            if result == 0:
                break
            if result == -1:
                raise ctypes.WinError(ctypes.get_last_error())
            if msg.message == WM_HOTKEY and msg.wParam == HOTKEY_QUIT:
                request_shutdown()
                break
            if msg.message == WM_HOTKEY and msg.wParam in (HOTKEY_ALT_Q, HOTKEY_F8, HOTKEY_CTRL_Q):
                start_hotkey_task(int(msg.wParam))
            user32.TranslateMessage(ctypes.byref(msg))
            user32.DispatchMessageW(ctypes.byref(msg))
    except KeyboardInterrupt:
        pass
    finally:
        request_shutdown()
        _release_injected_keys()
        for hotkey_id in registered:
            user32.UnregisterHotKey(None, hotkey_id)
        stop_keyboard_shield_thread()
        _MAIN_THREAD_ID = 0
        kernel32.CloseHandle(mutex)
    return 0


def main(argv=None) -> int:
    parser = argparse.ArgumentParser(description="SSMS SQL assistant")
    parser.add_argument("--hidden", action="store_true", help="Write output to assistant.log")
    parser.add_argument("--check", action="store_true", help="Check startup without installing hooks")
    args = parser.parse_args(argv)
    if args.hidden or sys.stdout is None or sys.stderr is None:
        log_path = Path(__file__).with_name("assistant.log")
        log = log_path.open("a", encoding="utf-8", buffering=1)
        sys.stdout = sys.stderr = log
    if args.check:
        print("[SSMS-Assistant] Startup check OK.", flush=True)
        return 0
    try:
        result = run_daemon()
    except Exception:
        traceback.print_exc()
        result = 1
    if result and args.hidden:
        user32.MessageBoxW(None, "Khong the khoi dong tro ly. Xem tools/ssms_assistant/assistant.log.", "Tro ly SSMS", 0x10)
    return result


if __name__ == "__main__":
    sys.exit(main())
