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
from typing import Optional, Tuple, List


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
    """Strip comments and decorative banners so typed SQL looks 100% hand-written live."""
    no_block = re.sub(r"/\*.*?\*/", "", sql, flags=re.DOTALL)
    out_lines = []
    for line in no_block.splitlines():
        clean_line = re.sub(r"\s*--.*$", "", line).rstrip()
        if not clean_line.strip():
            continue
        if re.match(r"^\s*PRINT\b", clean_line, flags=re.IGNORECASE):
            if "ERROR_MESSAGE" in clean_line.upper():
                indent = re.match(r"^\s*", clean_line).group(0)
                out_lines.append(f"{indent}PRINT ERROR_MESSAGE();")
            continue
        out_lines.append(clean_line)
    return "\n".join(out_lines)


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
        return _sql_help_menu()

    # Force AI mode if triggered via stealth syntax (`---`, trailing `.`, `Ctrl+Q`) or legacy `ai `
    if force_ai or norm.startswith("ai "):
        prompt_text = cleaned_raw[3:].strip() if norm.startswith("ai ") else cleaned_raw
        ai_sql = _call_gemini_fallback(prompt_text)
        if ai_sql:
            return _clean_for_human_typing(ai_sql) if clean_comments else ai_sql

    res = _generate_offline_or_fallback(cleaned_raw, norm)
    return _clean_for_human_typing(res) if clean_comments else res


