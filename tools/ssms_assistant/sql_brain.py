# -*- coding: utf-8 -*-
"""
Schema-Aware SQL Generator for QuanLyNhanSuTienLuong (SSMS Inline Assistant).
Understands Vietnamese prompts (with or without accents) starting with `-- #...` or `#...`.
"""
import json
import os
import re
import unicodedata
import urllib.request
import urllib.error
from typing import Optional, Tuple, List

from schema_context import load_schema_context
from sql_format import code_mask, materialize_payroll_exec_arguments, prepare_sql


def strip_accents(text: str) -> str:
    if not text:
        return ""
    text = text.replace("đ", "d").replace("Đ", "D")
    norm = unicodedata.normalize("NFD", text)
    return "".join(ch for ch in norm if unicodedata.category(ch) != "Mn").lower()


# ---------------------------------------------------------------------------
# Metadata Dictionaries for Dynamic Slot Extraction
# ---------------------------------------------------------------------------
ROLES = [
    ("role_HRManager", "gui_hr", ["role_hrmanager", "hr_manager", "gui_hr", "nhan su", "qlns", "hr"]),
    ("role_PayrollOfficer", "gui_payroll", ["role_payrollofficer", "payroll_officer", "gui_payroll", "ke toan", "payroll", "tinh luong", "kt"]),
    ("role_Employee", "gui_a", ["role_employee", "employee", "gui_a", "gui_b", "nhan vien", "nv"]),
    ("role_DBAdmin", "gui_admin", ["role_dbadmin", "db_admin", "gui_admin", "admin", "quan tri", "dba"]),
]

OBJECTS = [
    # Stored Procedures (is_exec = True)
    ("dbo.sp_ChotBangLuong", True, ["sp_chotbangluong", "chot bang luong", "chot luong", "chot ky luong"]),
    ("dbo.sp_HuyChotBangLuong", True, ["sp_huychotbangluong", "huy chot", "mo lai ky luong", "mo lai bang luong", "mo ky luong"]),
    ("dbo.sp_TinhBangLuongThang", True, ["sp_tinhbangluongthang", "tinh bang luong", "tinh luong thang", "sp tinh luong"]),
    ("dbo.sp_XoaBangLuongChuaChot", True, ["sp_xoabangluongchuachot", "xoa bang luong chua chot"]),
    ("dbo.sp_XoaKyLuongChuaChot", True, ["sp_xoakyluongchuachot", "xoa ky luong chua chot", "xoa ky nhap"]),
    ("dbo.sp_ThemNhanVien", True, ["sp_themnhanvien", "sp them nhan vien", "them nhan vien"]),
    ("dbo.sp_GhiNhanChamCong", True, ["sp_ghinhanchamcong", "ghi nhan cham cong", "sp cham cong"]),
    ("dbo.sp_ThemPhuCapNhanVien", True, ["sp_themphucapnhanvien", "sp them phu cap"]),
    ("dbo.sp_LayTaiKhoanHienTai", True, ["sp_laytaikhoanhientai", "lay tai khoan hien tai"]),
    ("dbo.sp_MigrateMatKhau", True, ["sp_migratematkhau", "migrate mat khau"]),
    ("dbo.sp_AdminResetPassword", True, ["sp_adminresetpassword", "reset password", "dat lai mat khau"]),
    ("dbo.sp_AdminSetRole", True, ["sp_adminsetrole", "set role", "doi vai tro"]),
    ("dbo.sp_AdminSetStatus", True, ["sp_adminsetstatus", "set status", "khoa tai khoan"]),
    ("dbo.sp_DBAProvisionIdentity", True, ["sp_dbaprovisionidentity", "provision identity"]),
    # Functions (is_exec = True)
    ("dbo.fn_TinhThucNhan", True, ["fn_tinhthucnhan", "ham tinh thuc nhan", "tinh thuc nhan"]),
    ("dbo.fn_TinhTienCong", True, ["fn_tinhtiencong", "ham tinh tien cong", "tinh tien cong"]),
    ("dbo.fn_TongPhuCap", True, ["fn_tongphucap", "ham tong phu cap"]),
    ("dbo.fn_TongKhauTru", True, ["fn_tongkhautru", "ham tong khau tru"]),
    ("dbo.fn_TinhSoNgayCong", True, ["fn_tinhsongaycong", "ham tinh so ngay cong"]),
    # Views (is_exec = False)
    ("dbo.vw_PhieuLuongCaNhan", False, ["vw_phieuluongcanhan", "phieu luong ca nhan", "view ca nhan", "view rls"]),
    ("dbo.vw_BangLuongChiTiet", False, ["vw_bangluongchitiet", "view bang luong chi tiet", "view luong tong"]),
    ("dbo.vw_TongHopChamCongThang", False, ["vw_tonghopchamcongthang", "view cham cong"]),
    ("dbo.vw_TongPhuCapThang", False, ["vw_tongphucapthang", "view phu cap"]),
    ("dbo.vw_TongKhauTruThang", False, ["vw_tongkhautruthang", "view khau tru"]),
    ("dbo.vw_NhanVien_PhongBan_ChucVu", False, ["vw_nhanvien_phongban_chucvu", "view nhan vien"]),
    # Tables (is_exec = False)
    ("dbo.CHITIETBANGLUONG", False, ["chitietbangluong", "chi tiet bang luong", "chi tiet luong"]),
    ("dbo.BANGLUONG", False, ["bangluong", "bang luong"]),
    ("dbo.PHUCAPNHANVIEN", False, ["phucapnhanvien", "phu cap nhan vien", "bang phu cap", "phu cap"]),
    ("dbo.KHAUTRUNHANVIEN", False, ["khautrunhanvien", "khau tru nhan vien", "bang khau tru", "khau tru"]),
    ("dbo.CHAMCONG", False, ["chamcong", "bang cham cong", "cham cong"]),
    ("dbo.LICHSULUONG", False, ["lichsuluong", "lich su luong"]),
    ("dbo.TAIKHOAN", False, ["taikhoan", "bang tai khoan", "tai khoan"]),
    ("dbo.NHANVIEN", False, ["nhanvien", "bang nhan vien", "ho so nhan vien", "nhan vien"]),
    ("dbo.PHONGBAN", False, ["phongban", "bang phong ban", "phong ban"]),
    ("dbo.CHUCVU", False, ["chucvu", "bang chuc vu", "chuc vu"]),
]


def _extract_role(norm: str) -> Tuple[str, str]:
    for role_name, login_name, keywords in ROLES:
        for kw in keywords:
            if re.search(r"\b" + re.escape(kw) + r"\b", norm):
                return role_name, login_name
    return "role_HRManager", "gui_hr"


def _extract_object(norm: str) -> Tuple[str, bool]:
    # Mask out role phrases first so 'nhan vien' in 'cho nhan vien' doesn't false-match table NHANVIEN
    cleaned = re.sub(
        r"\b(cho|cua|role|vai tro)\s+(nhan su|hr|ke toan|payroll|nhan vien|employee|admin|quan tri)\b",
        " ",
        norm,
    )
    for obj_name, is_exec, keywords in OBJECTS:
        for kw in keywords:
            if kw in cleaned:
                return obj_name, is_exec
    for obj_name, is_exec, keywords in OBJECTS:
        for kw in keywords:
            if kw in norm:
                return obj_name, is_exec
    return "dbo.sp_ChotBangLuong", True


def _extract_privileges(norm: str, is_exec: bool) -> str:
    if is_exec or "execute" in norm or re.search(r"\bexec\b", norm):
        return "EXECUTE"
    privs: List[str] = []
    if any(w in norm for w in ["select", "xem", "doc", "tra cuu"]):
        privs.append("SELECT")
    if any(w in norm for w in ["insert", "them", "chen"]):
        privs.append("INSERT")
    if any(w in norm for w in ["update", "sua", "cap nhat"]):
        privs.append("UPDATE")
    if any(w in norm for w in ["delete", "xoa"]):
        privs.append("DELETE")
    if not privs:
        return "SELECT, INSERT, UPDATE, DELETE"
    return ", ".join(privs)


