# -*- coding: utf-8 -*-
"""
Stealth SSMS Inline SQL Assistant Daemon (Human Typing Simulation + Stealth AI).
Hotkeys:
  - F8        : Smart suggest (Offline TV5 templates first -> auto fallback to Gemini AI)
  - Alt + Q   : Smart suggest (same as F8)
  - Ctrl + Q  : Force Gemini AI generation for any SQL comment line
While typing:
  - Esc       : Stop typing immediately
  - F9        : Speed up remaining characters
"""
import ctypes
from ctypes import wintypes
import random
import sys
import time
from sql_brain import generate_sql

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")
if hasattr(sys.stderr, "reconfigure"):
    sys.stderr.reconfigure(encoding="utf-8", errors="replace")

user32 = ctypes.WinDLL("user32", use_last_error=True)
kernel32 = ctypes.WinDLL("kernel32", use_last_error=True)

# Win32 Constants
WM_HOTKEY = 0x0312
MOD_ALT = 0x0001
MOD_CONTROL = 0x0002
MOD_NOREPEAT = 0x4000

HOTKEY_ALT_Q = 1
HOTKEY_F8 = 2
HOTKEY_CTRL_Q = 3

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

INPUT_KEYBOARD = 1
KEYEVENTF_KEYUP = 0x0002
KEYEVENTF_UNICODE = 0x0004
CF_UNICODETEXT = 13
GMEM_MOVEABLE = 0x0002
GENERIC_ALL = 0x10000000

# 64-bit safe Win32 INPUT structures for SendInput
ULONG_PTR = ctypes.c_uint64 if ctypes.sizeof(ctypes.c_void_p) == 8 else ctypes.c_uint32


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

kernel32.GlobalAlloc.argtypes = [wintypes.UINT, ctypes.c_size_t]
kernel32.GlobalAlloc.restype = wintypes.HGLOBAL
kernel32.GlobalLock.argtypes = [wintypes.HGLOBAL]
kernel32.GlobalLock.restype = ctypes.c_void_p
kernel32.GlobalUnlock.argtypes = [wintypes.HGLOBAL]
kernel32.GlobalUnlock.restype = wintypes.BOOL


def switch_to_default_desktop() -> bool:
    """Attach current thread to interactive WinSta0\\Default desktop."""
    h_desk = user32.OpenDesktopW("Default", 0, False, GENERIC_ALL)
    if not h_desk:
        return False
    return bool(user32.SetThreadDesktop(h_desk))


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
    user32.keybd_event(vk, 0, 0, 0)


def _key_up(vk: int) -> None:
    user32.keybd_event(vk, 0, KEYEVENTF_KEYUP, 0)