def _generate_offline_or_fallback(cleaned_raw: str, norm: str) -> str:

    # 1. Rescue / Reset
    if any(w in norm for w in ["reset", "cuu ho", "khoi phuc trang thai", "ve ban dau"]):
        return _sql_reset_state()

    # 2. sp_helptext / View source code of any object
    if any(w in norm for w in ["helptext", "xem code", "in code", "code cua", "dinh nghia cua"]):
        m = re.search(r"(sp_[a-z0-9_]+|trg_[a-z0-9_]+|vw_[a-z0-9_]+|fn_[a-z0-9_]+)", norm)
        obj = f"dbo.{m.group(1)}" if m else _extract_object(norm)[0]
        return (
            f"USE QuanLyNhanSuTienLuong;\nGO\n"
            f"EXEC sp_helptext '{obj}';\n"
            f"-- Hoặc lấy nguyên khối:\n"
            f"SELECT OBJECT_DEFINITION(OBJECT_ID(N'{obj}')) AS CodeDinhNghia;\nGO"
        )

    # 3. Create Login + User + Assign Role
    if any(w in norm for w in ["tao login", "create login", "tao user", "tao tai khoan sql"]):
        m = re.search(r"(?:login|user|tai khoan)\s+([a-zA-Z0-9_]+)", norm)
        login_id = m.group(1) if m and m.group(1) not in ("sql", "moi", "cho", "va") else "test_gv"
        role_name, _ = _extract_role(norm)
        return (
            f"USE QuanLyNhanSuTienLuong;\nGO\n"
            f"-- 1. Tạo SQL Login ở mức Server + Tạo User trong DB + Gán vào {role_name}:\n"
            f"CREATE LOGIN [{login_id}] WITH PASSWORD = N'Test@123456', CHECK_POLICY = ON;\n"
            f"CREATE USER [{login_id}] FOR LOGIN [{login_id}];\n"
            f"ALTER ROLE {role_name} ADD MEMBER [{login_id}];\nGO\n"
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
            f"USE QuanLyNhanSuTienLuong;\nGO\n"
            f"ALTER ROLE {role_name} {action} [{user_target}];\nGO"
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
            f"USE QuanLyNhanSuTienLuong;\nGO\n"
            f"SELECT pr.name AS TenRole, dp.state_desc AS TrangThai,\n"
            f"       dp.permission_name AS Quyen, OBJECT_NAME(dp.major_id) AS DoiTuong\n"
            f"FROM sys.database_permissions dp\n"
            f"JOIN sys.database_principals pr ON dp.grantee_principal_id = pr.principal_id\n"
            f"WHERE pr.name LIKE 'role_%' -- Hoặc lọc riêng: pr.name = '{role_name}'\n"
            f"ORDER BY pr.name, dp.state_desc, DoiTuong;\nGO"
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
        if any(w in norm for w in ["sua view", "da_chot", "da chot", "suser_sname", "create or alter view"]):
            return _sql_alter_rls_view(only_da_chot=("da_chot" in norm or "da chot" in norm))
        return _sql_test_rls()

    # 9. Section 3: Payroll Closing & Reopening (sp_ChotBangLuong / sp_HuyChotBangLuong)
    if any(w in norm for w in ["mo lai", "huy chot", "sp_huychotbangluong", "3.4"]):
        return (
            "USE QuanLyNhanSuTienLuong;\nGO\n"
            "EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009;\n"
            "SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot FROM dbo.BANGLUONG WHERE MaBangLuong = 1009;\nGO"
        )
    if any(w in norm for w in ["chot bang luong", "chot luong", "sp_chotbangluong", "phan 3", "3.1", "3.2", "3.3"]):
        return _sql_test_chot_bang_luong()

    # 10. Section 4: Triggers (5 TV5 Triggers + Enable/Disable)
    if "trigger" in norm or "phan 4" in norm or any(w in norm for w in ["4.1", "4.2", "4.3", "4.4", "4.5", "khoa so"]):
        if any(w in norm for w in ["tat trigger", "disable trigger", "bat trigger", "enable trigger"]):
            return _sql_toggle_trigger(norm)
        if any(w in norm for w in ["cham cong", "chamcong", "4.1"]):
            return _sql_trigger_chamcong()
        if any(w in norm for w in ["phu cap", "phucap", "4.2"]):
            return _sql_trigger_phucap()
        if any(w in norm for w in ["khau tru", "khautru", "4.3"]):
            return _sql_trigger_khautru()
        if any(w in norm for w in ["chi tiet", "chitiet", "100 trieu", "4.4"]):
            return _sql_trigger_chitiet()
        if any(w in norm for w in ["xoa nhan vien", "delete nhan vien", "nhanvien", "4.5"]):
            return _sql_trigger_xoa_nv()
        return _sql_all_5_triggers()

    # 11. Section 5: Function fn_TinhThucNhan & Overflow 8115
    if any(w in norm for w in ["tran so", "8115", "overflow", "bien tien te", "decimal"]):
        return _sql_test_money_overflow()
    if any(w in norm for w in ["fn_tinhthucnhan", "thuc nhan", "sua ham", "phan 5", "ham tinh"]):
        if any(w in norm for w in ["ve 0", "bang 0", "khong am", "sua ham", "create or alter function"]):
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
    if not has_custom_top_n:
        if any(w in norm for w in ["thong ke", "group by", "having", "quy luong", "theo phong ban"]):
            return _sql_query_group_by_phongban()
        if any(w in norm for w in ["top", "cao nhat", "thap nhat", "nhieu nhat", "it nhat"]):
            order = "ASC" if any(w in norm for w in ["thap nhat", "it nhat"]) else "DESC"
            return _sql_query_top_luong(order)
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
        return ai_sql

    # Default fallback: return a helpful comment + quick metadata lookup
    return (
        f"-- [Gợi ý nhanh cho yêu cầu: {cleaned_raw}]\n"
        f"-- Gõ: -- #help (và bấm Alt+Q hoặc F8) để xem danh sách từ khóa hỗ trợ.\n"
        f"USE QuanLyNhanSuTienLuong;\nGO\n"
        f"SELECT TOP(10) * FROM dbo.vw_BangLuongChiTiet WHERE Thang = 10 AND Nam = 2026;\nGO"
    )


# ---------------------------------------------------------------------------
# Template Builders
# ---------------------------------------------------------------------------
def _sql_dynamic_permission(norm: str) -> str:
    role_name, login_name = _extract_role(norm)
    obj_name, is_exec = _extract_object(norm)
    privs = _extract_privileges(norm, is_exec)

    is_deny = any(w in norm for w in ["cam", "chan", "deny", "khong cho"])
    is_revoke = any(w in norm for w in ["thu hoi", "go quyen", "xoa quyen", "revoke"])

    if is_deny:
        main_cmd = f"DENY {privs} ON {obj_name} TO {role_name};"
        restore_cmd = f"-- Khôi phục lại (nếu cần): REVOKE {privs} ON {obj_name} FROM {role_name};"
    elif is_revoke:
        main_cmd = f"REVOKE {privs} ON {obj_name} FROM {role_name};"
        restore_cmd = f"-- Cấp lại (nếu cần): GRANT {privs} ON {obj_name} TO {role_name};"
    else:
        main_cmd = (
            f"-- Gỡ lệnh DENY cũ (nếu có) trước khi GRANT:\n"
            f"REVOKE {privs} ON {obj_name} FROM {role_name};\n"
            f"GRANT {privs} ON {obj_name} TO {role_name};"
        )
        restore_cmd = f"-- Khóa lại như thiết kế gốc sau khi test: DENY {privs} ON {obj_name} TO {role_name};"

    test_stmt = (
        f"EXEC {obj_name} @MaBangLuong = 1009;"
        if "sp_ChotBangLuong" in obj_name
        else (f"EXEC {obj_name};" if is_exec else f"SELECT TOP(3) * FROM {obj_name};")
    )

    return (
        f"USE QuanLyNhanSuTienLuong;\nGO\n"
        f"{main_cmd}\nGO\n\n"
        f"-- Test ngay dưới ngữ cảnh '{login_name}' ({role_name}):\n"
        f"EXECUTE AS LOGIN = '{login_name}';\n"
        f"BEGIN TRY\n"
        f"    {test_stmt}\n"
        f"    PRINT N'-> THỰC THI THÀNH CÔNG dưới quyền ' + SUSER_SNAME();\n"
        f"END TRY\n"
        f"BEGIN CATCH\n"
        f"    PRINT N'-> BỊ CHẶN (Mã lỗi ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N'): ' + ERROR_MESSAGE();\n"
        f"END CATCH;\n"
        f"REVERT;\nGO\n"
        f"{restore_cmd}\n"
    )


def _sql_test_rbac_hr() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "EXECUTE AS LOGIN = 'gui_hr';\n"
        "PRINT N'=== ĐANG CHẠY DƯỚI QUYỀN: ' + SUSER_SNAME() + N' ===';\n\n"
        "-- 1. Thao tác hợp lệ (HR đọc nhân sự & chấm công):\n"
        "SELECT TOP(3) MaNV, HoTen, MaPB, MaCV, TrangThai FROM dbo.NHANVIEN;\n"
        "SELECT TOP(3) MaChamCong, MaNV, NgayChamCong, TrangThai FROM dbo.CHAMCONG;\n\n"
        "-- 2. Thao tác bị cấm: HR chốt lương (DENY EXECUTE sp_ChotBangLuong -> Lỗi 229):\n"
        "BEGIN TRY\n"
        "    EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> CHẶN HR CHỐT LƯƠNG: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';\n"
        "END CATCH;\n\n"
        "-- 3. Thao tác bị cấm: HR đọc bảng TAIKHOAN (DENY SELECT TAIKHOAN -> Lỗi 229):\n"
        "BEGIN TRY\n"
        "    SELECT TOP(1) * FROM dbo.TAIKHOAN;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> CHẶN HR ĐỌC TAIKHOAN: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';\n"
        "END CATCH;\n\n"
        "REVERT;\nGO"
    )


