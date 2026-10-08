package com.test;

/** Manual UI testing starts at real authentication, with no synthetic admin session. */
public final class QuickTestLauncher {
    public static void main(String[] args) { com.ui.auth.LoginFrame.main(args); }
}
