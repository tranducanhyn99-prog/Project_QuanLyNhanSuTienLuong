"""Read schema metadata from an existing SQL Server database, without installing it."""

import os
import shutil
import subprocess
import threading
import time
from typing import Dict, Optional, Tuple


__all__ = ["load_schema_context", "get_last_schema_error"]

_CACHE_SECONDS = 60.0
_schema_cache: Dict[Tuple[str, str], Tuple[float, str]] = {}
_cache_lock = threading.Lock()
_last_schema_error = ""
_SCHEMA_COMPLETE = "SCHEMA_COMPLETE"


def get_last_schema_error() -> str:
    """Explain the last metadata failure without exposing raw process output."""
    return _last_schema_error


def _process_error_detail(stderr: str) -> str:
    message = stderr.lower()
    if any(marker in message for marker in ("timeout", "time out", "resource_semaphore", "memory grant")):
        return "Truy vấn metadata quá thời gian chờ; SQL Server có thể đang bận hoặc thiếu bộ nhớ."
    if any(marker in message for marker in ("login failed", "permission", "not authorized", "cannot open database")):
        return "Tài khoản Windows hiện tại không có quyền kết nối hoặc đọc metadata database."
    if any(marker in message for marker in ("provider", "connection", "server was not found", "certificate")):
        return "Không kết nối được server/database đã chọn bằng tài khoản Windows hiện tại."
    return "sqlcmd không đọc được đầy đủ metadata của database đã chọn."


def _sql_type(value_alias: str) -> str:
    """Render a catalog column/parameter type; aliases here are fixed internal SQL."""
    return f"""
    CASE WHEN t.is_user_defined = 1
        THEN QUOTENAME(SCHEMA_NAME(t.schema_id)) + N'.' + QUOTENAME(t.name)
        ELSE t.name END
    + CASE
        WHEN t.is_user_defined = 1 THEN N''
        WHEN t.name IN (N'varchar', N'char', N'varbinary', N'binary', N'nvarchar', N'nchar')
            THEN N'(' + CASE WHEN {value_alias}.max_length = -1 THEN N'max'
                ELSE CONVERT(nvarchar(10), CASE WHEN t.name IN (N'nvarchar', N'nchar')
                    THEN {value_alias}.max_length / 2 ELSE {value_alias}.max_length END) END + N')'
        WHEN t.name IN (N'decimal', N'numeric')
            THEN N'(' + CONVERT(nvarchar(10), {value_alias}.precision)
                + N',' + CONVERT(nvarchar(10), {value_alias}.scale) + N')'
        WHEN t.name IN (N'time', N'datetime2', N'datetimeoffset')
            THEN N'(' + CONVERT(nvarchar(10), {value_alias}.scale) + N')'
        ELSE N'' END
    """