def _sql_test_rbac_payroll() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "EXECUTE AS LOGIN = 'gui_payroll';\n"
        "PRINT N'=== ĐANG CHẠY DƯỚI QUYỀN: ' + SUSER_SNAME() + N' ===';\n\n"
        "-- 1. Hợp lệ: Kế toán đọc bảng lương:\n"
        "SELECT TOP(3) MaBangLuong, Thang, Nam, TrangThai FROM dbo.BANGLUONG;\n\n"
        "-- 2. Bị cấm: Kế toán sửa hồ sơ nhân viên (DENY UPDATE NHANVIEN -> Lỗi 229):\n"
        "BEGIN TRY\n"
        "    UPDATE dbo.NHANVIEN SET HoTen = N'Hacker Sửa Tên' WHERE MaNV = 1008;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> CHẶN PAYROLL SỬA NHANVIEN: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';\n"
        "END CATCH;\n\n"
        "-- 3. Bị cấm: Kế toán chèn chấm công (DENY INSERT CHAMCONG -> Lỗi 229):\n"
        "BEGIN TRY\n"
        "    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai)\n"
        "    VALUES (1008, '2026-10-28', '08:00', '17:00', N'CO_MAT');\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> CHẶN PAYROLL THÊM CHAMCONG: ' + ERROR_MESSAGE() + N' (Mã lỗi: ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N')';\n"
        "END CATCH;\n\n"
        "REVERT;\nGO"
    )


def _sql_test_rbac_employee() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "EXECUTE AS LOGIN = 'gui_a';\n"
        "PRINT N'=== ĐANG CHẠY DƯỚI QUYỀN: ' + SUSER_SNAME() + N' ===';\n\n"
        "BEGIN TRY\n"
        "    SELECT * FROM dbo.BANGLUONG;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> CHẶN EMPLOYEE ĐỌC BANGLUONG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\n\n"
        "BEGIN TRY\n"
        "    SELECT * FROM dbo.vw_BangLuongChiTiet;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> CHẶN EMPLOYEE ĐỌC vw_BangLuongChiTiet: ' + ERROR_MESSAGE();\n"
        "END CATCH;\n\n"
        "REVERT;\nGO"
    )


def _sql_test_rls() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- 1. Kiểm tra phiếu lương Nhân viên A (gui_a - MaNV 1008 - Nguyễn Tuấn Anh):\n"
        "EXECUTE AS LOGIN = 'gui_a';\n"
        "SELECT ORIGINAL_LOGIN() AS LoginKetNoi, SUSER_SNAME() AS LoginHienTai,\n"
        "       MaNV, HoTen, Thang, Nam, LuongCoBan, NgayCongThucTe, TongPhuCap, TongKhauTru, ThucNhan\n"
        "FROM dbo.vw_PhieuLuongCaNhan;\n"
        "REVERT;\n\n"
        "-- 2. Kiểm tra phiếu lương Nhân viên B (gui_b - MaNV 1009 - Trần Thị Bảo Bình):\n"
        "EXECUTE AS LOGIN = 'gui_b';\n"
        "SELECT ORIGINAL_LOGIN() AS LoginKetNoi, SUSER_SNAME() AS LoginHienTai,\n"
        "       MaNV, HoTen, Thang, Nam, LuongCoBan, NgayCongThucTe, TongPhuCap, TongKhauTru, ThucNhan\n"
        "FROM dbo.vw_PhieuLuongCaNhan;\n"
        "REVERT;\nGO"
    )


def _sql_alter_rls_view(only_da_chot: bool = False) -> str:
    extra = "\n  AND bl.TrangThaiBangLuong = 'DA_CHOT'" if only_da_chot else ""
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "CREATE OR ALTER VIEW dbo.vw_PhieuLuongCaNhan\n"
        "AS\n"
        "SELECT bl.*\n"
        "FROM dbo.vw_BangLuongChiTiet bl\n"
        "JOIN dbo.TAIKHOAN tk ON tk.MaNV = bl.MaNV\n"
        "WHERE tk.SqlLogin IN (ORIGINAL_LOGIN(), SUSER_SNAME())\n"
        f"  AND tk.TrangThai = 'HOAT_DONG'{extra};\n"
        "GO"
    )


def _sql_test_chot_bang_luong() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- Bước 1: Đưa kỳ 1009 về CHUA_CHOT nếu đang DA_CHOT\n"
        "IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = 1009 AND TrangThai = 'DA_CHOT')\n"
        "    EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009;\nGO\n\n"
        "-- Bước 2: Chốt bảng lương kỳ 1009 (Kỳ vọng: THÀNH CÔNG -> DA_CHOT)\n"
        "EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;\n"
        "SELECT MaBangLuong, Thang, Nam, TrangThai, NgayChot FROM dbo.BANGLUONG WHERE MaBangLuong = 1009;\nGO\n\n"
        "-- Bước 3: Cố tình chốt lại lần 2 (Kỳ vọng: Bẫy lỗi 50000 mức 16)\n"
        "BEGIN TRY\n"
        "    EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> BẪY LỖI CHỐT LẠI THÀNH CÔNG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_trigger_chamcong() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "BEGIN TRY\n"
        "    INSERT INTO dbo.CHAMCONG (MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu)\n"
        "    VALUES (1008, '2026-10-15', '08:00:00', '17:00:00', N'CO_MAT', N'Test Trigger Chấm Công');\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> TRIGGER CHẤM CÔNG BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_trigger_phucap() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "BEGIN TRY\n"
        "    INSERT INTO dbo.PHUCAPNHANVIEN (MaNV, Thang, Nam, TenPhuCap, SoTien, NgayGhiNhan)\n"
        "    VALUES (1008, 10, 2026, N'Phụ cấp khống', 2000000, GETDATE());\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> TRIGGER PHỤ CẤP BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_trigger_khautru() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "BEGIN TRY\n"
        "    INSERT INTO dbo.KHAUTRUNHANVIEN (MaNV, Thang, Nam, TenKhauTru, SoTien, NgayGhiNhan)\n"
        "    VALUES (1008, 10, 2026, N'Phạt bổ sung', 100000, GETDATE());\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> TRIGGER KHẤU TRỪ BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_trigger_chitiet() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "BEGIN TRY\n"
        "    UPDATE dbo.CHITIETBANGLUONG SET ThucNhan = 100000000.00 WHERE MaBangLuong = 1009 AND MaNV = 1008;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> TRIGGER CHI TIẾT LƯƠNG BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_trigger_xoa_nv() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "BEGIN TRY\n"
        "    DELETE FROM dbo.NHANVIEN WHERE MaNV = 1008;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> TRIGGER CHẶN XÓA NHÂN VIÊN BẮT THÀNH CÔNG: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_all_5_triggers() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = 1009 AND TrangThai = 'CHUA_CHOT')\n"
        "    EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;\nGO\n"
        + _sql_trigger_chamcong()
        + "\n"
        + _sql_trigger_phucap()
        + "\n"
        + _sql_trigger_khautru()
        + "\n"
        + _sql_trigger_chitiet()
        + "\n"
        + _sql_trigger_xoa_nv()
    )


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
    return (
        f"USE QuanLyNhanSuTienLuong;\nGO\n"
        f"-- Tắt tạm thời Trigger:\n"
        f"DISABLE TRIGGER {trig} ON {tbl};\nGO\n"
        f"-- Bật lại Trigger:\n"
        f"ENABLE TRIGGER {trig} ON {tbl};\nGO"
    )