def _clean_for_human_typing(sql: str) -> str:
    """Prepare current-database SQL, preserving quoted text and error messages."""
    return prepare_sql(sql, clean_comments=True)


_last_ai_error = ""
_active_sql_target = {}


def set_database_context(server: str, database: str) -> None:
    """Use the connection shown by the active SSMS query for metadata reads."""
    _active_sql_target.clear()
    _active_sql_target.update(server=server, database=database)


def _ai_unavailable_message() -> str:
    detail = _last_ai_error or "Kiểm tra API key, kết nối mạng và model Gemini."
    return "-- Không thể sinh SQL bằng Gemini. " + detail


def _prepare_ai_sql(sql: str, prompt: str, clean_comments: bool) -> str:
    """Do not accept schema bootstrap for a request to use existing objects."""
    norm = strip_accents(prompt)
    definition_requested = bool(re.search(
        r"\b(?:tao|create|sua|alter)\s+(?:(?:moi|lai|or alter)\s+)?"
        r"(?:dinh nghia|cau truc|doi tuong|bang\b|co so du lieu|database\b|table\b|view\b|ham\b|function\b|"
        r"thu tuc|procedure\b|proc\b|trigger\b|index\b|chi muc|login\b|user\b|tai khoan sql)"
        r"|\bdinh nghia lai\b", norm,
    ))
    mask = code_mask(sql)
    if not definition_requested and (
        re.search(
            r"\b(?:CREATE\s+(?:OR\s+ALTER\s+)?(?:(?:UNIQUE|CLUSTERED|NONCLUSTERED)\s+)*|ALTER\s+)"
            r"(?:DATABASE|TABLE|VIEW|FUNCTION|PROC(?:EDURE)?|TRIGGER|INDEX|LOGIN|USER)\b",
            mask, re.IGNORECASE,
        )
        or re.search(r"\bSELECT\b[\s\S]*?\bINTO\b", mask, re.IGNORECASE)
    ):
        return "-- Gemini trả về lệnh tạo/sửa cấu trúc ngoài yêu cầu. Hãy yêu cầu dùng đối tượng đã có trong database."
    return prepare_sql(materialize_payroll_exec_arguments(sql), clean_comments=clean_comments)


def _explicit_payroll_id(norm: str) -> Optional[int]:
    match = re.search(
        r"\b(?:mabangluong|ma\s*(?:bang\s*)?luong|(?:bang|ky)\s+luong|sp_(?:huy)?chotbangluong)"
        r"\s*(?:=|:|so|ma)?\s*(\d+)\b(?!\s*[/.-])", norm,
    )
    return int(match.group(1)) if match else None


def _explicit_period(norm: str) -> Tuple[Optional[int], Optional[int]]:
    pair = re.search(r"\b(?:thang|ky(?:\s+luong)?)\s*(\d{1,2})\s*/\s*(\d{4})\b", norm)
    if pair:
        return int(pair.group(1)), int(pair.group(2))
    month = re.search(r"\bthang\s+(\d{1,2})\b", norm)
    year = re.search(r"\bnam\s*(\d{4})\b", norm)
    return (int(month.group(1)) if month else None, int(year.group(1)) if year else None)


# ---------------------------------------------------------------------------
# Main Entry Point
# ---------------------------------------------------------------------------
def generate_sql(prompt_line: str, force_ai: bool = False, clean_comments: bool = False) -> str:
    raw = (prompt_line or "").strip()
    # Stealth AI trigger 1: starts with 3 dashes `---` or ends with `.` or `?`
    if raw.startswith("---") or raw.endswith(".") or raw.endswith("?"):
        force_ai = True

    # Strip leading SQL comment markers `--`, `---`, `//`, `/* */` and optional `#`
    cleaned_raw = re.sub(r"^(--+|//|/\*+|\*+)\s*", "", raw).strip()
    cleaned_raw = re.sub(r"^#+\s*", "", cleaned_raw).strip()
    cleaned_raw = cleaned_raw.rstrip(".?").strip()
    norm = strip_accents(cleaned_raw)

    if not norm or norm in ("help", "menu", "tro giup", "huong dan"):
        return prepare_sql(_sql_help_menu())

    # Force AI mode if triggered via stealth syntax (`---`, trailing `.`, `Ctrl+Q`) or legacy `ai `
    if force_ai or norm.startswith("ai "):
        prompt_text = cleaned_raw[3:].strip() if norm.startswith("ai ") else cleaned_raw
        ai_sql = _call_gemini_fallback(prompt_text)
        if ai_sql:
            return _prepare_ai_sql(ai_sql, prompt_text, clean_comments)
        return _ai_unavailable_message()

    res = _generate_offline_or_fallback(cleaned_raw, norm)
    return prepare_sql(res, clean_comments=clean_comments)