# Each result row is a single nvarchar(max) value. Only catalog views are read:
# no table rows, object source code, credentials, or default values are requested.
# Bound catalog-join memory grants so small SQL Server instances do not wait on
# RESOURCE_SEMAPHORE despite the small number of metadata rows.
_SCHEMA_QUERY = """
SET NOCOUNT ON;
SELECT CAST(N'LIVE_DATABASE|' + QUOTENAME(DB_NAME()) AS nvarchar(max))
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'OBJECT|' + o.type_desc COLLATE DATABASE_DEFAULT + N'|'
    + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name) AS nvarchar(max))
FROM sys.objects AS o
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
WHERE o.is_ms_shipped = 0 AND o.type IN ('U', 'V', 'P', 'FN', 'IF', 'TF', 'TR')
ORDER BY o.type, s.name, o.name
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'COLUMN|' + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
    + N'|' + QUOTENAME(c.name) + N'|' + (""" + _sql_type("c") + """)
    + CASE WHEN c.is_nullable = 1 THEN N' NULL' ELSE N' NOT NULL' END
    + CASE WHEN c.is_identity = 1 THEN N' IDENTITY' ELSE N'' END
    + CASE WHEN c.is_computed = 1 THEN N' COMPUTED AS ' + ISNULL(cc.definition, N'(unavailable)')
        ELSE N'' END AS nvarchar(max))
FROM sys.columns AS c
JOIN sys.objects AS o ON o.object_id = c.object_id
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
JOIN sys.types AS t ON t.user_type_id = c.user_type_id
LEFT JOIN sys.computed_columns AS cc ON cc.object_id = c.object_id AND cc.column_id = c.column_id
WHERE o.is_ms_shipped = 0 AND o.type IN ('U', 'V', 'IF', 'TF')
ORDER BY s.name, o.name, c.column_id
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'PARAMETER|' + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
    + N'|' + CASE WHEN p.parameter_id = 0 THEN N'RETURNS' ELSE p.name END
    + N'|' + (""" + _sql_type("p") + """)
    + CASE WHEN p.parameter_id > 0 AND p.is_output = 1 THEN N' OUTPUT' ELSE N'' END
    + CASE WHEN p.is_readonly = 1 THEN N' READONLY' ELSE N'' END AS nvarchar(max))
FROM sys.parameters AS p
JOIN sys.objects AS o ON o.object_id = p.object_id
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
JOIN sys.types AS t ON t.user_type_id = p.user_type_id
WHERE o.is_ms_shipped = 0 AND o.type IN ('P', 'FN', 'IF', 'TF')
ORDER BY s.name, o.name, p.parameter_id
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'KEY|' + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
    + N'|' + QUOTENAME(k.name) + N'|' + k.type_desc COLLATE DATABASE_DEFAULT
    + N'|' + CONVERT(nvarchar(10), ic.key_ordinal) + N':' + QUOTENAME(c.name) AS nvarchar(max))
FROM sys.key_constraints AS k
JOIN sys.objects AS o ON o.object_id = k.parent_object_id
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
JOIN sys.index_columns AS ic ON ic.object_id = o.object_id AND ic.index_id = k.unique_index_id
JOIN sys.columns AS c ON c.object_id = ic.object_id AND c.column_id = ic.column_id
WHERE o.is_ms_shipped = 0 AND ic.key_ordinal > 0
ORDER BY s.name, o.name, k.name, ic.key_ordinal
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'CHECK|' + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
    + N'|' + QUOTENAME(ch.name) + N'|' + ch.definition
    + CASE WHEN ch.is_disabled = 1 THEN N' DISABLED' ELSE N'' END AS nvarchar(max))
FROM sys.check_constraints AS ch
JOIN sys.objects AS o ON o.object_id = ch.parent_object_id
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
WHERE o.is_ms_shipped = 0
ORDER BY s.name, o.name, ch.name
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'FOREIGN_KEY|' + QUOTENAME(ps.name) + N'.' + QUOTENAME(po.name)
    + N'|' + QUOTENAME(fk.name) + N'|' + QUOTENAME(pc.name)
    + N' -> ' + QUOTENAME(rs.name) + N'.' + QUOTENAME(ro.name) + N'.' + QUOTENAME(rc.name)
    + CASE WHEN fk.is_disabled = 1 THEN N' DISABLED' ELSE N'' END AS nvarchar(max))
FROM sys.foreign_keys AS fk
JOIN sys.foreign_key_columns AS fkc ON fkc.constraint_object_id = fk.object_id
JOIN sys.objects AS po ON po.object_id = fk.parent_object_id
JOIN sys.schemas AS ps ON ps.schema_id = po.schema_id
JOIN sys.columns AS pc ON pc.object_id = po.object_id AND pc.column_id = fkc.parent_column_id
JOIN sys.objects AS ro ON ro.object_id = fk.referenced_object_id
JOIN sys.schemas AS rs ON rs.schema_id = ro.schema_id
JOIN sys.columns AS rc ON rc.object_id = ro.object_id AND rc.column_id = fkc.referenced_column_id
WHERE po.is_ms_shipped = 0
ORDER BY ps.name, po.name, fk.name, fkc.constraint_column_id
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'TRIGGER|' + QUOTENAME(s.name) + N'.' + QUOTENAME(tr.name)
    + N'|ON ' + QUOTENAME(s.name) + N'.' + QUOTENAME(o.name)
    + CASE WHEN tr.is_disabled = 1 THEN N' DISABLED' ELSE N' ENABLED' END AS nvarchar(max))
FROM sys.triggers AS tr
JOIN sys.objects AS o ON o.object_id = tr.parent_id
JOIN sys.schemas AS s ON s.schema_id = o.schema_id
WHERE tr.is_ms_shipped = 0 AND tr.parent_class = 1
ORDER BY s.name, tr.name
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'ROLE|' + QUOTENAME(name) AS nvarchar(max))
FROM sys.database_principals
WHERE type = 'R'
ORDER BY name
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);

SELECT CAST(N'SCHEMA_COMPLETE' AS nvarchar(max))
OPTION (MAXDOP 1, MAX_GRANT_PERCENT = 1);
"""