def _sql_test_fn_thuc_nhan() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT\n"
        "    dbo.fn_TinhThucNhan(10000000, 2000000, 1000000) AS [1_Binh_Thuong_11M],\n"
        "    dbo.fn_TinhThucNhan(NULL, 1500000, NULL)        AS [2_Xu_Ly_NULL_1.5M],\n"
        "    dbo.fn_TinhThucNhan(5000000, 0, 7000000)        AS [3_Thuc_Nhan_Am_-2M];\nGO"
    )


def _sql_alter_fn_thuc_nhan_clamp_zero() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- Sửa hàm fn_TinhThucNhan: nếu Thực nhận < 0 thì ép về 0\n"
        "CREATE OR ALTER FUNCTION dbo.fn_TinhThucNhan(\n"
        "    @TienCong    DECIMAL(18,2),\n"
        "    @TongPhuCap  DECIMAL(18,2),\n"
        "    @TongKhauTru DECIMAL(18,2)\n"
        ")\n"
        "RETURNS DECIMAL(18,2)\n"
        "AS\n"
        "BEGIN\n"
        "    DECLARE @KetQua DECIMAL(18,2) = ISNULL(@TienCong, 0) + ISNULL(@TongPhuCap, 0) - ISNULL(@TongKhauTru, 0);\n"
        "    RETURN CASE WHEN @KetQua < 0 THEN 0 ELSE @KetQua END;\n"
        "END;\nGO\n"
        "SELECT dbo.fn_TinhThucNhan(5000000, 0, 7000000) AS KetQua_DaEpVe0;\nGO"
    )


def _sql_test_money_overflow() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- 1. Tính đúng ở biên tối đa DECIMAL(18,2):\n"
        "SELECT dbo.fn_TinhThucNhan(9999999999999999.99, 0.00, 0.00) AS Max_Decimal_18_2;\n\n"
        "-- 2. Bẫy lỗi tràn số Arithmetic Overflow (Mã lỗi 8115):\n"
        "BEGIN TRY\n"
        "    SELECT dbo.fn_TinhThucNhan(9999999999999999.99, 1.00, 0.00) AS Tran_So;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    PRINT N'-> BẮT THÀNH CÔNG LỖI TRÀN SỐ (Mã ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N'): ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_benchmark_index() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SET NOCOUNT ON;\n"
        "SET STATISTICS IO ON;\n"
        "SET STATISTICS TIME ON;\n\n"
        "PRINT N'=== 1. CLUSTERED INDEX SCAN (INDEX 1) ===';\n"
        "SELECT MaNV, HoTen, LuongCoBan, TrangThai\n"
        "FROM dbo.NHANVIEN WITH (INDEX(1))\n"
        "WHERE MaPB = 2 AND MaCV = 1;\n\n"
        "PRINT N'=== 2. COVERING INDEX SEEK (IX_NHANVIEN_MaPB_MaCV) ===';\n"
        "SELECT MaNV, HoTen, LuongCoBan, TrangThai\n"
        "FROM dbo.NHANVIEN WITH (INDEX(IX_NHANVIEN_MaPB_MaCV))\n"
        "WHERE MaPB = 2 AND MaCV = 1;\n\n"
        "SET STATISTICS IO OFF;\n"
        "SET STATISTICS TIME OFF;\nGO"
    )


def _sql_benchmark_10k() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SET NOCOUNT ON;\n"
        "IF OBJECT_ID('tempdb..#BenchmarkNhanVien') IS NOT NULL DROP TABLE #BenchmarkNhanVien;\n\n"
        "SELECT CONVERT(INT, ROW_NUMBER() OVER (ORDER BY a.object_id, b.object_id)) AS MaNV,\n"
        "       CONVERT(NVARCHAR(100), N'Nhân viên test ' + CONVERT(NVARCHAR(10), ROW_NUMBER() OVER (ORDER BY a.object_id, b.object_id))) AS HoTen,\n"
        "       CONVERT(INT, (ROW_NUMBER() OVER (ORDER BY a.object_id, b.object_id) % 4) + 1) AS MaPB,\n"
        "       CONVERT(INT, (ROW_NUMBER() OVER (ORDER BY a.object_id, b.object_id) % 3) + 1) AS MaCV,\n"
        "       CAST(10000000 + (ROW_NUMBER() OVER (ORDER BY a.object_id, b.object_id) % 20) * 500000 AS DECIMAL(18,2)) AS LuongCoBan,\n"
        "       CONVERT(NVARCHAR(20), N'DANG_LAM_VIEC') AS TrangThai\n"
        "INTO #BenchmarkNhanVien\n"
        "FROM sys.all_objects a CROSS JOIN sys.all_objects b\n"
        "ORDER BY a.object_id, b.object_id OFFSET 0 ROWS FETCH NEXT 10000 ROWS ONLY;\n\n"
        "CREATE UNIQUE CLUSTERED INDEX PK_Bench_MaNV ON #BenchmarkNhanVien(MaNV);\n"
        "CREATE NONCLUSTERED INDEX IX_Bench_MaPB_MaCV ON #BenchmarkNhanVien(MaPB, MaCV) INCLUDE(HoTen, LuongCoBan, TrangThai);\n"
        "UPDATE STATISTICS #BenchmarkNhanVien WITH FULLSCAN;\n\n"
        "PRINT N'=== [BEFORE] CLUSTERED SCAN (~126-142 LOGICAL READS) ===';\n"
        "SET STATISTICS IO ON; SET STATISTICS TIME ON; SET STATISTICS XML ON;\nGO\n"
        "SELECT MaNV, HoTen, LuongCoBan, TrangThai FROM #BenchmarkNhanVien WITH(INDEX(PK_Bench_MaNV)) WHERE MaPB = 2 AND MaCV = 1 OPTION(MAXDOP 1);\nGO\n"
        "SET STATISTICS XML OFF; SET STATISTICS IO OFF; SET STATISTICS TIME OFF;\n\n"
        "PRINT N'=== [AFTER] COVERING INDEX SEEK (~13-15 LOGICAL READS) ===';\n"
        "SET STATISTICS IO ON; SET STATISTICS TIME ON; SET STATISTICS XML ON;\nGO\n"
        "SELECT MaNV, HoTen, LuongCoBan, TrangThai FROM #BenchmarkNhanVien WITH(INDEX(IX_Bench_MaPB_MaCV)) WHERE MaPB = 2 AND MaCV = 1 OPTION(MAXDOP 1);\nGO\n"
        "SET STATISTICS XML OFF; SET STATISTICS IO OFF; SET STATISTICS TIME OFF;\n"
        "DROP TABLE #BenchmarkNhanVien;\nGO"
    )