def _generate_offline_or_fallback(cleaned_raw: str, norm: str) -> str:

    # Calling an installed payroll procedure is different from viewing its module.
    if ("sp_tinhbangluongthang" in norm or "tinh bang luong" in norm) and re.search(
        r"\b(chay|exec(?:ute)?|tinh(?: lai)?)\b", norm,
    ) and not any(word in norm for word in ("xem code", "dinh nghia", "sua", "tao")):
        month, year = _explicit_period(norm)
        return _sql_run_existing_payroll(_explicit_payroll_id(norm), month, year)

    # 1. Rescue / Reset
    if any(w in norm for w in ["reset", "cuu ho", "khoi phuc trang thai", "ve ban dau"]):
        return _sql_reset_state()

    # 2. sp_helptext / View source code of any object
    if any(w in norm for w in ["helptext", "xem code", "in code", "code cua", "dinh nghia cua"]):
        m = re.search(r"(sp_[a-z0-9_]+|trg_[a-z0-9_]+|vw_[a-z0-9_]+|fn_[a-z0-9_]+)", norm)
        obj = f"dbo.{m.group(1)}" if m else _extract_object(norm)[0]
        return (
            f"exec sys.sp_helptext N'{obj}';"
        )

    # 3. Create Login + User + Assign Role
    if any(w in norm for w in ["tao login", "create login", "tao user", "tao tai khoan sql"]):
        m = re.search(r"(?:login|user|tai khoan)\s+([a-zA-Z0-9_]+)", norm)
        login_id = m.group(1) if m and m.group(1) not in ("sql", "moi", "cho", "va") else "test_gv"
        role_name, _ = _extract_role(norm)
        return (
            f"-- 1. Tạo SQL Login ở mức Server + Tạo User trong DB + Gán vào {role_name}:\n"
            f"CREATE LOGIN [{login_id}] WITH PASSWORD = N'Test@123456', CHECK_POLICY = ON;\n"
            f"CREATE USER [{login_id}] FOR LOGIN [{login_id}];\n"
            f"ALTER ROLE {role_name} ADD MEMBER [{login_id}];\n"
            f"-- 2. (Tùy chọn) Lệnh gỡ khỏi Role và xóa sau khi demo xong:\n"
            f"/*\n"
            f"ALTER ROLE {role_name} DROP MEMBER [{login_id}];\n"
            f"DROP USER [{login_id}];\n"
            f"DROP LOGIN [{login_id}];\n"
            f"*/"
        )

    # 4. Add / Remove Member to/from Role
    if any(w in norm for w in ["them user", "gan role", "add member", "dua vao role", "go khoi role", "xoa khoi role", "drop member"]):
        role_name, login_name = _extract_role(norm)
        m = re.search(r"\b(gui_[a-z0-9_]+|test_[a-z0-9_]+)\b", norm)
        user_target = m.group(1) if m else login_name
        action = "DROP MEMBER" if any(w in norm for w in ["go khoi", "xoa khoi", "drop"]) else "ADD MEMBER"
        return (
            f"ALTER ROLE {role_name} {action} [{user_target}];"
        )

    # 5. Dynamic GRANT / REVOKE / DENY
    if any(
        w in norm
        for w in [
            "cap quyen",
            "phan quyen",
            "cho phep",
            "grant ",
            "thu hoi quyen",
            "go quyen",
            "xoa quyen",
            "revoke ",
            "cam quyen",
            "chan quyen",
            "deny ",
        ]
    ) or re.match(r"^(grant|revoke|deny|cap|cam|thu hoi)\b", norm):
        return _sql_dynamic_permission(norm)

    # 6. View Permission Matrix of Roles
    if any(w in norm for w in ["xem quyen", "ma tran quyen", "danh sach quyen", "database_permissions"]):
        role_name, _ = _extract_role(norm)
        return (
            f"SELECT pr.name AS TenRole, dp.state_desc AS TrangThai,\n"
            f"       dp.permission_name AS Quyen, OBJECT_NAME(dp.major_id) AS DoiTuong\n"
            f"FROM sys.database_permissions dp\n"
            f"JOIN sys.database_principals pr ON dp.grantee_principal_id = pr.principal_id\n"
            f"WHERE pr.name LIKE 'role_%' -- Hoặc lọc riêng: pr.name = '{role_name}'\n"
            f"ORDER BY pr.name, dp.state_desc, DoiTuong;"
        )

    # 7. Section 1: RBAC Test Suites (HR, Payroll, Employee)
    if any(w in norm for w in ["test quyen", "test rbac", "rbac", "1.1", "1.2", "1.3", "phan 1"]):
        if any(w in norm for w in ["ke toan", "payroll", "gui_payroll", "1.2"]):
            return _sql_test_rbac_payroll()
        if any(w in norm for w in ["nhan vien", "employee", "gui_a", "1.3"]):
            return _sql_test_rbac_employee()
        return _sql_test_rbac_hr()

    # 8. Section 2: Row-Level Security (vw_PhieuLuongCaNhan)
    if any(w in norm for w in ["rls", "phieu luong ca nhan", "vw_phieuluongcanhan", "sua view", "phan 2", "2.1", "2.2", "original_login"]):
        if any(w in norm for w in ["sua view", "alter view", "create or alter view"]):
            return _sql_alter_rls_view(only_da_chot=("da_chot" in norm or "da chot" in norm))
        return _sql_test_rls()

    # 9. Section 3: Payroll Closing & Reopening (sp_ChotBangLuong / sp_HuyChotBangLuong)
    if any(w in norm for w in ["mo lai", "huy chot", "bo chot", "mo khoa bang luong", "mo khoa ky luong", "sp_huychotbangluong", "3.4"]):
        month, year = _explicit_period(norm)
        return _sql_reopen_bang_luong(_explicit_payroll_id(norm), month, year)
    if any(w in norm for w in ["chot bang luong", "chot luong", "sp_chotbangluong", "phan 3", "3.1", "3.2", "3.3"]):
        month, year = _explicit_period(norm)
        return _sql_test_chot_bang_luong(_explicit_payroll_id(norm), month, year)

    # 10. Section 4: Triggers (5 TV5 Triggers + Enable/Disable)
    if "trigger" in norm or "phan 4" in norm or any(w in norm for w in ["4.1", "4.2", "4.3", "4.4", "4.5", "khoa so"]):
        if any(w in norm for w in ["tat trigger", "disable trigger", "bat trigger", "enable trigger", "kich hoat trigger"]):
            return _sql_toggle_trigger(norm)
        if any(w in norm for w in ["cham cong", "chamcong", "4.1"]):
            return _sql_trigger_chamcong()
        if any(w in norm for w in ["phu cap", "phucap", "4.2"]):
            return _sql_trigger_phucap()
        if any(w in norm for w in ["khau tru", "khautru", "4.3"]):
            return _sql_trigger_khautru()
        if any(w in norm for w in ["chi tiet", "chitiet", "100 trieu", "4.4"]):
            return _sql_trigger_chitiet()
        if any(w in norm for w in ["bang luong", "bangluong"]):
            return _sql_trigger_bangluong()
        if any(w in norm for w in ["xoa nhan vien", "delete nhan vien", "nhanvien", "4.5"]):
            return _sql_trigger_xoa_nv()
        return _sql_all_5_triggers()

    # 11. Section 5: Function fn_TinhThucNhan & Overflow 8115
    if any(w in norm for w in ["fn_tinhtiencong", "ham tinh tien cong"]):
        return _sql_tv4_demo()
    if any(w in norm for w in ["fn_tinhsongaycong", "ham tinh so ngay cong"]):
        return _sql_tv1_demo()
    if any(w in norm for w in ["tran so", "8115", "overflow", "bien tien te", "decimal"]):
        return _sql_test_money_overflow()
    if any(w in norm for w in ["fn_tinhthucnhan", "tinh thuc nhan", "ham thuc nhan", "test thuc nhan", "sua ham", "phan 5"]):
        if any(w in norm for w in ["sua ham", "alter function", "create or alter function"]):
            return _sql_alter_fn_thuc_nhan_clamp_zero()
        return _sql_test_fn_thuc_nhan()

    # 12. Section 6: Covering Index & 10,000-row Benchmark
    if any(w in norm for w in ["10000", "10.000", "10k", "bang tam", "benchmarknhanvien"]):
        return _sql_benchmark_10k()
    if any(w in norm for w in ["index", "covering", "statistics io", "benchmark", "phan 6", "ix_nhanvien"]):
        return _sql_benchmark_index()

    # 13. Section 7: Concurrency Locking & Blocking Inspection
    if any(w in norm for w in ["block", "spid", "dm_exec_requests", "dm_tran_locks", "ai dang khoa", "session nao"]):
        return _sql_check_blocking_sessions()
    if any(w in norm for w in ["concurrency", "dong thoi", "updlock", "holdlock", "cua so 1", "cua so 2", "phan 7"]):
        if "cua so 2" in norm or "phien b" in norm:
            return _sql_concurrency_window2()
        return _sql_concurrency_window1()

    # 14. Section 8: Constraints & Metadata
    if any(w in norm for w in ["metadata", "sys.objects", "danh muc rang buoc", "8.9"]):
        return _sql_constraints_metadata()
    if any(w in norm for w in ["constraint", "rang buoc", "check", "unique", "foreign key", "khoa ngoai", "thang 13", "phan 8"]):
        return _sql_test_constraints(norm)

    # 15. TV5 Admin Stored Procedures
    if any(w in norm for w in ["admin", "khoa tai khoan", "mo khoa", "doi vai tro", "reset mat khau", "laytaikhoanhientai"]):
        return _sql_test_admin_sps()

    # 16. Analytical Queries (GROUP BY, TOP 1 WITH TIES, LEFT JOIN, LICHSULUONG)
    has_custom_top_n = bool(
        re.search(r"\b([2-9]|\d{2,})\s*(nhan vien|nguoi|dong|phong)\b", norm)
        or re.search(r"\btop\s*\(?\s*([2-9]|\d{2,})\b", norm)
    )
    if has_custom_top_n and any(w in norm for w in ["luong", "thuc nhan"]):
        match = re.search(r"\btop\s*\(?\s*(\d+)\b|\b(\d+)\s*(?:nhan vien|nguoi|dong)\b", norm)
        if match:
            order = "ASC" if any(w in norm for w in ["thap nhat", "it nhat"]) else "DESC"
            month, year = _explicit_period(norm)
            return _sql_query_top_luong(
                order, top_n=max(1, int(match.group(1) or match.group(2))), month=month, year=year,
            )
    if not has_custom_top_n:
        if any(w in norm for w in ["thong ke", "group by", "having", "quy luong", "theo phong ban"]):
            month, year = _explicit_period(norm)
            return _sql_query_group_by_phongban(month=month, year=year)
        if any(w in norm for w in ["top", "cao nhat", "thap nhat", "nhieu nhat", "it nhat"]):
            order = "ASC" if any(w in norm for w in ["thap nhat", "it nhat"]) else "DESC"
            month, year = _explicit_period(norm)
            return _sql_query_top_luong(order, month=month, year=year)
        if any(w in norm for w in ["chua co tai khoan", "chua cap tai khoan", "chua cham cong", "is null", "not exists"]):
            return _sql_query_unassigned()
        if any(w in norm for w in ["lich su luong", "tang luong", "lichsuluong", "snapshot"]):
            return _sql_query_lich_su_luong()

    # 17. Cross-module quick helpers (TV1, TV2, TV3, TV4)
    if any(w in norm for w in ["tv1", "sp_themnhanvien", "them nhan vien"]):
        return _sql_tv1_demo()
    if any(w in norm for w in ["tv2", "sp_ghinhanchamcong", "ghi nhan cham cong"]):
        return _sql_tv2_demo()
    if any(w in norm for w in ["tv3", "sp_xoakyluongchuachot", "sp_themphucapnhanvien", "fn_tongphucap", "fn_tongkhautru"]):
        return _sql_tv3_demo()
    if any(w in norm for w in ["tv4", "sp_tinhbangluongthang", "fn_tinhtiencong", "tinh bang luong"]):
        return _sql_tv4_demo()

    # 18. Optional Gemini API fallback if GEMINI_API_KEY is available in environment
    ai_sql = _call_gemini_fallback(cleaned_raw)
    if ai_sql:
        return _prepare_ai_sql(ai_sql, cleaned_raw, clean_comments=False)

    # An unmatched request must not execute an unrelated payroll query.
    return (
        "-- Chưa có mẫu SQL phù hợp với yêu cầu này.\n"
        + _ai_unavailable_message() + "\n"
        + "-- Gõ -- help rồi bấm F8 để xem các yêu cầu hỗ trợ."
    )