def load_schema_context(server: Optional[str] = None, database: Optional[str] = None) -> Optional[str]:
    """Return recent, verified catalog metadata, or None if it cannot be read.

    Explicit targets override SSMS_SQL_SERVER and SSMS_SQL_DATABASE settings.
    Authentication always uses the current Windows account, independently of SSMS.
    """
    global _last_schema_error
    _last_schema_error = ""
    server = (server if server is not None else os.environ.get("SSMS_SQL_SERVER", "")).strip() or "localhost"
    database = (database if database is not None else os.environ.get("SSMS_SQL_DATABASE", "")).strip() or "QuanLyNhanSuTienLuong"
    if any(char in server + database for char in ("\0", "\r", "\n")):
        _last_schema_error = "Tên server/database chứa ký tự không hợp lệ."
        return None
    target = (server, database)
    with _cache_lock:
        cached = _schema_cache.get(target)
        if cached is not None and time.monotonic() - cached[0] < _CACHE_SECONDS:
            return cached[1]

    sqlcmd = shutil.which("sqlcmd")
    if not sqlcmd:
        _last_schema_error = "Không tìm thấy sqlcmd trong PATH; cần cài sqlcmd để đọc schema database hiện có."
        return None
    try:
        result = subprocess.run(
            [sqlcmd, "-S", server, "-d", database, "-E", "-C", "-l", "3", "-t", "3",
             "-f", "65001", "-y", "0", "-w", "65535", "-b", "-r", "1",
             "-Q", _SCHEMA_QUERY],
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=5,
            shell=False,
            creationflags=getattr(subprocess, "CREATE_NO_WINDOW", 0),
        )
    except subprocess.TimeoutExpired:
        _last_schema_error = "Truy vấn metadata quá thời gian chờ; SQL Server có thể đang bận hoặc thiếu bộ nhớ."
        return None
    except (OSError, subprocess.SubprocessError):
        _last_schema_error = "Không chạy được sqlcmd để đọc schema database hiện có."
        return None
    if result.returncode != 0 or "sqlcmd: error:" in result.stderr.lower():
        _last_schema_error = _process_error_detail(result.stderr)
        return None
    lines = [line.strip() for line in result.stdout.lstrip("\ufeff").splitlines() if line.strip()]
    if not lines or not lines[0].startswith("LIVE_DATABASE|") or lines[-1] != _SCHEMA_COMPLETE:
        _last_schema_error = "Metadata trả về chưa đầy đủ; sqlcmd chưa hoàn tất truy vấn schema."
        return None
    lines.pop()
    if not any(line.startswith("OBJECT|") for line in lines):
        _last_schema_error = "Không thấy đối tượng trong database; kiểm tra database đã chọn và quyền đọc metadata của tài khoản Windows."
        return None
    context = (
        "VERIFIED LIVE SQL SERVER SCHEMA (catalog metadata, cached for at most 60 seconds).\n"
        f"Target server: {server}; target database: {database}.\n"
        "These objects already exist; no installation or database creation is required.\n"
        "No data rows or example IDs are provided. Select existing rows when an ID is needed.\n"
        "Metadata is limited to objects visible to the current Windows account.\n"
        + "\n".join(lines)
    )
    with _cache_lock:
        _schema_cache[target] = (time.monotonic(), context)
    return context
