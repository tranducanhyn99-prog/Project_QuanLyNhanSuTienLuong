-- Reuse the maintained executable two-session payroll test, with assertions and cleanup.
-- This file intentionally reports no PASS: run BOTH scripts below on the QA database.
-- Session A: database/tests/TV4_Payroll_Concurrency_SessionA.sql
-- Session B: database/tests/TV4_Payroll_Concurrency_SessionB.sql
-- Start B when A prints "SESSION A DA GIU LOCK". A verifies one header, consistent
-- detail values, an observed wait, and zero leftover fixtures. Both processes must exit 0.
-- Closing payroll uses dbo.sp_ChotBangLuong @MaBangLuong=<id>; no NguoiChot column exists.
THROW 53210,N'Run both executable TV4 concurrency scripts; this routing file is not a test result.',1;
