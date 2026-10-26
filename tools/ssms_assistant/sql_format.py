"""Prepare SQL for the current SSMS database without damaging SQL literals."""
import re
from typing import Iterator, Tuple


def sql_spans(sql: str) -> Iterator[Tuple[str, int, int]]:
    """Yield code, literals, quoted identifiers and comments, including nested blocks."""
    index = code_start = 0
    size = len(sql)
    while index < size:
        start = index
        kind = ""
        if sql.startswith("--", index):
            kind = "comment"
            end = sql.find("\n", index)
            index = size if end < 0 else end
        elif sql.startswith("/*", index):
            kind = "comment"
            depth = 1
            index += 2
            while index < size and depth:
                if sql.startswith("/*", index):
                    depth += 1
                    index += 2
                elif sql.startswith("*/", index):
                    depth -= 1
                    index += 2
                else:
                    index += 1
        elif sql[index] in "'\"[":
            kind = "quoted"
            closing = "]" if sql[index] == "[" else sql[index]
            index += 1
            while index < size:
                if sql[index] == closing:
                    index += 1
                    if index < size and sql[index] == closing:
                        index += 1
                        continue
                    break
                index += 1
        else:
            index += 1
        if kind:
            if code_start < start:
                yield "code", code_start, start
            yield kind, start, index
            code_start = index
    if code_start < size:
        yield "code", code_start, size


def code_mask(sql: str) -> str:
    """Keep executable characters at their original offsets, mask protected spans."""
    parts = []
    for kind, start, end in sql_spans(sql):
        part = sql[start:end]
        parts.append(part if kind == "code" else re.sub(r"[^\r\n]", " ", part))
    return "".join(parts)


def materialize_payroll_exec_arguments(sql: str) -> str:
    """Use the known int payroll parameter for scalar SELECT arguments to EXEC."""
    mask = code_mask(sql)
    procedure = re.compile(
        r"\s+(?:(?:dbo|\[dbo\])\s*\.\s*)?"
        r"\[?sp_(?:HuyChot|Chot)BangLuong\]?\s+@MaBangLuong\s*=\s*\(",
        re.IGNORECASE,
    )
    edits = []
    variables = {value.lower() for value in re.findall(r"@\w+", mask)}
    for call in re.finditer(r"\bexec(?:ute)?\b", mask, re.IGNORECASE):
        argument = procedure.match(sql, call.end())
        if argument is None:
            continue
        opening = argument.end() - 1
        if not re.match(r"\s*select\b", mask[opening + 1:], re.IGNORECASE):
            continue
        depth, closing = 1, opening + 1
        while closing < len(mask) and depth:
            depth += (mask[closing] == "(") - (mask[closing] == ")")
            closing += 1
        if depth:
            continue
        # These installed procedures take exactly one integer parameter.
        if re.match(r"\s*(?:,|output\b)", mask[closing:], re.IGNORECASE):
            continue
        variable = "@ssms_MaBangLuong"
        suffix = 1
        while variable.lower() in variables:
            variable = f"@ssms_MaBangLuong{suffix}"
            suffix += 1
        variables.add(variable.lower())
        query = sql[opening + 1:closing - 1].strip()
        statement_end = closing
        semicolon = re.match(r"[ \t]*;", sql[closing:])
        if semicolon:
            statement_end += semicolon.end()
        replacement = (
            f"declare {variable} int;\n"
            f"select {variable} = ({query});\n"
            + sql[call.start():opening] + variable + ";"
        )
        preceding = mask[:call.start()].rstrip()
        if preceding and not preceding.endswith(";"):
            replacement = "begin\n" + replacement + "\nend;"
        edits.append((call.start(), statement_end, replacement))
    for start, end, replacement in reversed(edits):
        sql = sql[:start] + replacement + sql[end:]
    return sql


def strip_sql_comments(sql: str) -> str:
    parts = []
    for kind, start, end in sql_spans(sql):
        part = sql[start:end]
        parts.append(re.sub(r"[^\r\n]", " ", part) if kind == "comment" else part)
    return "".join(parts).strip()


