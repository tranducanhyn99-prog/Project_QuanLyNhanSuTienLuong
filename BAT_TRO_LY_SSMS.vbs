Option Explicit

Dim shell, files, projectFolder, daemonPath, checkOnly, argument
Dim interpreter, command, result
Set shell = CreateObject("WScript.Shell")
Set files = CreateObject("Scripting.FileSystemObject")
projectFolder = files.GetParentFolderName(WScript.ScriptFullName)
daemonPath = files.BuildPath(projectFolder, "tools\ssms_assistant\ssms_daemon.py")
checkOnly = False

For Each argument In WScript.Arguments
    If LCase(argument) = "--check" Then
        checkOnly = True
    Else
        Fail "Tham so khong hop le. Chi ho tro --check.", 2
    End If
Next

If Not files.FileExists(daemonPath) Then
    Fail "Khong tim thay tools\ssms_assistant\ssms_daemon.py trong thu muc du an.", 1
End If

interpreter = ResolvePython()
If IsEmpty(interpreter) Then
    Fail "Khong tim thay Python 3 cho Windows. Hay cai Python 3 kem Python Launcher, roi mo lai file nay.", 1
End If

shell.CurrentDirectory = projectFolder
command = Quote(interpreter(0)) & interpreter(1) & " " & Quote(daemonPath) & " --hidden"
result = RunHidden(command & " --check", True)
If result <> 0 Then
    Fail "Khong the khoi dong tro ly. Xem tools\ssms_assistant\assistant.log de biet chi tiet.", 1
End If
If checkOnly Then WScript.Quit 0

result = RunHidden(command, False)
If result <> 0 Then
    Fail "Khong the chay tro ly. Xem tools\ssms_assistant\assistant.log de biet chi tiet.", 1
End If
WScript.Quit 0

Function ResolvePython()
    Dim launchers, interpreters, folder, candidate, paths, probe
    Set launchers = CreateObject("Scripting.Dictionary")
    Set interpreters = CreateObject("Scripting.Dictionary")
    launchers.CompareMode = 1
    interpreters.CompareMode = 1
    paths = Split(shell.ExpandEnvironmentStrings("%PATH%"), ";")

    For Each folder In paths
        folder = Replace(Trim(folder), Chr(34), "")
        If Len(folder) > 0 Then
            AddCandidate launchers, files.BuildPath(folder, "pyw.exe")
            AddCandidate interpreters, files.BuildPath(folder, "pythonw.exe")
        End If
    Next
    AddCandidate launchers, shell.ExpandEnvironmentStrings("%WINDIR%\pyw.exe")

    ' Verify a native Windows Python without opening a console or installing hooks.
    probe = "import sys, ctypes; assert sys.version_info.major == 3; ctypes.WinDLL('user32')"
    For Each candidate In launchers.Keys
        If RunHidden(Quote(candidate) & " -3 -c " & Quote(probe), True) = 0 Then
            ResolvePython = Array(candidate, " -3")
            Exit Function
        End If
    Next
    For Each candidate In interpreters.Keys
        If RunHidden(Quote(candidate) & " -c " & Quote(probe), True) = 0 Then
            ResolvePython = Array(candidate, "")
            Exit Function
        End If
    Next
End Function

Sub AddCandidate(ByRef candidates, ByVal candidate)
    ' Ignore Microsoft Store execution aliases; use a verified native interpreter.
    If InStr(1, candidate, "\Microsoft\WindowsApps\", 1) = 0 Then
        If files.FileExists(candidate) And Not candidates.Exists(candidate) Then
            candidates.Add candidate, True
        End If
    End If
End Sub

Function RunHidden(ByVal text, ByVal wait)
    On Error Resume Next
    Err.Clear
    RunHidden = shell.Run(text, 0, wait)
    If Err.Number <> 0 Then RunHidden = -1
    On Error GoTo 0
End Function

Function Quote(ByVal text)
    Quote = Chr(34) & text & Chr(34)
End Function

Sub Fail(ByVal message, ByVal code)
    If Not checkOnly Then MsgBox message, vbExclamation, "Tro ly SSMS"
    WScript.Quit code
End Sub
