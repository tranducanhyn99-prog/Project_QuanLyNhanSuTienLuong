# TV2 Benchmark Results Equivalence Verification

- **Run ID:** `run_20261013_140800`
- **Target Database:** `PRJ_Fix_QA_20261013_001534_ae185d81`
- **Session SPID:** `88`
- **Verification Timestamp:** `2026-10-13T14:08:05+07:00`
- **Methodology:** Exact duplicate-sensitive value-by-value comparison across all projected columns, duplicate multiplicity check, row counts, and cryptographic SHA-256 hash matching.

---

## 1. Detail Query Equivalence: Query 1.1 (BEFORE) vs Query 3.1 (AFTER)

- **Query:** Tra cứu chi tiết chấm công nhân viên 25 từ `2026-01-01` đến `2026-09-30`.
- **Projected Columns:** `MaNV`, `NgayChamCong`, `GioVao`, `GioRa`, `TrangThai` (5 cột).
- **Baseline Index:** `UQ_Bench_MaNV_Ngay` (Non-clustered Index Seek + Key Lookup trên Clustered Index `PK_Bench_ChamCong`).
- **Covering Index:** `IX_Bench_CHAMCONG_MaNV_Ngay` (Non-clustered Index Seek thuần, zero Key Lookup).
- **Row Counts:**
  - BEFORE (1.1): **273 rows**
  - AFTER (3.1): **273 rows**
  - Row Count Delta: **0** (Exact match)
- **Column Header Match:** `PASS` (`MaNV,NgayChamCong,GioVao,GioRa,TrangThai`)
- **Duplicate Multiplicity Check:** 273 distinct business dates for `MaNV = 25` in range `[2026-01-01, 2026-09-30]`, 0 duplicate keys, identical multiset cardinality.
- **Value Differences Found:** **0** (All 273 rows identical across all 5 columns).
- **Physical & Canonical Hashes:**
  - `TV2_BEFORE_1_1.csv` SHA-256: `76B576D6B0B4E081A7A7B039993A0BDF40A53A755B49ECA63BBCB83B015F7E31`
  - `TV2_AFTER_3_1.csv` SHA-256: `76B576D6B0B4E081A7A7B039993A0BDF40A53A755B49ECA63BBCB83B015F7E31`
  - Direct File Hash Equality: **TRUE (Bit-for-bit identical)**
- **Verification Verdict:** **PASS (100% EQUIVALENT)**

---

## 2. Aggregate Query Equivalence: Query 1.2 (BEFORE) vs Query 3.2 (AFTER)

- **Query:** Tổng hợp công và giờ làm nhân viên 25 theo tháng từ `2026-01-01` đến `2026-09-30` (mô phỏng view `vw_TongHopChamCongThang`).
- **Projected Columns:** `MaNV, Thang, Nam, SoNgayDiLam, SoLanDiTre, SoLanVeSom, SoNgayVang, TongSoGioLam` (8 cột).
- **Baseline Index:** `UQ_Bench_MaNV_Ngay` (Index Seek + Key Lookup + Stream Aggregate).
- **Covering Index:** `IX_Bench_CHAMCONG_MaNV_Ngay` (Index Seek + Stream Aggregate, zero Key Lookup).
- **Row Counts:**
  - BEFORE (1.2): **9 rows** (tháng 1 đến tháng 9 năm 2026)
  - AFTER (3.2): **9 rows** (tháng 1 đến tháng 9 năm 2026)
  - Row Count Delta: **0** (Exact match)
- **Column Header Match:** `PASS` (`MaNV,Thang,Nam,SoNgayDiLam,SoLanDiTre,SoLanVeSom,SoNgayVang,TongSoGioLam`)
- **Decimal Precision Match:** 100% exact on `TongSoGioLam` (`NUMERIC(..., 6)`: Tháng 1 = `284.333333`, Tháng 2 = `256.833333`, Tháng 3 = `283.833333`, Tháng 4 = `275.000000`, v.v.).
- **Value Differences Found:** **0** (All 9 rows identical across all 8 columns).
- **Physical & Canonical Hashes:**
  - `TV2_BEFORE_1_2.csv` SHA-256: `ADE24D339160184F3CB4D4DE23A1D748725075B624E1D21AFE5E569C2F67BB4F`
  - `TV2_AFTER_3_2.csv` SHA-256: `ADE24D339160184F3CB4D4DE23A1D748725075B624E1D21AFE5E569C2F67BB4F`
  - Direct File Hash Equality: **TRUE (Bit-for-bit identical)**
- **Verification Verdict:** **PASS (100% EQUIVALENT)**

---

## 3. Summary Equivalence Matrix

| Comparison Pair | Scope | Rows (Before) | Rows (After) | Value Differences | Null / Decimal Match | SHA-256 Match | Final Verdict |
|---|---|---:|---:|---:|---|---|---|
| **Query 1.1 ↔ 3.1** | Tra cứu chi tiết công nhân viên 25 | 273 | 273 | 0 | 100% match | Identical (`76B576...`) | **PASS** |
| **Query 1.2 ↔ 3.2** | Tổng hợp công theo tháng nhân viên 25 | 9 | 9 | 0 | 100% match | Identical (`ADE24D...`) | **PASS** |

> **Conclusion:** Việc thay thế `UQ_Bench_MaNV_Ngay` (Seek + Key Lookup) bằng covering index `IX_Bench_CHAMCONG_MaNV_Ngay` (Seek thuần) bảo toàn 100% tính toàn vẹn dữ liệu, không làm sai lệch bất kỳ giá trị, số dòng hay kết quả tổng hợp nào của nghiệp vụ.