_KEYWORDS = frozenset("""
    add all alter and any as asc authorization backup begin between break browse bulk
    by cascade case catch check checkpoint close clustered coalesce collate column commit
    constraint continue create cross current current_date current_time current_timestamp
    current_user cursor database dbcc deallocate declare default delete deny desc disable
    distinct distributed drop else enable end errlvl escape except exec execute exists
    exit external fetch file fillfactor for foreign freetext from full function go goto
    grant group having holdlock identity identity_insert identitycol if in index inner
    insert intersect into is join key kill left like lineno load merge national nocheck
    nonclustered not null nullif of off offsets on open opendatasource openquery openrowset
    openxml option or order outer over percent pivot plan precision primary print proc
    procedure public raiserror read readtext reconfigure references replication restore
    restrict return returns revert revoke right role rollback rowcount rowguidcol rule
    save schema securityaudit select session_user set setuser shutdown some statistics
    table tablesample textsize then throw to top tran transaction trigger truncate
    tsequal union unique unpivot update updatetext use user values varying view waitfor
    when where while with within writetext ties try output member login password
    check_policy no no_revert cookie instead after before include clustered nonclustered
    nocount xact_abort quoted_identifier ansi_nulls ansi_padding ansi_warnings arithabort
    concat_null_yields_null numeric_roundabort lock_timeout updlock readcommitted
    readuncommitted repeatableread serializable snapshot readpast rowlock tablock tablockx
    isolation level committed uncommitted delay io xml maxdop recompile
""".split())
_BUILTINS = frozenset("""
    abs avg cast ceiling char charindex concat concat_ws convert count count_big dateadd
    datediff datediff_big datefromparts datename datepart day db_id db_name error_line
    error_message error_number error_procedure error_severity error_state floor format
    getdate getutcdate has_perms_by_name ident_current isnull is_rolemember len lower
    ltrim max min month newid object_definition object_id object_name object_schema_name
    objectproperty original_login parsename patindex power quotename replace replicate
    round row_number rtrim schema_id schema_name scope_identity space string_agg stuff
    substring sum suser_id suser_name suser_sname sysdatetime trim try_cast try_convert
    type_id type_name unicode upper user_id user_name xact_state year
""".split())
_DATA_TYPES = frozenset("""
    bigint binary bit char date datetime datetime2 datetimeoffset decimal float image int
    money nchar ntext numeric nvarchar real smalldatetime smallint smallmoney sql_variant
    sysname text time timestamp tinyint uniqueidentifier varbinary varchar xml
""".split())
_TOKEN = re.compile(r"(?<![\w$#@])[@#]*[A-Za-z_][\w$#@]*|[(),;]")
_CONTEXTUAL = frozenset("""
    role login password output member ties try catch after before instead include
    check_policy no_revert cookie nocount xact_abort quoted_identifier ansi_nulls
    ansi_padding ansi_warnings arithabort concat_null_yields_null numeric_roundabort
    lock_timeout updlock readcommitted readuncommitted repeatableread serializable
    snapshot readpast rowlock tablock tablockx isolation level committed uncommitted
    delay io maxdop recompile statistics enable disable
""".split())


def _contextual_keyword(word: str, previous: str, suffix: str, prefix: str, calls: list,
                        module_header: bool) -> bool:
    statement = prefix.rsplit(";", 1)[-1]
    if word == "role":
        return previous in ("create", "alter", "drop")
    if word == "login":
        return previous in ("create", "alter", "drop", "for") or bool(
            re.search(r"\bexec(?:ute)?\s+as\s*$", statement, re.IGNORECASE)
        )
    if word in ("password", "check_policy"):
        return bool(re.search(r"\b(?:create|alter)\s+login\b", statement, re.IGNORECASE))
    if word == "output":
        return module_header or bool(re.search(r"\bexec(?:ute)?\b", statement, re.IGNORECASE))
    if word == "member":
        return previous in ("add", "drop")
    if word in ("try", "catch"):
        return previous in ("begin", "end")
    if word == "ties":
        return previous == "with"
    if word in ("enable", "disable"):
        return bool(re.match(r"trigger\b", suffix, re.IGNORECASE))
    if word in ("after", "before"):
        return bool(re.match(r"(?:insert|update|delete)\b", suffix, re.IGNORECASE))
    if word == "instead":
        return bool(re.match(r"of\b", suffix, re.IGNORECASE))
    if word == "include":
        return suffix.startswith("(") and bool(re.search(r"\bindex\b", prefix, re.IGNORECASE))
    if word in ("cookie", "no_revert"):
        return previous in ("with", "revert")
    if word == "statistics":
        return previous in ("set", "update")
    if word == "io":
        return previous == "statistics"
    if word == "isolation":
        return previous == "transaction"
    if word == "level":
        return previous == "isolation"
    if word in ("committed", "uncommitted"):
        return previous == "read"
    if word == "delay":
        return previous == "waitfor"
    return previous in ("set", "level") or bool(calls and calls[-1][0] in ("with", "option"))