# ---------------------------------------------------------------------------
# Template Builders
# ---------------------------------------------------------------------------
def _sql_dynamic_permission(norm: str) -> str:
    role_name, _ = _extract_role(norm)
    obj_name, is_exec = _extract_object(norm)
    privs = _extract_privileges(norm, is_exec)
    if any(w in norm for w in ["cam", "chan", "deny", "khong cho"]):
        return f"DENY {privs} ON OBJECT::{obj_name} TO [{role_name}];"
    if any(w in norm for w in ["thu hoi", "go quyen", "xoa quyen", "revoke"]):
        return f"REVOKE {privs} ON OBJECT::{obj_name} FROM [{role_name}];"
    return f"GRANT {privs} ON OBJECT::{obj_name} TO [{role_name}];"


def _sql_existing_period(status: Optional[str] = None, payroll_id: Optional[int] = None,
                         require_details: bool = False, month: Optional[int] = None,
                         year: Optional[int] = None, include_parameters: bool = False) -> str:
    """Resolve a payroll period from rows already present in the current database."""
    filters = []
    if payroll_id is not None:
        filters.append(f"bl.MaBangLuong = {int(payroll_id)}")
    elif status:
        filters.append(f"bl.TrangThai = '{status}'")
    if month is not None:
        filters.append(f"bl.Thang = {int(month)}")
    if year is not None:
        filters.append(f"bl.Nam = {int(year)}")
    if require_details:
        filters.append("EXISTS (SELECT 1 FROM dbo.CHITIETBANGLUONG ct WHERE ct.MaBangLuong = bl.MaBangLuong)")
    where = "\nWHERE " + "\n  AND ".join(filters) if filters else ""
    declaration = "DECLARE @MaBangLuong INT;\n"
    selection = "SELECT TOP (1) @MaBangLuong = bl.MaBangLuong\n"
    if include_parameters:
        declaration = "DECLARE @MaBangLuong INT, @Thang INT, @Nam INT, @NgayCongChuan INT;\n"
        selection = (
            "SELECT TOP (1) @MaBangLuong = bl.MaBangLuong, @Thang = bl.Thang,\n"
            "       @Nam = bl.Nam, @NgayCongChuan = bl.NgayCongChuan\n"
        )
    return (
        declaration + selection
        + f"FROM dbo.BANGLUONG bl{where}\n"
        "ORDER BY bl.Nam DESC, bl.Thang DESC, bl.MaBangLuong DESC;\n"
        "IF @MaBangLuong IS NULL\n"
        "    THROW 51000, N'Không có kỳ lương hiện có phù hợp với yêu cầu.', 1;\n"
    )


def _sql_role_permissions(role: str, checks: List[Tuple[str, str]]) -> str:
    """Inspect effective permissions using an existing database role member."""
    from sql_format import lowercase_sql_keywords

    probes = "\nUNION ALL\n".join(
        f"SELECT N'{obj}' AS DoiTuong, N'{perm}' AS Quyen, "
        f"HAS_PERMS_BY_NAME(N'{obj}', N'OBJECT', N'{perm}') AS DuocPhep"
        for obj, perm in checks
    ) + ";"
    # SQL identifiers from the database are quoted before constructing EXECUTE AS.
    body = (
        "DECLARE @DangGiaLap BIT = 0;\nBEGIN TRY\n"
        "    EXECUTE AS USER = "
    )
    tail = (
        ";\n    SET @DangGiaLap = 1;\n"
        "    SELECT USER_NAME() AS NguoiDungDangKiemTra;\n"
        f"    {probes}\n"
        "    REVERT;\n    SET @DangGiaLap = 0;\nEND TRY\n"
        "BEGIN CATCH\n    IF @DangGiaLap = 1 REVERT;\n    THROW;\nEND CATCH;"
    )
    body = lowercase_sql_keywords(body)
    tail = lowercase_sql_keywords(tail)
    return (
        "DECLARE @NguoiDung SYSNAME, @Lenh NVARCHAR(MAX);\n"
        "SELECT TOP (1) @NguoiDung = m.name\n"
        "FROM sys.database_role_members rm\n"
        "JOIN sys.database_principals r ON r.principal_id = rm.role_principal_id\n"
        "JOIN sys.database_principals m ON m.principal_id = rm.member_principal_id\n"
        f"WHERE r.name = N'{role}' AND m.type IN ('S', 'U', 'E')\n"
        "ORDER BY m.name;\n"
        "IF @NguoiDung IS NULL\n"
        "    THROW 51001, N'Role chưa có người dùng hiện có để kiểm tra quyền.', 1;\n"
        f"SET @Lenh = N'{body.replace(chr(39), chr(39) * 2)}'\n"
        "    + QUOTENAME(@NguoiDung, CHAR(39))\n"
        f"    + N'{tail.replace(chr(39), chr(39) * 2)}';\n"
        "EXEC sys.sp_executesql @Lenh;"
    )


def _sql_test_rbac_hr() -> str:
    return _sql_role_permissions("role_HRManager", [
        ("dbo.NHANVIEN", "SELECT"), ("dbo.CHAMCONG", "SELECT"),
        ("dbo.sp_ChotBangLuong", "EXECUTE"), ("dbo.TAIKHOAN", "SELECT"),
    ])


def _sql_test_rbac_payroll() -> str:
    return _sql_role_permissions("role_PayrollOfficer", [
        ("dbo.BANGLUONG", "SELECT"), ("dbo.NHANVIEN", "UPDATE"),
        ("dbo.CHAMCONG", "INSERT"), ("dbo.sp_ChotBangLuong", "EXECUTE"),
    ])


def _sql_test_rbac_employee() -> str:
    return _sql_role_permissions("role_Employee", [
        ("dbo.BANGLUONG", "SELECT"), ("dbo.vw_BangLuongChiTiet", "SELECT"),
        ("dbo.vw_PhieuLuongCaNhan", "SELECT"),
    ])


