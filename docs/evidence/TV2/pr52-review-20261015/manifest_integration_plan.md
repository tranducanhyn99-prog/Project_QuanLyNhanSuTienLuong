# PR 52 and PR 53 manifest comparison

Observed through read-only Git commands during this task:

- Feature branch: `fix/tv2-sp-try-catch`, HEAD `9982d28ef62af009c9f524cb0358ca193e2c285a`.
- Fetched `origin/main`: `1ea673804bd5caf9b1f1fa0a28db66dabddff8b8`, merge of PR 53.
- Common ancestor: `e00da71904a68000e3b67d7634853795e7576630`.
- All 537 entries of the current feature-branch package manifest matched working-tree file sizes and SHA256 hashes before implementation.
- `Bo_nop_Nhom06/SHA256_MANIFEST.csv`, Word and PowerPoint files have not been edited by this task.

Commands used:

```powershell
git merge-base HEAD origin/main
git diff e00da71904a68000e3b67d7634853795e7576630 HEAD -- Bo_nop_Nhom06/SHA256_MANIFEST.csv
git diff e00da71904a68000e3b67d7634853795e7576630 origin/main -- Bo_nop_Nhom06/SHA256_MANIFEST.csv
```

The feature branch (PR 52) changes only the TV2 SQL row in this manifest. PR 53 on main changed exactly four different rows (`README_BO_NOP.md`, `report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx`, `report/TV5_Ra_Soat_Bao_Cao.md`, and `source/SHA256_INVENTORY.csv`), and did NOT touch TV1 or the CSV header row. These observed hunks do not overlap; no integration operation was run, so this comparison is not an executed merge result. No merge or rebase is needed to preserve and deliver the current working-tree fixes on the existing branch.

If branch integration is performed later in an authorized terminal, retain the following TV2 row:

```csv
database/02_Module_ChamCong_TV2.sql,10064,4dbb78fc0b3946d4048fb779eb5111f10748bb81d7f608b6ddf1f69acacfd471
```

Also retain these PR 53 rows and their associated files from main:

```csv
README_BO_NOP.md,12115,81500da370a325ccf346d0fd860ec6513cb54224f3f4e6561d5d26ef9cc05043
report/Nhom_06_Bao_Cao_Cuoi_Ky_DBMS330284.docx,510333,be36924dfc08011cf6de02250f11f7b9dcd015e7f9829db6cc935cfada1dbf8e
report/TV5_Ra_Soat_Bao_Cao.md,8842,4501ded6c5ded24fcc3821e145037ad7393dfd26fb1c0f5b39d84c8908aaa384
source/SHA256_INVENTORY.csv,64680,73ae5fa13a4b3f2f6749813a672d0e05cbbca15c476cd2678fda5e0f676de79a
```

Do not resolve a future conflict by replacing the entire manifest with either side. Inspect any conflict, retain the independent changes, and recalculate all applicable package hashes and byte sizes after integration. Never overwrite TV5's newer report with the feature branch's older copy.