def lowercase_sql_keywords(sql: str) -> str:
    """Change executable SQL syntax only; keep identifiers and literal bytes intact."""
    mask = code_mask(sql)
    replacements = []
    previous = ""
    previous_end = 0
    declaring = False
    module_header = False
    # Track calls to distinguish CAST types from aliases such as `AS Date`.
    calls = []
    for token in _TOKEN.finditer(mask):
        word = token.group()
        lower = word.lower()
        if word == "(":
            calls.append([previous.lower(), 0])
        elif word == ")":
            if calls:
                calls.pop()
        elif word == ",":
            if calls:
                calls[-1][1] += 1
        elif word == ";":
            declaring = module_header = False
            calls.clear()
        else:
            prefix, suffix = mask[:token.start()].rstrip(), mask[token.end():].lstrip()
            qualified = prefix.endswith((".", "::")) or suffix.startswith(".")
            identifier = word.startswith(("@", "#")) or qualified
            in_cast = bool(calls and calls[-1][0] in ("cast", "try_cast") and previous.lower() == "as")
            in_convert = bool(calls and calls[-1][0] in ("convert", "try_convert") and calls[-1][1] == 0)
            type_position = (
                previous.lower() == "returns" or in_cast or in_convert
                or (
                    previous.startswith("@") and not mask[previous_end:token.start()].strip()
                    and (module_header or (declaring and not calls))
                )
            )
            syntax = lower in _KEYWORDS or (
                lower == "object" and previous.lower() == "on" and suffix.startswith("::")
            )
            if lower in _CONTEXTUAL:
                syntax = _contextual_keyword(lower, previous.lower(), suffix, prefix, calls, module_header)
            if lower in _DATA_TYPES:
                syntax = type_position or (lower in ("time", "xml") and previous.lower() == "statistics")
            if lower == "max" and calls and calls[-1][0] in _DATA_TYPES:
                syntax = True
            builtin_call = lower in _BUILTINS and suffix.startswith("(")
            if not identifier and (syntax or builtin_call):
                replacements.append((token.start(), token.end(), lower))
            if not identifier and lower == "declare":
                declaring = True
            if not identifier and lower in ("function", "procedure", "proc"):
                module_header = True
            if not identifier and lower == "as" and not calls:
                module_header = False
        previous = word
        previous_end = token.end()
    for start, end, replacement in reversed(replacements):
        sql = sql[:start] + replacement + sql[end:]
    return sql


_MODULE_DDL = re.compile(
    r"\b(?:CREATE\s+(?:OR\s+ALTER\s+)?|ALTER\s+)"
    r"(?:VIEW|FUNCTION|PROC(?:EDURE)?|TRIGGER)\b", re.IGNORECASE,
)
def _declared_variables(mask: str) -> set:
    variables = set()
    for statement in re.finditer(r"\bDECLARE\b([^;]*)(?:;|$)", mask, re.IGNORECASE):
        variables.update(name.lower() for name in re.findall(
            r"(?:^|,)\s*(@\w+)\s+(?:AS\s+)?[\w\[]", statement.group(1), re.IGNORECASE,
        ))
    return variables


_GO = re.compile(r"^[ \t]*GO(?:[ \t]+([0-9]+))?[ \t]*\r?$", re.MULTILINE | re.IGNORECASE)
_USE = re.compile(
    r"(?:^[ \t]*|(?<=;)[ \t]*)USE\b[^;\r\n]*(?:;|(?=\r?$))",
    re.MULTILINE | re.IGNORECASE,
)


def prepare_sql(sql: str, clean_comments: bool = False) -> str:
    """Remove USE and redundant GO; keep DDL, repeat and variable-scope boundaries."""
    sql = sql.replace("\r\n", "\n")
    mask = code_mask(sql)
    for match in reversed(list(_USE.finditer(mask))):
        sql = sql[:match.start()] + sql[match.end():]

    mask = code_mask(sql)
    result = []
    current_batch_mask = ""
    offset = 0
    separators = list(_GO.finditer(mask))
    for index, separator in enumerate(separators):
        batch = sql[offset:separator.start()]
        batch_mask = mask[offset:separator.start()]
        current_batch_mask += batch_mask
        result.append(batch)
        next_end = separators[index + 1].start() if index + 1 < len(separators) else len(sql)
        next_mask = mask[separator.end():next_end]
        previous_vars = _declared_variables(current_batch_mask)
        next_vars = _declared_variables(next_mask)
        repeat = separator.group(1) is not None
        needed = (
            repeat or _MODULE_DDL.search(current_batch_mask)
            or _MODULE_DDL.search(next_mask)
            or previous_vars.intersection(next_vars)
        )
        if needed:
            result.append(sql[separator.start():separator.end()])
            current_batch_mask = ""
        offset = separator.end()
    result.append(sql[offset:])
    sql = "".join(result).strip()
    if clean_comments and code_mask(sql).strip():
        sql = strip_sql_comments(sql)
    return lowercase_sql_keywords(sql)