def _sql_test_rls() -> str:
    return (
        "SELECT MaNV, HoTen, Thang, Nam, ThucNhan\n"
        "FROM dbo.vw_PhieuLuongCaNhan\n"
        "ORDER BY Nam DESC, Thang DESC;"
    )


def _sql_alter_rls_view(only_da_chot: bool = False) -> str:
    extra = "\n  AND bl.TrangThaiBangLuong = 'DA_CHOT'" if only_da_chot else ""
    return (
        "GO\n"
        "ALTER VIEW dbo.vw_PhieuLuongCaNhan\nAS\n"
        "SELECT bl.*\n"
        "FROM dbo.vw_BangLuongChiTiet bl\n"
        "JOIN dbo.TAIKHOAN tk ON tk.MaNV = bl.MaNV\n"
        "WHERE tk.SqlLogin IN (ORIGINAL_LOGIN(), SUSER_SNAME())\n"
        f"  AND tk.TrangThai = 'HOAT_DONG'{extra};\nGO"
    )


def _sql_test_chot_bang_luong(payroll_id: Optional[int] = None,
                             month: Optional[int] = None, year: Optional[int] = None) -> str:
    return (
        _sql_existing_period("CHUA_CHOT", payroll_id, require_details=True, month=month, year=year)
        + "EXEC dbo.sp_ChotBangLuong @MaBangLuong = @MaBangLuong;\n"
        "SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot\n"
        "FROM dbo.BANGLUONG\nWHERE MaBangLuong = @MaBangLuong;"
    )


def _sql_reopen_bang_luong(payroll_id: Optional[int] = None,
                          month: Optional[int] = None, year: Optional[int] = None) -> str:
    return (
        _sql_existing_period("DA_CHOT", payroll_id, month=month, year=year)
        + "EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = @MaBangLuong;\n"
        "SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot\n"
        "FROM dbo.BANGLUONG\nWHERE MaBangLuong = @MaBangLuong;"
    )


def _sql_existing_trigger_test(table: str, primary_key: str, trigger: str,
                               source: str, assignment: Optional[str],
                               suffix: str, expected_message: str) -> str:
    """Exercise an installed trigger using existing rows; never prepare fixtures."""
    variable = f"@MaKiemTra_{suffix}"
    operation = (
        f"UPDATE dbo.{table} SET {assignment} WHERE {primary_key} = {variable};"
        if assignment else f"DELETE FROM dbo.{table} WHERE {primary_key} = {variable};"
    )
    return (
        "IF @@TRANCOUNT <> 0\n"
        "    THROW 51002, N'Hãy hoàn tất transaction hiện tại trước khi chạy kiểm tra trigger.', 1;\n"
        "IF NOT EXISTS (SELECT 1 FROM sys.triggers\n"
        f"               WHERE object_id = OBJECT_ID(N'dbo.{trigger}') AND is_disabled = 0)\n"
        "    THROW 51003, N'Trigger cần kiểm tra chưa tồn tại hoặc đang tắt.', 1;\n"
        f"DECLARE {variable} INT;\n"
        f"SELECT TOP (1) {variable} = t.{primary_key}\n{source}\n"
        f"ORDER BY t.{primary_key};\n"
        f"IF {variable} IS NULL\n"
        "    THROW 51004, N'Không có dòng hiện có phù hợp để kiểm tra trigger; không tự tạo dữ liệu.', 1;\n"
        "BEGIN TRY\n"
        "    BEGIN TRANSACTION;\n"
        f"    {operation}\n"
        "    ROLLBACK TRANSACTION;\n"
        "    THROW 51005, N'Thao tác không bị trigger chặn; mọi thay đổi đã rollback.', 1;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;\n"
        f"    IF ERROR_NUMBER() <> 50000 OR ERROR_MESSAGE() NOT LIKE N'%{expected_message}%' THROW;\n"
        f"    SELECT N'{trigger}' AS TriggerDaChan, ERROR_NUMBER() AS MaLoi,\n"
        "           ERROR_MESSAGE() AS ThongBao;\n"
        "END CATCH;"
    )


def _sql_trigger_chamcong() -> str:
    return _sql_existing_trigger_test(
        "CHAMCONG", "MaChamCong", "trg_ChamCong_KhongSuaKhiDaChotLuong",
        "FROM dbo.CHAMCONG t\nJOIN dbo.BANGLUONG bl ON bl.Thang = MONTH(t.NgayChamCong)\n"
        "    AND bl.Nam = YEAR(t.NgayChamCong)\nWHERE bl.TrangThai = 'DA_CHOT'",
        "GhiChu = GhiChu", "ChamCong", "không được thay đổi chấm công",
    )


def _sql_trigger_phucap() -> str:
    return _sql_existing_trigger_test(
        "PHUCAPNHANVIEN", "MaPCNV", "trg_PhuCap_KhongSuaKhiDaChotLuong",
        "FROM dbo.PHUCAPNHANVIEN t\nJOIN dbo.BANGLUONG bl ON bl.Thang = t.Thang AND bl.Nam = t.Nam\n"
        "WHERE bl.TrangThai = 'DA_CHOT'",
        "SoTien = SoTien", "PhuCap", "không được thay đổi phụ cấp",
    )


def _sql_trigger_khautru() -> str:
    return _sql_existing_trigger_test(
        "KHAUTRUNHANVIEN", "MaKTNV", "trg_KhauTru_KhongSuaKhiDaChotLuong",
        "FROM dbo.KHAUTRUNHANVIEN t\nJOIN dbo.BANGLUONG bl ON bl.Thang = t.Thang AND bl.Nam = t.Nam\n"
        "WHERE bl.TrangThai = 'DA_CHOT'",
        "SoTien = SoTien", "KhauTru", "không được thay đổi khấu trừ",
    )


def _sql_trigger_chitiet() -> str:
    return _sql_existing_trigger_test(
        "CHITIETBANGLUONG", "MaChiTiet", "trg_ChiTietLuong_KhongSuaKhiDaChot",
        "FROM dbo.CHITIETBANGLUONG t\nJOIN dbo.BANGLUONG bl ON bl.MaBangLuong = t.MaBangLuong\n"
        "WHERE bl.TrangThai = 'DA_CHOT'",
        "ThucNhan = ThucNhan", "ChiTiet", "chi tiết bảng lương đã chốt",
    )


def _sql_trigger_bangluong() -> str:
    return _sql_existing_trigger_test(
        "BANGLUONG", "MaBangLuong", "trg_BangLuong_KhongSuaKhiDaChot",
        "FROM dbo.BANGLUONG t\nWHERE t.TrangThai = 'DA_CHOT'",
        "NgayCongChuan = NgayCongChuan", "BangLuong", "Khong duoc sua hoac xoa bang luong da chot",
    )


def _sql_trigger_xoa_nv() -> str:
    return _sql_existing_trigger_test(
        "NHANVIEN", "MaNV", "trg_NhanVien_KhongXoaKhiDaPhatSinhLuong",
        "FROM dbo.NHANVIEN t\nWHERE EXISTS (SELECT 1 FROM dbo.CHITIETBANGLUONG ct WHERE ct.MaNV = t.MaNV)\n"
        "   OR EXISTS (SELECT 1 FROM dbo.CHAMCONG cc WHERE cc.MaNV = t.MaNV)",
        None, "NhanVien", "Không được phép xóa nhân viên",
    )


def _sql_all_5_triggers() -> str:
    return "\n\n".join(builder() for builder in (
        _sql_trigger_chamcong, _sql_trigger_phucap, _sql_trigger_khautru,
        _sql_trigger_chitiet, _sql_trigger_xoa_nv,
    ))