def _tap_key(vk: int, hold: float = 0.015) -> None:
    _key_down(vk)
    time.sleep(hold)
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
    Type SQL matching the user's exact measured biometric profile (typing_sample.json):
      - First char -> second char of word: ~260ms - 520ms (finding key position)
      - Intra-word burst (e.g. L-E-C-T, R-O-M): ~105ms - 280ms
      - Pause before Space: ~200ms - 450ms
      - Thinking pause after Space / before next word: ~650ms - 1350ms
      - Newline pause (Enter): ~750ms - 1400ms
      - Occasional human typo + ~450ms realization + Backspace + ~250ms retype
    """
    lines = [line.rstrip() for line in sql_text.replace("\r\n", "\n").split("\n") if line.strip()]
    if not lines:
        return

    fast_mode = False
    prev_spaces = 0
    typos_made = 0

    for line_idx, raw_line in enumerate(lines):
        if user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000:
            print("[SSMS-Assistant] Da dung go theo lenh Esc!", flush=True)
            return

        stripped = raw_line.lstrip(" \t")
        cur_spaces = len(raw_line) - len(stripped)

        if line_idx > 0:
            # Pause before hitting Enter at end of line, then pause on new line
            time.sleep(0.02 if fast_mode else random.uniform(0.35, 0.70))
            _tap_key(VK_RETURN, 0.025)
            time.sleep(0.03 if fast_mode else random.uniform(0.75, 1.35))

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

        for idx, ch in enumerate(text_to_type):
            if user32.GetAsyncKeyState(VK_ESCAPE) & 0x8000:
                print("[SSMS-Assistant] Da dung go theo lenh Esc!", flush=True)
                return
            if user32.GetAsyncKeyState(VK_F9) & 0x8000:
                fast_mode = True

            if fast_mode:
                _send_unicode_char(ch)
                time.sleep(0.015)
                continue

            # Slight hesitation right before pressing Space (measured: 202ms - 539ms)
            if ch == " " and word_char_pos > 0:
                time.sleep(random.uniform(0.18, 0.42))

            # Occasional realistic 1-char typo + Backspace correction inside a word (like FOR -> Backspace -> ROM)
            if (
                word_char_pos >= 2
                and ch in QWERTY_NEIGHBORS
                and typos_made < 2
                and random.random() < 0.06
            ):
                wrong_ch = QWERTY_NEIGHBORS[ch]
                _send_unicode_char(wrong_ch)
                # Realization pause (measured in sample: 452ms - 658ms)
                time.sleep(random.uniform(0.42, 0.64))
                _tap_key(VK_BACK, 0.02)
                # Recovery pause before typing the right key (measured: 183ms - 361ms)
                time.sleep(random.uniform(0.22, 0.36))
                typos_made += 1

            _send_unicode_char(ch)

            if ch == " ":
                word_char_pos = 0
                # Thinking pause after Space before starting next word/keyword (measured: 958ms - 1571ms)
                time.sleep(random.uniform(0.65, 1.30))
            elif ch in (",", ";", "=", "(", ")"):
                word_char_pos = 0
                time.sleep(random.uniform(0.32, 0.68))
            else:
                word_char_pos += 1
                if word_char_pos == 1:
                    # Transition from 1st letter to 2nd letter of word (measured: 380ms - 644ms)
                    time.sleep(random.uniform(0.26, 0.50))
                else:
                    # Intra-word burst (measured: 94ms - 322ms)
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


def handle_hotkey_trigger(hotkey_id: int) -> None:
    win_title = get_active_window_title()
    _wait_modifiers_released(hotkey_id)

    old_clipboard = get_clipboard_text()

    # Case 1: Try reading the line where the cursor currently sits
    copied_line = _copy_current_line_text()

    if copied_line.strip():
        # Cursor was on the comment line -> unselect (End) and press Enter to go to next line!
        _tap_key(VK_END, 0.015)
        time.sleep(0.025)
        _tap_key(VK_RETURN, 0.02)
        time.sleep(0.05)
    else:
        # Case 2: User already pressed Enter and cursor is on an empty line below the comment!
        # Move up 1 line to read the comment, then come back down to this empty line!
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

    # Generate clean, comment-free multi-line SQL
    sql_block = generate_sql(copied_line, force_ai=force_ai, clean_comments=True)

    # Type character-by-character on the new line below the comment
    type_sql_humanlike(sql_block)
    print(f"[SSMS-Assistant] -> Da go xong {len(sql_block.splitlines())} dong SQL!", flush=True)


def run_daemon() -> int:
    switched = switch_to_default_desktop()

    ok_alt_q = bool(user32.RegisterHotKey(None, HOTKEY_ALT_Q, MOD_ALT | MOD_NOREPEAT, VK_Q))
    ok_f8 = bool(user32.RegisterHotKey(None, HOTKEY_F8, MOD_NOREPEAT, VK_F8))
    ok_ctrl_q = bool(user32.RegisterHotKey(None, HOTKEY_CTRL_Q, MOD_CONTROL | MOD_NOREPEAT, VK_Q))

    if not (ok_alt_q or ok_f8 or ok_ctrl_q):
        print("[LOI] Khong the dang ky phim tat Alt+Q, Ctrl+Q hoac F8.", flush=True)
        return 1

    print("======================================================================", flush=True)
    print("  TRO LY GO CODE NHU NGUOI THAT TRONG SSMS DA SAN SANG!               ", flush=True)
    print(f"  - Desktop ket noi: Default (attached={switched})                    ", flush=True)
    print(f"  - F8 / Alt+Q : Goi y thong minh (Offline -> tu dong AI)             ", flush=True)
    print(f"  - Ctrl + Q   : Ep goi AI Gemini tu bat ky dong comment nao          ", flush=True)
    print("  - Ho tro ca 2 kieu: Bam ngay cuoi dong comment HOAC Enter xuong dong", flush=True)
    print("  - Esc        : Dung go ngay lap tuc                                 ", flush=True)
    print("  - F9         : Tang toc go het phan con lai                         ", flush=True)
    print("======================================================================", flush=True)

    msg = wintypes.MSG()
    try:
        while user32.GetMessageW(ctypes.byref(msg), None, 0, 0) != 0:
            if msg.message == WM_HOTKEY and msg.wParam in (HOTKEY_ALT_Q, HOTKEY_F8, HOTKEY_CTRL_Q):
                try:
                    handle_hotkey_trigger(int(msg.wParam))
                except Exception as exc:
                    print(f"[WARN] Loi xu ly phim tat: {exc}", flush=True)
            user32.TranslateMessage(ctypes.byref(msg))
            user32.DispatchMessageW(ctypes.byref(msg))
    except KeyboardInterrupt:
        pass
    finally:
        if ok_alt_q:
            user32.UnregisterHotKey(None, HOTKEY_ALT_Q)
        if ok_f8:
            user32.UnregisterHotKey(None, HOTKEY_F8)
        if ok_ctrl_q:
            user32.UnregisterHotKey(None, HOTKEY_CTRL_Q)
    return 0


if __name__ == "__main__":
    sys.exit(run_daemon())