def _sql_concurrency_window1() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = 1009 AND TrangThai = 'DA_CHOT')\n"
        "    EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009;\nGO\n\n"
        "BEGIN TRANSACTION;\n"
        "SELECT MaBangLuong, Thang, Nam, TrangThai\n"
        "FROM dbo.BANGLUONG WITH (UPDLOCK, HOLDLOCK)\n"
        "WHERE MaBangLuong = 1009;\n\n"
        "PRINT N'Phiên A (SPID = ' + CAST(@@SPID AS NVARCHAR(10)) + N') đang giữ khóa UPDLOCK + HOLDLOCK trên kỳ 1009...';\n"
        "-- Sau khi chạy Cửa sổ 2 thấy bị xoay vòng chờ, bôi đen dòng dưới và bấm F5 để nhả khóa:\n"
        "-- ROLLBACK TRANSACTION;"
    )


def _sql_concurrency_window2() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "PRINT N'Phiên B (SPID = ' + CAST(@@SPID AS NVARCHAR(10)) + N') đang gọi sp_ChotBangLuong...';\n"
        "EXEC dbo.sp_ChotBangLuong @MaBangLuong = 1009;\nGO"
    )


def _sql_check_blocking_sessions() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- 1. Xem phiên nào đang bị BLOCK và ai đang giữ khóa (blocking_session_id):\n"
        "SELECT session_id AS PhienBiChan, blocking_session_id AS PhienDangGiuKhoa,\n"
        "       wait_type, wait_time, status, command\n"
        "FROM sys.dm_exec_requests\n"
        "WHERE blocking_session_id > 0;\n\n"
        "-- 2. Xem chi tiết loại khóa (U, X, S, IX...) đang giữ trong DB:\n"
        "SELECT request_session_id AS Spid, resource_type,\n"
        "       OBJECT_NAME(resource_associated_entity_id) AS TenBang,\n"
        "       request_mode AS LoaiKhoa, request_status AS TrangThai\n"
        "FROM sys.dm_tran_locks\n"
        "WHERE resource_database_id = DB_ID('QuanLyNhanSuTienLuong')\n"
        "  AND resource_type IN ('OBJECT', 'KEY', 'PAGE', 'RID');\nGO"
    )


def _sql_constraints_metadata() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT t.name AS Bang, c.name AS TenRangBuoc, c.type_desc AS LoaiRangBuoc,\n"
        "       OBJECT_DEFINITION(c.object_id) AS DinhNghiaChiTiet\n"
        "FROM sys.objects c\n"
        "JOIN sys.tables t ON t.object_id = c.parent_object_id\n"
        "WHERE t.name IN ('BANGLUONG', 'CHITIETBANGLUONG', 'TAIKHOAN')\n"
        "  AND c.type_desc LIKE '%CONSTRAINT'\n"
        "ORDER BY t.name, c.type_desc, c.name;\nGO"
    )


def _sql_test_constraints(norm: str) -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- 1. CHECK Constraint: Chặn nhập tháng 13 (CHK_BANGLUONG_Thang -> Lỗi 547)\n"
        "BEGIN TRY\n"
        "    BEGIN TRANSACTION;\n"
        "    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai) VALUES (13, 2026, 26, 'CHUA_CHOT');\n"
        "    ROLLBACK TRANSACTION;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;\n"
        "    PRINT N'[CHK_BANGLUONG_Thang - Mã ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N']: ' + ERROR_MESSAGE();\n"
        "END CATCH;\n\n"
        "-- 2. UNIQUE Constraint: Chặn trùng kỳ 10/2026 (UQ_BANGLUONG_ThangNam -> Lỗi 2627)\n"
        "BEGIN TRY\n"
        "    BEGIN TRANSACTION;\n"
        "    INSERT INTO dbo.BANGLUONG (Thang, Nam, NgayCongChuan, TrangThai) VALUES (10, 2026, 26, 'CHUA_CHOT');\n"
        "    ROLLBACK TRANSACTION;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;\n"
        "    PRINT N'[UQ_BANGLUONG_ThangNam - Mã ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N']: ' + ERROR_MESSAGE();\n"
        "END CATCH;\n\n"
        "-- 3. CHECK Constraint: Chặn vai trò lạ trên TAIKHOAN (CHK_TAIKHOAN_VaiTro -> Lỗi 547)\n"
        "BEGIN TRY\n"
        "    BEGIN TRANSACTION;\n"
        "    INSERT INTO dbo.TAIKHOAN (MaNV, TenDangNhap, MatKhau, VaiTro, TrangThai)\n"
        "    VALUES (1008, 'test_hacker', 'hash123', 'Hacker_Role', 'HOAT_DONG');\n"
        "    ROLLBACK TRANSACTION;\n"
        "END TRY\n"
        "BEGIN CATCH\n"
        "    IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;\n"
        "    PRINT N'[CHK_TAIKHOAN_VaiTro - Mã ' + CAST(ERROR_NUMBER() AS NVARCHAR(10)) + N']: ' + ERROR_MESSAGE();\n"
        "END CATCH;\nGO"
    )