def _sql_toggle_trigger(norm: str) -> str:
    trig, tbl = "dbo.trg_ChamCong_KhongSuaKhiDaChotLuong", "dbo.CHAMCONG"
    if "phu cap" in norm or "phucap" in norm:
        trig, tbl = "dbo.trg_PhuCap_KhongSuaKhiDaChotLuong", "dbo.PHUCAPNHANVIEN"
    elif "khau tru" in norm or "khautru" in norm:
        trig, tbl = "dbo.trg_KhauTru_KhongSuaKhiDaChotLuong", "dbo.KHAUTRUNHANVIEN"
    elif "chi tiet" in norm or "chitiet" in norm:
        trig, tbl = "dbo.trg_ChiTietLuong_KhongSuaKhiDaChot", "dbo.CHITIETBANGLUONG"
    elif "nhan vien" in norm or "nhanvien" in norm:
        trig, tbl = "dbo.trg_NhanVien_KhongXoaKhiDaPhatSinhLuong", "dbo.NHANVIEN"
    elif "bang luong" in norm or "bangluong" in norm:
        trig, tbl = "dbo.trg_BangLuong_KhongSuaKhiDaChot", "dbo.BANGLUONG"
    action = "DISABLE" if "tat trigger" in norm or "disable trigger" in norm else "ENABLE"
    return f"{action} TRIGGER {trig} ON {tbl};"


def _sql_test_fn_thuc_nhan() -> str:
    return (
        "SELECT MaNV, HoTen, ThucNhan AS ThucNhanDaLuu,\n"
        "       dbo.fn_TinhThucNhan(TienCong, TongPhuCap, TongKhauTru) AS ThucNhanTinhLai\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        + _sql_read_payroll_filter()
        + "\nORDER BY MaNV;"
    )


def _sql_alter_fn_thuc_nhan_clamp_zero() -> str:
    return (
        "GO\n"
        "ALTER FUNCTION dbo.fn_TinhThucNhan(\n"
        "    @TienCong DECIMAL(18,2),\n"
        "    @TongPhuCap DECIMAL(18,2),\n"
        "    @TongKhauTru DECIMAL(18,2)\n)\n"
        "RETURNS DECIMAL(18,2)\nAS\nBEGIN\n"
        "    DECLARE @KetQua DECIMAL(20,2) = ISNULL(@TienCong, 0)\n"
        "        + ISNULL(@TongPhuCap, 0) - ISNULL(@TongKhauTru, 0);\n"
        "    RETURN CASE WHEN @KetQua < 0 THEN 0 ELSE CAST(@KetQua AS DECIMAL(18,2)) END;\n"
        "END;\nGO\n"
        + _sql_test_fn_thuc_nhan()
    )


def _sql_test_money_overflow() -> str:
    return (
        "SELECT dbo.fn_TinhThucNhan(9999999999999999.99, 0.00, 0.00) AS Max_Decimal_18_2;\n"
        "BEGIN TRY\n"
        "    SELECT dbo.fn_TinhThucNhan(9999999999999999.99, 1.00, 0.00) AS TranSo;\n"
        "END TRY\nBEGIN CATCH\n"
        "    IF ERROR_NUMBER() <> 8115 THROW;\n"
        "    SELECT ERROR_NUMBER() AS MaLoi, ERROR_MESSAGE() AS ThongBao;\n"
        "END CATCH;"
    )


def _sql_benchmark_existing(row_limit: int) -> str:
    return (
        "SET STATISTICS IO ON;\nSET STATISTICS TIME ON;\n"
        f"SELECT TOP ({int(row_limit)}) MaNV, HoTen, LuongCoBan, TrangThai\n"
        "FROM dbo.NHANVIEN\n"
        "WHERE MaPB = (SELECT TOP (1) MaPB FROM dbo.NHANVIEN ORDER BY MaNV)\n"
        "ORDER BY MaNV;\n"
        "SET STATISTICS IO OFF;\nSET STATISTICS TIME OFF;"
    )


def _sql_benchmark_index() -> str:
    return _sql_benchmark_existing(100)


def _sql_benchmark_10k() -> str:
    return _sql_benchmark_existing(10000)


def _sql_concurrency_window1() -> str:
    return (
        "IF @@TRANCOUNT <> 0\n"
        "    THROW 51007, N'Phiên A phải bắt đầu ngoài transaction hiện có.', 1;\n"
        "DECLARE @MaBangLuong INT;\n"
        "BEGIN TRY\n    BEGIN TRANSACTION;\n"
        "    SELECT TOP (1) @MaBangLuong = MaBangLuong\n"
        "    FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)\n"
        "    ORDER BY Nam DESC, Thang DESC, MaBangLuong DESC;\n"
        "    IF @MaBangLuong IS NULL THROW 51000, N'Chưa có kỳ lương để kiểm tra khóa.', 1;\n"
        "    SELECT @@SPID AS PhienAGiuKhoa, @MaBangLuong AS MaBangLuongDangKhoa;\n"
        "    WAITFOR DELAY '00:00:20';\n"
        "    ROLLBACK TRANSACTION;\n"
        "END TRY\nBEGIN CATCH\n"
        "    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;\n    THROW;\nEND CATCH;"
    )


def _sql_concurrency_window2() -> str:
    return (
        "IF @@TRANCOUNT <> 0\n"
        "    THROW 51008, N'Phiên B phải bắt đầu ngoài transaction hiện có.', 1;\n"
        "BEGIN TRY\n    BEGIN TRANSACTION;\n"
        "    SELECT TOP (1) @@SPID AS PhienB, MaBangLuong, Thang, Nam, TrangThai\n"
        "    FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)\n"
        "    ORDER BY Nam DESC, Thang DESC, MaBangLuong DESC;\n"
        "    ROLLBACK TRANSACTION;\n"
        "END TRY\nBEGIN CATCH\n"
        "    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;\n    THROW;\nEND CATCH;"
    )


def _sql_check_blocking_sessions() -> str:
    return (
        "SELECT session_id AS PhienBiChan, blocking_session_id AS PhienDangGiuKhoa,\n"
        "       wait_type, wait_time, status, command\n"
        "FROM sys.dm_exec_requests\nWHERE blocking_session_id > 0 AND database_id = DB_ID();\n"
        "SELECT request_session_id AS Spid, resource_type,\n"
        "       CASE WHEN resource_type = 'OBJECT'\n"
        "            THEN OBJECT_NAME(resource_associated_entity_id, resource_database_id) END AS TenBang,\n"
        "       request_mode AS LoaiKhoa, request_status AS TrangThai\n"
        "FROM sys.dm_tran_locks\nWHERE resource_database_id = DB_ID()\n"
        "  AND resource_type IN ('OBJECT', 'KEY', 'PAGE', 'RID');"
    )


def _sql_constraints_metadata() -> str:
    return (
        "SELECT t.name AS Bang, c.name AS TenRangBuoc, c.type_desc AS LoaiRangBuoc,\n"
        "       OBJECT_DEFINITION(c.object_id) AS DinhNghiaChiTiet\n"
        "FROM sys.objects c\nJOIN sys.tables t ON t.object_id = c.parent_object_id\n"
        "WHERE c.type_desc LIKE '%CONSTRAINT'\nORDER BY t.name, c.type_desc, c.name;"
    )