def _sql_test_admin_sps() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- 1. Kiểm tra danh tính hiện tại qua ORIGINAL_LOGIN() và IS_ROLEMEMBER:\n"
        "EXEC dbo.sp_LayTaiKhoanHienTai;\n\n"
        "-- 2. Khóa tài khoản gui_b và mở khóa lại qua sp_AdminSetStatus:\n"
        "DECLARE @MaTK INT = (SELECT MaTK FROM dbo.TAIKHOAN WHERE TenDangNhap = 'gui_b');\n"
        "EXEC dbo.sp_AdminSetStatus @MaTK = @MaTK, @TrangThai = 'KHOA';\n"
        "SELECT TenDangNhap, TrangThai, is_disabled AS SqlLoginDisabled\n"
        "FROM dbo.TAIKHOAN tk JOIN sys.server_principals sp ON sp.name = tk.SqlLogin WHERE tk.MaTK = @MaTK;\n\n"
        "EXEC dbo.sp_AdminSetStatus @MaTK = @MaTK, @TrangThai = 'HOAT_DONG';\nGO"
    )


def _sql_query_group_by_phongban() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT\n"
        "    TenPB,\n"
        "    COUNT(MaNV)     AS SoNhanVien,\n"
        "    SUM(TienCong)   AS TongTienCong,\n"
        "    SUM(TongPhuCap) AS TongPhuCap,\n"
        "    SUM(TongKhauTru) AS TongKhauTru,\n"
        "    SUM(ThucNhan)   AS TongQuyLuongThucNhan,\n"
        "    AVG(ThucNhan)   AS LuongBinhQuan\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        "WHERE Thang = 10 AND Nam = 2026\n"
        "GROUP BY TenPB\n"
        "ORDER BY TongQuyLuongThucNhan DESC;\nGO"
    )


def _sql_query_top_luong(order: str = "DESC") -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT TOP (1) WITH TIES\n"
        "    MaNV, HoTen, TenPB, TenCV, LuongCoBan, NgayCongThucTe, TongPhuCap, TongKhauTru, ThucNhan\n"
        "FROM dbo.vw_BangLuongChiTiet\n"
        "WHERE Thang = 10 AND Nam = 2026\n"
        f"ORDER BY ThucNhan {order};\nGO"
    )


def _sql_query_unassigned() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "-- Nhân viên chưa được cấp tài khoản ứng dụng:\n"
        "SELECT nv.MaNV, nv.HoTen, nv.Email, nv.TrangThai\n"
        "FROM dbo.NHANVIEN nv\n"
        "LEFT JOIN dbo.TAIKHOAN tk ON tk.MaNV = nv.MaNV\n"
        "WHERE tk.MaTK IS NULL;\nGO"
    )


def _sql_query_lich_su_luong() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT ls.MaNV, nv.HoTen, ls.TuThang, ls.LuongCoBan AS LuongMocLichSu, nv.LuongCoBan AS LuongHienTai\n"
        "FROM dbo.LICHSULUONG ls\n"
        "JOIN dbo.NHANVIEN nv ON nv.MaNV = ls.MaNV\n"
        "ORDER BY ls.MaNV, ls.TuThang;\nGO"
    )


def _sql_tv1_demo() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT * FROM dbo.vw_NhanVien_PhongBan_ChucVu;\n"
        "SELECT dbo.fn_TinhSoNgayCong(1008, 10, 2026) AS SoNgayCong_NV1008;\nGO"
    )


def _sql_tv2_demo() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT * FROM dbo.vw_TongHopChamCongThang WHERE Nam = 2026 AND Thang = 10;\nGO"
    )


def _sql_tv3_demo() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT dbo.fn_TongPhuCap(1008, 10, 2026) AS TongPhuCap_1008,\n"
        "       dbo.fn_TongKhauTru(1008, 10, 2026) AS TongKhauTru_1008;\n"
        "SELECT * FROM dbo.vw_TongPhuCapThang WHERE Thang = 10 AND Nam = 2026;\nGO"
    )


def _sql_tv4_demo() -> str:
    return (
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "SELECT dbo.fn_TinhTienCong(26000000, 26, 24) AS TienCong_24Ngay;\n"
        "SELECT * FROM dbo.vw_TongKhauTruThang WHERE Thang = 10 AND Nam = 2026;\nGO"
    )


def _sql_reset_state() -> str:
    return (
        "REVERT;\n"
        "IF @@TRANCOUNT > 0 ROLLBACK TRANSACTION;\nGO\n"
        "USE QuanLyNhanSuTienLuong;\nGO\n"
        "DENY EXECUTE ON dbo.sp_ChotBangLuong TO role_HRManager;\n"
        "DENY SELECT, INSERT, UPDATE, DELETE ON dbo.TAIKHOAN TO role_HRManager;\n"
        "UPDATE dbo.TAIKHOAN SET TrangThai = 'HOAT_DONG' WHERE TenDangNhap LIKE 'gui_%';\n"
        "IF EXISTS (SELECT 1 FROM dbo.BANGLUONG WHERE MaBangLuong = 1009 AND TrangThai = 'DA_CHOT')\n"
        "    EXEC dbo.sp_HuyChotBangLuong @MaBangLuong = 1009;\n"
        "PRINT N'[OK] Đã khôi phục toàn bộ quyền, tài khoản và kỳ lương 1009 về trạng thái chuẩn!';\nGO"
    )