def _sql_test_constraints(norm: str) -> str:
    if "unique" in norm or "trung" in norm:
        target = (
            "DECLARE @MaKiemTra INT, @ThangTrung INT, @NamTrung INT;\n"
            "SELECT TOP (1) @MaKiemTra = a.MaBangLuong, @ThangTrung = b.Thang, @NamTrung = b.Nam\n"
            "FROM dbo.BANGLUONG a\nJOIN dbo.BANGLUONG b ON b.MaBangLuong <> a.MaBangLuong\n"
            "WHERE a.TrangThai = 'CHUA_CHOT'\nORDER BY a.Nam DESC, a.Thang DESC, b.MaBangLuong;\n"
        )
        operation = "UPDATE dbo.BANGLUONG SET Thang = @ThangTrung, Nam = @NamTrung WHERE MaBangLuong = @MaKiemTra;"
        error_check = "ERROR_NUMBER() NOT IN (2601, 2627)"
        constraint = "UQ_BANGLUONG_ThangNam"
    elif "vai tro" in norm or "taikhoan" in norm or "tai khoan" in norm:
        target = "DECLARE @MaKiemTra INT = (SELECT TOP (1) MaTK FROM dbo.TAIKHOAN ORDER BY MaTK);\n"
        operation = "UPDATE dbo.TAIKHOAN SET VaiTro = 'INVALID_ROLE' WHERE MaTK = @MaKiemTra;"
        error_check = "ERROR_NUMBER() <> 547"
        constraint = "CHK_TAIKHOAN_VaiTro"
    else:
        target = (
            "DECLARE @MaKiemTra INT = (SELECT TOP (1) MaBangLuong FROM dbo.BANGLUONG\n"
            "    WHERE TrangThai = 'CHUA_CHOT' ORDER BY Nam DESC, Thang DESC);\n"
        )
        operation = "UPDATE dbo.BANGLUONG SET Thang = 13 WHERE MaBangLuong = @MaKiemTra;"
        error_check = "ERROR_NUMBER() <> 547"
        constraint = "CHK_BANGLUONG_Thang"
    return (
        "IF @@TRANCOUNT <> 0\n"
        "    THROW 51009, N'Hãy hoàn tất transaction hiện tại trước khi kiểm tra ràng buộc.', 1;\n"
        + target
        + "IF @MaKiemTra IS NULL\n"
        "    THROW 51010, N'Không có dòng hiện có phù hợp để kiểm tra ràng buộc.', 1;\n"
        "BEGIN TRY\n    BEGIN TRANSACTION;\n"
        f"    {operation}\n"
        "    ROLLBACK TRANSACTION;\n"
        "    THROW 51011, N'Ràng buộc không chặn thao tác; thay đổi đã rollback.', 1;\n"
        "END TRY\nBEGIN CATCH\n"
        "    IF XACT_STATE() <> 0 ROLLBACK TRANSACTION;\n"
        f"    IF {error_check} OR ERROR_MESSAGE() NOT LIKE N'%{constraint}%' THROW;\n"
        f"    SELECT N'{constraint}' AS RangBuocDaChan, ERROR_NUMBER() AS MaLoi, ERROR_MESSAGE() AS ThongBao;\n"
        "END CATCH;"
    )


def _sql_test_admin_sps() -> str:
    return (
        "EXEC dbo.sp_LayTaiKhoanHienTai;\n"
        "SELECT MaTK, TenDangNhap, VaiTro, TrangThai\n"
        "FROM dbo.TAIKHOAN\nORDER BY MaTK;"
    )


def _sql_read_payroll_filter(month: Optional[int] = None, year: Optional[int] = None) -> str:
    """Use plain filters for a specified period, otherwise select the latest with data."""
    filters = []
    if month is not None:
        filters.append(f"Thang = {int(month)}")
    if year is not None:
        filters.append(f"Nam = {int(year)}")
    if month is not None and year is not None:
        return "WHERE " + " AND ".join(filters)
    where = "    WHERE " + " AND ".join(filters) + "\n" if filters else ""
    return (
        "WHERE MaBangLuong = (\n"
        "    SELECT TOP (1) MaBangLuong\n"
        "    FROM dbo.vw_BangLuongChiTiet\n"
        + where
        + "    ORDER BY Nam DESC, Thang DESC\n)"
    )


def _sql_query_group_by_phongban(month: Optional[int] = None, year: Optional[int] = None) -> str:
    return (
        "SELECT TenPB, COUNT(*) AS SoNhanVien, SUM(ThucNhan) AS TongQuyLuongThucNhan\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        + _sql_read_payroll_filter(month, year)
        + "\nGROUP BY TenPB\nORDER BY TongQuyLuongThucNhan DESC;"
    )


def _sql_query_top_luong(order: str = "DESC", top_n: int = 1,
                         month: Optional[int] = None, year: Optional[int] = None) -> str:
    order = "ASC" if order.upper() == "ASC" else "DESC"
    return (
        f"SELECT TOP ({max(1, int(top_n))}) WITH TIES MaNV, HoTen, TenPB, ThucNhan\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        + _sql_read_payroll_filter(month, year)
        + f"\nORDER BY ThucNhan {order};"
    )


def _sql_query_unassigned() -> str:
    return (
        "SELECT nv.MaNV, nv.HoTen, nv.Email, nv.TrangThai\n"
        "FROM dbo.NHANVIEN nv\nLEFT JOIN dbo.TAIKHOAN tk ON tk.MaNV = nv.MaNV\n"
        "WHERE tk.MaTK IS NULL\nORDER BY nv.MaNV;"
    )


def _sql_query_lich_su_luong() -> str:
    return (
        "SELECT ls.MaNV, nv.HoTen, ls.TuThang, ls.LuongCoBan AS LuongMocLichSu,\n"
        "       nv.LuongCoBan AS LuongHienTai\n"
        "FROM dbo.LICHSULUONG ls\nJOIN dbo.NHANVIEN nv ON nv.MaNV = ls.MaNV\n"
        "ORDER BY ls.MaNV, ls.TuThang;"
    )


def _sql_tv1_demo() -> str:
    return (
        "SELECT MaNV, HoTen, TenPB, TenCV, LuongCoBan\n"
        "FROM dbo.vw_NhanVien_PhongBan_ChucVu\nORDER BY MaNV;"
    )


def _sql_tv2_demo() -> str:
    return (
        "SELECT TOP (1) WITH TIES MaNV, HoTen, Thang, Nam, SoNgayDiLam, SoNgayVang\n"
        "FROM dbo.vw_TongHopChamCongThang\nORDER BY Nam DESC, Thang DESC;"
    )


def _sql_tv3_demo() -> str:
    return (
        "SELECT MaNV, HoTen,\n"
        "       dbo.fn_TongPhuCap(MaNV, Thang, Nam) AS TongPhuCap,\n"
        "       dbo.fn_TongKhauTru(MaNV, Thang, Nam) AS TongKhauTru\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        + _sql_read_payroll_filter()
        + "\nORDER BY MaNV;"
    )


def _sql_tv4_demo() -> str:
    return (
        "SELECT MaNV, HoTen, TienCong AS TienCongDaLuu,\n"
        "       dbo.fn_TinhTienCong(LuongCoBan, NgayCongChuan, NgayCongThucTe) AS TienCongTinhLai\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        + _sql_read_payroll_filter()
        + "\nORDER BY MaNV;"
    )


def _sql_run_existing_payroll(payroll_id: Optional[int] = None,
                              month: Optional[int] = None, year: Optional[int] = None) -> str:
    return (
        _sql_existing_period("CHUA_CHOT", payroll_id, month=month, year=year, include_parameters=True)
        + "EXEC dbo.sp_TinhBangLuongThang @Thang = @Thang, @Nam = @Nam,\n"
        "    @NgayCongChuan = @NgayCongChuan, @MaBangLuong = @MaBangLuong OUTPUT;\n"
        "SELECT MaNV, HoTen, ThucNhan\nFROM dbo.vw_BangLuongChiTiet\n"
        "WHERE MaBangLuong = @MaBangLuong\nORDER BY MaNV;"
    )


def _sql_reset_state() -> str:
    return (
        "IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;\n"
        "BEGIN TRY\n    REVERT;\nEND TRY\nBEGIN CATCH\n"
        "    SELECT ERROR_MESSAGE() AS ThongBaoHoanNguyen;\nEND CATCH;\n"
        "SELECT DB_NAME() AS DatabaseHienTai, @@TRANCOUNT AS SoTransaction,\n"
        "       ORIGINAL_LOGIN() AS LoginKetNoi, SUSER_SNAME() AS LoginHienTai;"
    )


def _sql_help_menu() -> str:
    return (
        "-- Chọn database hiện có trong SSMS trước khi chạy SQL.\n"
        "-- Gõ yêu cầu trên dòng -- rồi nhấn F8 / Alt+Q; Ctrl+Q ưu tiên Gemini.\n"
        "-- Truy vấn và kiểm tra dùng dữ liệu hiện có, không tự tạo database, object hay dữ liệu mẫu.\n"
        "-- test quyền hr | test quyền kế toán | test quyền nhân viên\n"
        "-- test rls | sửa view rls chỉ xem kỳ đã chốt\n"
        "-- chốt bảng lương | mở lại kỳ lương\n"
        "-- test trigger chấm công | phụ cấp | khấu trừ | chi tiết | xóa nhân viên\n"
        "-- Test trigger cần dòng phù hợp đã có; các thay đổi kiểm tra đều rollback.\n"
        "-- test hàm tính thực nhận | sửa hàm thực nhận âm thì về 0 | tràn số 8115\n"
        "-- đo hiệu năng index | benchmark 10000 dòng hiện có\n"
        "-- concurrency cửa sổ 1 | concurrency cửa sổ 2 | xem session đang block\n"
        "-- test check tháng 13 | metadata | xem code sp_ChotBangLuong\n"
        "-- thống kê quỹ lương theo phòng ban | tìm 3 nhân viên lương cao nhất\n"
        "-- tv1 | tv2 | tv3 | tv4 | reset"
    )


def _load_api_key() -> str:
    key = os.environ.get("GEMINI_API_KEY", "").strip()
    if key:
        return key
    key_file = os.path.join(os.path.dirname(os.path.abspath(__file__)), "api_key.txt")
    if os.path.exists(key_file):
        try:
            with open(key_file, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if not line or line.startswith("#"):
                        continue
                    if "=" in line:
                        k, v = line.split("=", 1)
                        if k.strip().upper() == "GEMINI_API_KEY" and v.strip():
                            return v.strip().strip('"').strip("'")
                    else:
                        return line.strip().strip('"').strip("'")
        except Exception:
            pass
    return ""


AI_SQL_RULES = (
    "Bạn là chuyên gia T-SQL. SQL sẽ chạy trong database đang được chọn trong SSMS.\n"
    "Phong cách: code đơn giản, ngắn gọn, dễ đọc; từ khóa SQL phải viết thường (select, from, where, join, exec...). "
    "Giữ nguyên cách viết tên bảng/cột/hàm/biến và giá trị chuỗi. Mỗi mệnh đề chính xuống một dòng.\n"
    "Chỉ sinh phần SQL tối thiểu để giải quyết yêu cầu. Với truy vấn đọc, ưu tiên select trực tiếp; "
    "không thêm hàng loạt declare, if exists, try/catch, metadata, kết quả phụ hoặc thông báo trang trí. "
    "Chỉ giữ biến, kiểm tra và transaction khi cần cho thao tác thay đổi dữ liệu hay kiểm tra trigger.\n"
    "Cú pháp SQL Server: tham số exec chỉ nhận hằng số hoặc biến, không truyền trực tiếp "
    "(select ...), lời gọi hàm hay biểu thức tính toán. Lấy giá trị bằng select vào biến trước, "
    "rồi exec dbo.sp_HuyChotBangLuong @MaBangLuong = @MaBangLuong.\n"
    "Metadata bên dưới được đọc từ database đã tồn tại; chỉ dùng đúng đối tượng/cột/tham số có trong đó.\n"
    "Không thêm USE; chỉ thêm GO để tách batch khi ALTER/CREATE VIEW, FUNCTION, PROCEDURE hoặc TRIGGER thực sự cần.\n"
    "Mặc định không CREATE DATABASE/TABLE/VIEW/FUNCTION/PROCEDURE/TRIGGER/INDEX, không SELECT INTO, "
    "không tự thêm dữ liệu mẫu rồi mới thực hiện yêu cầu. Chỉ sinh DDL khi người dùng yêu cầu rõ việc tạo/sửa cấu trúc.\n"
    "Khi sửa đối tượng đã có, dùng ALTER; khi gọi hàm/thủ tục hoặc kích hoạt trigger, dùng đối tượng đang có.\n"
    "Không đoán mã nhân viên, mã kỳ lương hay ngày tháng. Nếu người dùng không chỉ định mã, SELECT bản ghi thực tế phù hợp; "
    "truy vấn đọc không có dữ liệu thì trả kết quả rỗng. Với thao tác thay đổi, thiếu đối tượng/bản ghi thì báo lỗi; "
    "không tự tạo dữ liệu hay đổi trạng thái để chuẩn bị.\n"
    "Test trigger trên dòng phù hợp đã có, transaction rồi rollback; không tự bật/tắt hoặc tạo lại trigger, "
    "không tự chốt/mở kỳ lương để tạo điều kiện kiểm tra.\n"
    "Chỉ trả T-SQL đúng yêu cầu, kết thúc câu bằng dấu chấm phẩy, không Markdown hay lời giải thích.\n"
)


def _call_gemini_fallback(user_prompt: str) -> Optional[str]:
    global _last_ai_error
    _last_ai_error = ""
    api_key = _load_api_key()
    if not api_key:
        _last_ai_error = "Chưa có GEMINI_API_KEY hoặc file api_key.txt."
        return None
    schema = load_schema_context(**_active_sql_target)
    if not schema:
        _last_ai_error = (
            "Không đọc được schema database hiện có. Kiểm tra sqlcmd, quyền Windows "
            "và kết nối SSMS_SQL_SERVER / SSMS_SQL_DATABASE."
        )
        return None
    schema_prompt = AI_SQL_RULES + "\n" + schema + "\nYêu cầu: " + user_prompt
    configured_model = os.environ.get("GEMINI_MODEL", "").strip()
    model_names = (configured_model,) if configured_model else (
        "gemini-3.5-flash-lite", "gemini-3.8-flash",
    )
    for model_name in model_names:
        if not re.fullmatch(r"[A-Za-z0-9._-]+", model_name):
            _last_ai_error = "GEMINI_MODEL không phải mã model hợp lệ."
            return None
        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/{model_name}:generateContent"
            body = {"contents": [{"parts": [{"text": schema_prompt}]}]}
            payload = json.dumps(body).encode("utf-8")
            req = urllib.request.Request(
                url, data=payload,
                headers={"Content-Type": "application/json", "x-goog-api-key": api_key},
            )
            with urllib.request.urlopen(req, timeout=12.0) as resp:
                data = json.loads(resp.read().decode("utf-8"))
            parts = data.get("candidates", [{}])[0].get("content", {}).get("parts", [])
            sql = "\n".join(part["text"] for part in parts if "text" in part and not part.get("thought")).strip()
            sql = re.sub(r"^```(?:sql|tsql)?\s*", "", sql, flags=re.IGNORECASE)
            sql = re.sub(r"\s*```$", "", sql).strip()
            if sql:
                return sql
            _last_ai_error = "Gemini không trả về đoạn SQL có thể dùng."
        except urllib.error.HTTPError as exc:
            reasons = {
                400: "Gemini từ chối yêu cầu hoặc cấu hình model (HTTP 400).",
                401: "API key không hợp lệ (HTTP 401).",
                403: "API key hoặc project không có quyền gọi model (HTTP 403).",
                404: "Không tìm thấy model Gemini; kiểm tra GEMINI_MODEL (HTTP 404).",
                429: "Gemini đã hết hạn mức hoặc đang giới hạn tốc độ (HTTP 429).",
            }
            _last_ai_error = reasons.get(exc.code, f"Gemini trả lỗi HTTP {exc.code}.")
        except (urllib.error.URLError, TimeoutError):
            _last_ai_error = "Không kết nối được Gemini hoặc đã hết thời gian chờ."
        except (ValueError, KeyError, IndexError, TypeError):
            _last_ai_error = "Gemini trả về dữ liệu không đúng định dạng."
    return None