def _sql_help_menu() -> str:
    return (
        "-- ======================================================================\n"
        "-- DANH SÁCH LỆNH GỢI Ý NHANH TRONG SSMS (Gõ -- #... rồi bấm Alt+Q hoặc F8)\n"
        "-- ======================================================================\n"
        "-- 1. Phân quyền động:   -- #cấp quyền chốt lương cho hr\n"
        "--                       -- #cấm kế toán sửa bảng nhân viên\n"
        "--                       -- #thu hồi quyền select trên bangluong của nhân viên\n"
        "--                       -- #tạo login test_gv và gán vào role hr\n"
        "--                       -- #xem quyền của role hr\n"
        "-- 2. Test 4 Roles:      -- #test quyền hr  |  -- #test quyền kế toán  |  -- #test quyền nhân viên\n"
        "-- 3. Test & Sửa RLS:    -- #test rls  |  -- #sửa view rls chỉ xem kỳ đã chốt\n"
        "-- 4. Chốt / Mở lương:   -- #chốt bảng lương  |  -- #mở lại kỳ lương\n"
        "-- 5. Test 5 Triggers:   -- #test trigger (hoặc: chấm công / phụ cấp / khấu trừ / chi tiết / xóa nhân viên)\n"
        "--                       -- #tắt trigger chấm công\n"
        "-- 6. Hàm Thực nhận:     -- #test hàm tính thực nhận  |  -- #sửa hàm thực nhận âm thì về 0  |  -- #tràn số 8115\n"
        "-- 7. Index Benchmark:   -- #đo hiệu năng index  |  -- #benchmark 10000 dòng bảng tạm\n"
        "-- 8. Concurrency Lock:  -- #concurrency cửa sổ 1  |  -- #concurrency cửa sổ 2  |  -- #xem session nào đang block\n"
        "-- 9. Ràng buộc & Code:  -- #test check tháng 13  |  -- #metadata  |  -- #xem code sp_ChotBangLuong\n"
        "-- 10. Truy vấn nâng cao:-- #thống kê quỹ lương theo phòng ban  |  -- #tìm nhân viên lương cao nhất  |  -- #reset"
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


FULL_DB_SCHEMA_CONTEXT = (
    "Bạn là chuyên gia T-SQL cho CSDL SQL Server `QuanLyNhanSuTienLuong`.\n"
    "1. 10 BẢNG & GIÁ TRỊ CHECK CONSTRAINT:\n"
    "- dbo.PHONGBAN(MaPB int PK, TenPB nvarchar, SoDienThoai varchar, TrangThai nvarchar IN (N'HOAT_DONG', N'NGUNG_HOAT_DONG')) [MaPB: 1001..1005]\n"
    "- dbo.CHUCVU(MaCV int PK, TenCV nvarchar, PhuCapChucVu decimal(18,2)>=0) [MaCV: 1001..1006]\n"
    "- dbo.NHANVIEN(MaNV int PK, HoTen nvarchar, NgaySinh date, GioiTinh nvarchar IN (N'Nam',N'Nữ',N'Khác'), CCCD varchar(12), DiaChi nvarchar, SoDienThoai varchar(10), Email varchar, NgayVaoLam date, LuongCoBan decimal(18,2)>0, MaPB int FK, MaCV int FK, TrangThai nvarchar IN (N'DANG_LAM_VIEC',N'NGHI_VIEC'), NgayNghiViec date) [MaNV: 1001..1010]\n"
    "- dbo.TAIKHOAN(MaTK int PK, MaNV int UNIQUE FK, TenDangNhap varchar UNIQUE, MatKhau varchar, VaiTro varchar IN ('DB_Admin','HR_Manager','Payroll_Officer','Employee'), TrangThai varchar IN ('HOAT_DONG','KHOA'), NgayTao datetime2, NgaySuaCuoi datetime2, SqlLogin sysname)\n"
    "- dbo.LICHSULUONG(MaLichSu int PK, MaNV int FK, LuongCu decimal(18,2), LuongMoi decimal(18,2), NgayCapNhat datetime2, GhiChu nvarchar)\n"
    "- dbo.CHAMCONG(MaChamCong int PK, MaNV int FK, NgayChamCong date, GioVao time, GioRa time, SoGioLam decimal(4,2) computed, TrangThai nvarchar IN (N'CO_MAT',N'DI_TRE',N'VE_SOM',N'VANG'), GhiChu nvarchar)\n"
    "- dbo.PHUCAPNHANVIEN(MaPCNV int PK, MaNV int FK, Thang int 1..12, Nam int>=2020, TenPhuCap nvarchar, SoTien decimal(18,2)>=0, NgayGhiNhan date, GhiChu nvarchar)\n"
    "- dbo.KHAUTRUNHANVIEN(MaKTNV int PK, MaNV int FK, Thang int 1..12, Nam int>=2020, TenKhauTru nvarchar, SoTien decimal(18,2)>=0, NgayGhiNhan date, LyDo nvarchar)\n"
    "- dbo.BANGLUONG(MaBangLuong int PK, Thang int 1..12, Nam int>=2020, NgayCongChuan int>0, TrangThai varchar IN ('CHUA_CHOT','DA_CHOT'), NgayTao datetime2, NgayChot datetime2) [MaBangLuong 1008: 9/2026 DA_CHOT; 1009: 10/2026 CHUA_CHOT]\n"
    "- dbo.CHITIETBANGLUONG(MaChiTiet int PK, MaBangLuong int FK, MaNV int FK, LuongCoBan decimal(18,2), NgayCongThucTe int, TienCong decimal(18,2), TongPhuCap decimal(18,2), TongKhauTru decimal(18,2), ThucNhan decimal(18,2))\n"
    "2. 6 VIEWS:\n"
    "- dbo.vw_BangLuongChiTiet(MaBangLuong, Thang, Nam, NgayCongChuan, TrangThaiBangLuong, NgayTaoBangLuong, NgayChot, MaChiTiet, MaNV, HoTen, TenPB, TenCV, LuongCoBan, NgayCongThucTe, TienCong, TongPhuCap, TongKhauTru, ThucNhan)\n"
    "- dbo.vw_PhieuLuongCaNhan(cùng cột với vw_BangLuongChiTiet, lọc RLS theo ORIGINAL_LOGIN()/SUSER_SNAME())\n"
    "- dbo.vw_NhanVien_PhongBan_ChucVu(MaNV, HoTen, NgaySinh, GioiTinh, CCCD, DiaChi, SoDienThoai, Email, NgayVaoLam, LuongCoBan, TrangThai, MaPB, TenPB, MaCV, TenCV, PhuCapChucVu, TenDangNhap, VaiTro, TrangThaiTaiKhoan)\n"
    "- dbo.vw_TongHopChamCongThang(MaNV, HoTen, Thang, Nam, SoNgayDiLam, SoLanDiTre, SoLanVeSom, SoNgayVang, TongSoGioLam)\n"
    "- dbo.vw_TongPhuCapThang(MaNV, HoTen, Thang, Nam, SoKhoanPhuCap, TongTienPhuCap)\n"
    "- dbo.vw_TongKhauTruThang(MaNV, HoTen, Thang, Nam, TongKhauTru)\n"
    "3. 5 SCALAR FUNCTIONS:\n"
    "- dbo.fn_TinhSoNgayCong(@MaNV int, @Thang int, @Nam int) RETURNS int\n"
    "- dbo.fn_TinhTienCong(@LuongCoBan decimal(18,2), @NgayCongChuan int, @NgayCongThucTe int) RETURNS decimal(18,2)\n"
    "- dbo.fn_TongPhuCap(@MaNV int, @Thang int, @Nam int) RETURNS decimal(18,2)\n"
    "- dbo.fn_TongKhauTru(@MaNV int, @Thang int, @Nam int) RETURNS decimal(18,2)\n"
    "- dbo.fn_TinhThucNhan(@TienCong decimal(18,2), @TongPhuCap decimal(18,2), @TongKhauTru decimal(18,2)) RETURNS decimal(18,2)\n"
    "4. STORED PROCEDURES:\n"
    "- dbo.sp_ChotBangLuong(@MaBangLuong int), dbo.sp_HuyChotBangLuong(@MaBangLuong int)\n"
    "- dbo.sp_TinhBangLuongThang(@Thang int, @Nam int, @NgayCongChuan int=26, @MaBangLuong int OUTPUT)\n"
    "- dbo.sp_XoaBangLuongChuaChot(@MaBangLuong int), dbo.sp_XoaKyLuongChuaChot(@Thang int, @Nam int)\n"
    "- dbo.sp_ThemNhanVien(@HoTen, @NgaySinh, @GioiTinh, @CCCD, @DiaChi, @SoDienThoai, @Email, @NgayVaoLam, @LuongCoBan, @MaPB, @MaCV, @TaoTaiKhoan, @TenDangNhap, @MatKhauSHA256, @VaiTro, @NewMaNV OUTPUT)\n"
    "- dbo.sp_GhiNhanChamCong(@MaNV, @NgayChamCong, @GioVao, @GioRa, @TrangThai, @GhiChu, @MaChamCong OUTPUT)\n"
    "- dbo.sp_ThemPhuCapNhanVien(@MaNV, @Thang, @Nam, @TenPhuCap, @SoTien, @NgayGhiNhan, @GhiChu, @MaPCNV OUTPUT)\n"
    "- dbo.sp_LayTaiKhoanHienTai(), dbo.sp_AdminSetStatus(@MaTK, @TrangThai), dbo.sp_AdminSetRole(@MaTK, @VaiTro), dbo.sp_AdminResetPassword(@MaTK, @Hash, @Password), dbo.sp_DBAProvisionIdentity(@Login, @Password, @Hash, @VaiTro, @MaNV)\n"
    "5. 9 TRIGGERS:\n"
    "- trg_BangLuong_KhongSuaKhiDaChot, trg_ChiTietLuong_KhongSuaKhiDaChot, trg_ChamCong_KhongSuaKhiDaChotLuong, trg_PhuCap_KhongSuaKhiDaChotLuong, trg_KhauTru_KhongSuaKhiDaChotLuong, trg_NhanVien_KhongXoaKhiDaPhatSinhLuong, trg_ChamCong_KiemTraGio, trg_ChamCong_KiemTraNhanVien, trg_NhanVien_LichSuLuong.\n"
    "6. ROLES & LOGINS:\n"
    "- 4 Roles: role_DBAdmin, role_HRManager, role_PayrollOfficer, role_Employee.\n"
    "- 5 Logins: gui_admin (MaNV 1001), gui_hr (MaNV 1002), gui_payroll (MaNV 1005), gui_a (MaNV 1008), gui_b (MaNV 1009).\n"
)


def _call_gemini_fallback(user_prompt: str) -> Optional[str]:
    api_key = _load_api_key()
    if not api_key:
        return None
    schema_prompt = (
        FULL_DB_SCHEMA_CONTEXT
        + "YÊU CẦU ĐẦU RA: Chỉ trả về câu lệnh T-SQL ngắn gọn, đúng tên bảng/cột/hàm/thủ tục/giá trị CHECK ở trên, trình bày xuống dòng theo từng mệnh đề (SELECT / FROM / WHERE / ORDER BY), kết thúc bằng dấu ; (KHÔNG bọc ```sql, TUYỆT ĐỐI KHÔNG viết bất kỳ dòng comment -- hay /* */ nào) cho yêu cầu: "
        + user_prompt
    )
    for model_name in ("gemini-3-flash-preview", "gemini-3.5-flash-lite"):
        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/{model_name}:generateContent?key={api_key}"
            body = {
                "contents": [{"parts": [{"text": schema_prompt}]}],
                "generationConfig": {"thinkingConfig": {"thinkingBudget": 0}},
            }
            payload = json.dumps(body).encode("utf-8")
            req = urllib.request.Request(url, data=payload, headers={"Content-Type": "application/json"})
            with urllib.request.urlopen(req, timeout=8.0) as resp:
                data = json.loads(resp.read().decode("utf-8"))
                text = data["candidates"][0]["content"]["parts"][0]["text"].strip()
                text = re.sub(r"^```(?:sql|tsql)?\s*", "", text, flags=re.IGNORECASE)
                text = re.sub(r"\s*```$", "", text)
                return text
        except Exception:
            continue
    return None
