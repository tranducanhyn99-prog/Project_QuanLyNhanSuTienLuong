# -*- coding: utf-8 -*-
"""
Full Professional Slide Generator for PeopleOS - Nhóm 06
Builds all 18 slides with Modern Tech Deck design, high contrast, readable typography,
RBAC matrix table, lifecycle flowcharts, before/after benchmark cards, and preserves 100% speaker notes.
"""

import os
import json
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE
from pptx.dml.color import RGBColor

# Load speaker notes
with open('all_notes.json', 'r', encoding='utf-8') as f:
    speaker_notes = json.load(f)

prs = Presentation()
prs.slide_width = Inches(13.333)
prs.slide_height = Inches(7.500)
blank_layout = prs.slide_layouts[6]

# Modern Color Palette
BG_PAGE = RGBColor(248, 250, 252)       # Slate 50
CARD_BG = RGBColor(255, 255, 255)       # Pure White
CARD_BORDER = RGBColor(226, 232, 240)   # Slate 200
TEXT_DARK = RGBColor(15, 23, 42)        # Slate 900
TEXT_BODY = RGBColor(30, 41, 59)        # Slate 800
TEXT_MUTED = RGBColor(100, 116, 139)    # Slate 500
PRIMARY = RGBColor(79, 70, 229)         # Indigo 600
PRIMARY_BG = RGBColor(238, 242, 255)    # Indigo 50
PRIMARY_BORDER = RGBColor(199, 210, 254)# Indigo 200
SUCCESS = RGBColor(16, 185, 129)        # Emerald 600
SUCCESS_BG = RGBColor(236, 253, 245)    # Emerald 50
SUCCESS_BORDER = RGBColor(167, 243, 208)
DANGER = RGBColor(239, 68, 68)          # Rose 600
DANGER_BG = RGBColor(254, 242, 242)     # Rose 50
DANGER_BORDER = RGBColor(254, 202, 202)
AMBER = RGBColor(217, 119, 6)           # Amber 600
AMBER_BG = RGBColor(254, 243, 199)      # Amber 50
AMBER_BORDER = RGBColor(253, 230, 138)
CYAN = RGBColor(8, 145, 178)            # Cyan 600
CYAN_BG = RGBColor(236, 254, 255)       # Cyan 50
CYAN_BORDER = RGBColor(165, 243, 252)

def set_slide_background(slide):
    bg_shape = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), prs.slide_width, prs.slide_height)
    bg_shape.fill.solid()
    bg_shape.fill.fore_color.rgb = BG_PAGE
    bg_shape.line.fill.background()

def add_header(slide, tag_text, title_text, slide_num):
    # Tag Pill
    tag_box = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(0.32), Inches(6.0), Inches(0.36))
    tag_box.fill.solid()
    tag_box.fill.fore_color.rgb = PRIMARY_BG
    tag_box.line.color.rgb = PRIMARY_BORDER
    tag_box.line.width = Pt(1)
    tf = tag_box.text_frame
    tf.word_wrap = True
    tf.vertical_anchor = MSO_ANCHOR.MIDDLE
    tf.margin_left = Inches(0.12)
    tf.margin_top = Inches(0.0)
    p = tf.paragraphs[0]
    p.text = tag_text.upper()
    p.font.name = 'Segoe UI'
    p.font.size = Pt(10.5)
    p.font.bold = True
    p.font.color.rgb = PRIMARY

    # Slide Title
    title_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.72), Inches(10.5), Inches(0.60))
    tf_t = title_box.text_frame
    tf_t.word_wrap = True
    tf_t.margin_left = Inches(0.0)
    tf_t.margin_top = Inches(0.0)
    pt = tf_t.paragraphs[0]
    pt.text = title_text
    pt.font.name = 'Segoe UI'
    pt.font.size = Pt(21)
    pt.font.bold = True
    pt.font.color.rgb = TEXT_DARK

    # Slide Number Pill
    num_box = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(11.8), Inches(0.38), Inches(0.75), Inches(0.34))
    num_box.fill.solid()
    num_box.fill.fore_color.rgb = CARD_BG
    num_box.line.color.rgb = CARD_BORDER
    num_box.line.width = Pt(1)
    tf_n = num_box.text_frame
    tf_n.vertical_anchor = MSO_ANCHOR.MIDDLE
    pn = tf_n.paragraphs[0]
    pn.text = f"{slide_num:02d}"
    pn.alignment = PP_ALIGN.CENTER
    pn.font.name = 'Segoe UI'
    pn.font.size = Pt(11)
    pn.font.bold = True
    pn.font.color.rgb = PRIMARY

def add_footer(slide, presenter_info):
    line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(6.92), Inches(11.733), Pt(1))
    line.fill.solid()
    line.fill.fore_color.rgb = CARD_BORDER
    line.line.fill.background()

    footer_box = slide.shapes.add_textbox(Inches(0.8), Inches(6.98), Inches(11.733), Inches(0.35))
    tf = footer_box.text_frame
    tf.margin_left = Inches(0.0)
    tf.margin_top = Inches(0.0)
    p = tf.paragraphs[0]
    p.text = f"Nhóm 06 · Đồ án Hệ Quản trị Cơ sở Dữ liệu (DBMS330284)  |  {presenter_info}"
    p.font.name = 'Segoe UI'
    p.font.size = Pt(9.5)
    p.font.color.rgb = TEXT_MUTED

def add_standard_card(slide, left, top, width, height, title, bullets, badge_num=None):
    card = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    card.fill.solid()
    card.fill.fore_color.rgb = CARD_BG
    card.line.color.rgb = CARD_BORDER
    card.line.width = Pt(1.25)
    
    tf = card.text_frame
    tf.word_wrap = True
    tf.margin_left = Inches(0.20)
    tf.margin_right = Inches(0.20)
    tf.margin_top = Inches(0.14)
    tf.margin_bottom = Inches(0.10)
    
    # Title
    p_title = tf.paragraphs[0]
    p_title.text = f"{badge_num}. {title}" if badge_num else title
    p_title.font.name = 'Segoe UI'
    p_title.font.size = Pt(13.5)
    p_title.font.bold = True
    p_title.font.color.rgb = PRIMARY
    p_title.space_after = Pt(6)
    
    # Bullets
    for b in bullets:
        p_b = tf.add_paragraph()
        p_b.space_after = Pt(3)
        p_b.font.name = 'Segoe UI'
        p_b.font.size = Pt(12)
        p_b.font.color.rgb = TEXT_BODY
        
        # Split on colon for bold lead-in
        if ':' in b:
            lead, rest = b.split(':', 1)
            run1 = p_b.add_run()
            run1.text = f"• {lead.strip()}: "
            run1.font.bold = True
            run1.font.color.rgb = TEXT_DARK
            run2 = p_b.add_run()
            run2.text = rest.strip()
            run2.font.bold = False
        else:
            run = p_b.add_run()
            run.text = f"• {b.strip()}"

def add_right_image_frame(slide, left, top, width, height, img_path, caption):
    frame = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left, top, width, height)
    frame.fill.solid()
    frame.fill.fore_color.rgb = CARD_BG
    frame.line.color.rgb = CARD_BORDER
    frame.line.width = Pt(1.25)
    
    pad_h = Inches(0.15)
    pad_top = Inches(0.15)
    cap_height = Inches(0.50)
    avail_w = width - pad_h * 2
    
    if os.path.exists(img_path):
        slide.shapes.add_picture(img_path, left + pad_h, top + pad_top, width=avail_w)
    
    cap_box = slide.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left + pad_h, top + height - cap_height, avail_w, Inches(0.36))
    cap_box.fill.solid()
    cap_box.fill.fore_color.rgb = PRIMARY_BG
    cap_box.line.color.rgb = PRIMARY_BORDER
    cap_box.line.width = Pt(1)
    tf_c = cap_box.text_frame
    tf_c.vertical_anchor = MSO_ANCHOR.MIDDLE
    p_c = tf_c.paragraphs[0]
    p_c.text = caption
    p_c.alignment = PP_ALIGN.CENTER
    p_c.font.name = 'Segoe UI'
    p_c.font.size = Pt(10)
    p_c.font.bold = True
    p_c.font.color.rgb = PRIMARY

def attach_notes(slide, slide_num):
    notes_text = speaker_notes.get(str(slide_num), "")
    if notes_text:
        slide.notes_slide.notes_text_frame.text = notes_text

# ==============================================================================
# SLIDE 1: TRANG BÌA CHÍNH THỨC
# ==============================================================================
s1 = prs.slides.add_slide(blank_layout)
set_slide_background(s1)

# HCMUTE Logo
logo_path = 'extracted_slide_images/slide_1_img_1.png'
if os.path.exists(logo_path):
    s1.shapes.add_picture(logo_path, Inches(0.8), Inches(0.55), width=Inches(1.05))

# Header University text
header_box = s1.shapes.add_textbox(Inches(2.1), Inches(0.55), Inches(10.4), Inches(1.10))
tf1 = header_box.text_frame
tf1.margin_left = Inches(0)
tf1.margin_top = Inches(0)
p1 = tf1.paragraphs[0]
p1.text = "TRƯỜNG ĐẠI HỌC SƯ PHẠM KỸ THUẬT TP. HỒ CHÍ MINH"
p1.font.name = 'Segoe UI'
p1.font.size = Pt(16)
p1.font.bold = True
p1.font.color.rgb = PRIMARY

p2 = tf1.add_paragraph()
p2.text = "KHOA CÔNG NGHỆ THÔNG TIN  •  BỘ MÔN HỆ THỐNG THÔNG TIN"
p2.font.name = 'Segoe UI'
p2.font.size = Pt(12)
p2.font.color.rgb = TEXT_MUTED
p2.font.bold = True

# Title Card
title_card = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.85), Inches(11.733), Inches(2.20))
title_card.fill.solid()
title_card.fill.fore_color.rgb = PRIMARY_BG
title_card.line.color.rgb = PRIMARY_BORDER
title_card.line.width = Pt(1.5)

tf_t = title_card.text_frame
tf_t.word_wrap = True
tf_t.vertical_anchor = MSO_ANCHOR.MIDDLE
tf_t.margin_left = Inches(0.3)
tf_t.margin_right = Inches(0.3)

pt1 = tf_t.paragraphs[0]
pt1.text = "BÁO CÁO ĐỒ ÁN MÔN HỌC HỆ QUẢN TRỊ CƠ SỞ DỮ LIỆU"
pt1.font.name = 'Segoe UI'
pt1.font.size = Pt(14)
pt1.font.bold = True
pt1.font.color.rgb = PRIMARY
pt1.space_after = Pt(4)

pt2 = tf_t.add_paragraph()
pt2.text = "HỆ THỐNG QUẢN LÝ NHÂN SỰ VÀ TIỀN LƯƠNG (PeopleOS)"
pt2.font.name = 'Segoe UI'
pt2.font.size = Pt(26)
pt2.font.bold = True
pt2.font.color.rgb = TEXT_DARK
pt2.space_after = Pt(8)

pt3 = tf_t.add_paragraph()
pt3.text = "Mã lớp: DBMS330284_01   |   GVHD: TS. PHAN THỊ THỂ   |   Học kỳ I (2026 - 2027)"
pt3.font.name = 'Segoe UI'
pt3.font.size = Pt(13)
pt3.font.bold = True
pt3.font.color.rgb = CYAN

# Member Grid / Table Card
member_card = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(4.25), Inches(11.733), Inches(2.45))
member_card.fill.solid()
member_card.fill.fore_color.rgb = CARD_BG
member_card.line.color.rgb = CARD_BORDER
member_card.line.width = Pt(1.25)

# Member Table inside card
table_shape = s1.shapes.add_table(6, 4, Inches(1.0), Inches(4.35), Inches(11.333), Inches(2.25))
table = table_shape.table
table.columns[0].width = Inches(0.6)
table.columns[1].width = Inches(2.5)
table.columns[2].width = Inches(1.4)
table.columns[3].width = Inches(6.833)

headers = ["STT", "Thành viên thực hiện", "MSSV", "Phân hệ đảm nhiệm & Vai trò"]
for col_idx, h in enumerate(headers):
    cell = table.cell(0, col_idx)
    cell.text = h
    cell.fill.solid()
    cell.fill.fore_color.rgb = PRIMARY_BG
    p = cell.text_frame.paragraphs[0]
    p.font.name = 'Segoe UI'
    p.font.size = Pt(11)
    p.font.bold = True
    p.font.color.rgb = PRIMARY
    p.alignment = PP_ALIGN.CENTER if col_idx < 3 else PP_ALIGN.LEFT

members_data = [
    ("1", "Nguyễn Minh Trí", "24110359", "TV1: Thiết kế CSDL 10 bảng (3NF), Module Nhân sự & Quản lý Transaction"),
    ("2", "Phạm Minh Quân", "24110311", "TV2: Phân hệ Chấm công theo ngày, Xử lý lô Batch JDBC & Covering Index"),
    ("3", "Trần Tiến Đạt", "24110198", "TV3: Phân hệ Phụ cấp & Khấu trừ, Xóa kỳ nháp & Trigger khóa nguồn"),
    ("4", "Nguyễn Quang Vinh", "24110385", "TV4: Nghiệp vụ Tính lương, Snapshot lịch sử, Savepoint & Payroll E2E"),
    ("5", "Trần Đức Anh", "24110155", "TV5: Kiến trúc 3 tầng, Phân quyền 4 Roles, Chốt lương & Xử lý đồng thời")
]

for row_idx, data in enumerate(members_data):
    for col_idx, val in enumerate(data):
        cell = table.cell(row_idx + 1, col_idx)
        cell.text = val
        cell.fill.solid()
        cell.fill.fore_color.rgb = CARD_BG if row_idx % 2 == 0 else BG_PAGE
        p = cell.text_frame.paragraphs[0]
        p.font.name = 'Segoe UI'
        p.font.size = Pt(10.5)
        p.font.color.rgb = TEXT_DARK if col_idx != 3 else TEXT_BODY
        p.font.bold = (col_idx == 1 or col_idx == 2)
        p.alignment = PP_ALIGN.CENTER if col_idx < 3 else PP_ALIGN.LEFT

add_footer(s1, "Báo cáo Đồ án cuối kỳ — Tháng 10/2026")
attach_notes(s1, 1)

# ==============================================================================
# Helper for Standard 2-Column Slides (Slides 2 to 14)
# ==============================================================================
def build_two_column_slide(slide_num, tag, title, presenter, cards_data, img_path, caption):
    s = prs.slides.add_slide(blank_layout)
    set_slide_background(s)
    add_header(s, tag, title, slide_num)
    
    # Left Column: 3 Cards
    col_w = Inches(5.7)
    left_x = Inches(0.8)
    top_y = Inches(1.40)
    card_gap = Inches(0.14)
    
    c1_h = Inches(1.68)
    c2_h = Inches(1.85)
    c3_h = Inches(1.55)
    
    c1 = cards_data[0]
    add_standard_card(s, left_x, top_y, col_w, c1_h, c1['title'], c1['bullets'], 1)
    
    c2 = cards_data[1]
    add_standard_card(s, left_x, top_y + c1_h + card_gap, col_w, c2_h, c2['title'], c2['bullets'], 2)
    
    c3 = cards_data[2]
    add_standard_card(s, left_x, top_y + c1_h + card_gap + c2_h + card_gap, col_w, c3_h, c3['title'], c3['bullets'], 3)
    
    # Right Column: Image Frame
    right_x = Inches(6.80)
    right_w = Inches(5.733)
    right_h = Inches(5.36)
    add_right_image_frame(s, right_x, top_y, right_w, right_h, img_path, caption)
    
    add_footer(s, presenter)
    attach_notes(s, slide_num)
    return s

# SLIDE 2: TỔNG QUAN ĐỀ TÀI
build_two_column_slide(
    2, "Tổng quan đề tài", "Tổng quan Bài toán & Kiến trúc Hệ thống PeopleOS",
    "TV1: Nguyễn Minh Trí — Đại diện nhóm",
    [
        {"title": "Mục tiêu bài toán quản lý", "bullets": [
            "Quản lý tập trung: 20 hồ sơ nhân sự, danh mục phòng ban, chức vụ, trạng thái công tác.",
            "Quy trình tiền lương khép kín: Chấm công hàng ngày -> Ghi nhận phụ cấp/khấu trừ -> Tính lương -> Chốt kỳ.",
            "Bảo đảm toàn vẹn dữ liệu: Không lặp công, khóa chỉnh sửa kỳ đã chốt bằng Trigger."
        ]},
        {"title": "Kiến trúc 3 tầng (Three-Tier Architecture)", "bullets": [
            "Presentation Layer: Java Swing PeopleOS Light Theme, tác vụ nền bất đồng bộ SwingWorker.",
            "Business / Data Access: Service điều phối giao dịch, DAO kết nối JDBC bảo mật.",
            "Database Layer: SQL Server 10 bảng đạt 3NF, 14 Stored Procedures, 5 Functions, 9 Triggers, 4 Roles."
        ]},
        {"title": "Kịch bản Demo thực tế", "bullets": [
            "Dữ liệu thực tế tháng 10/2026: 20 nhân sự đa phòng ban, Nhân viên A (730.000đ), Nhân viên B (0đ).",
            "Luồng trải nghiệm xuyên suốt qua 4 vai trò: Admin -> HR -> Payroll -> Employee."
        ]}
    ],
    'extracted_slide_images/slide_2_img_1.png', "Hình 2.1: Giao diện Tổng quan Dashboard PeopleOS"
)

# SLIDE 3: TV1 - Mô hình dữ liệu 10 bảng
build_two_column_slide(
    3, "Phân hệ Nhân sự — TV1 Nguyễn Minh Trí (24110359)", "Mô hình Dữ liệu 10 Bảng & Chuẩn hóa 3NF",
    "TV1: Nguyễn Minh Trí — 24110359",
    [
        {"title": "Cấu trúc 10 bảng nghiệp vụ", "bullets": [
            "Bảng danh mục & nhân sự: PHONGBAN, CHUCVU, NHANVIEN, TAIKHOAN.",
            "Bảng công tác & quyền lợi: CHAMCONG, PHUCAPNHANVIEN, KHAUTRUNHANVIEN.",
            "Bảng tiền lương: BANGLUONG, CHITIETBANGLUONG, LICHSULUONG."
        ]},
        {"title": "Khử phụ thuộc bắc cầu đạt chuẩn 3NF", "bullets": [
            "Tách PHONGBAN và CHUCVU độc lập: Khử phụ thuộc bắc cầu qua MaPB, MaCV.",
            "Tách riêng PHUCAP và KHAUTRU: Quản lý biến động theo tháng, hỗ trợ nợ/tạm ứng.",
            "Chuẩn hóa 3NF: Mọi thuộc tính không khóa đều phụ thuộc trực tiếp vào khóa chính."
        ]},
        {"title": "Snapshot lịch sử lương có chủ đích", "bullets": [
            "CHITIETBANGLUONG lưu snapshot: LuongCoBan, TienCong, TongPhuCap, TongKhauTru, ThucNhan.",
            "Bảo đảm tính bất biến: Khi nhân viên tăng lương hiện tại, bảng lương quá khứ không bị sai lệch."
        ]}
    ],
    'extracted_slide_images/slide_3_img_1.png', "Hình 3.1: Sơ đồ thực thể - liên kết (ERD) 10 bảng cơ sở dữ liệu"
)

# SLIDE 4: TV1 - Ràng buộc toàn vẹn & Transaction
build_two_column_slide(
    4, "Phân hệ Nhân sự — TV1 Nguyễn Minh Trí (24110359)", "Ràng buộc Toàn vẹn & Transaction Hồ sơ Nhân viên",
    "TV1: Nguyễn Minh Trí — 24110359",
    [
        {"title": "Ràng buộc toàn vẹn miền dữ liệu (Constraints)", "bullets": [
            "CHECK tuổi lao động: NgaySinh <= DATEADD(YEAR, -18, NgayVaoLam) (đủ 18 tuổi).",
            "CHECK định dạng chuẩn: CCCD đúng 12 chữ số, Số điện thoại đúng 10 số (đầu 0).",
            "Ràng buộc UNIQUE: CCCD, Số điện thoại, Email không trùng lặp toàn hệ thống."
        ]},
        {"title": "Procedure sp_ThemNhanVien & Transaction", "bullets": [
            "SET XACT_ABORT ON & TRY...CATCH: Tự động hoàn tác toàn bộ khi có lỗi vi phạm.",
            "Tự động ghi nhận lịch sử lương: Trigger trg_NhanVien_LichSuLuong chèn LICHSULUONG.",
            "Đồng bộ tài khoản ứng dụng: Tạo tự động thông tin TAIKHOAN liên kết nhân sự."
        ]},
        {"title": "Bảo vệ lịch sử công tác", "bullets": [
            "Cập nhật NGHI_VIEC: Tự động ghi nhận NgayNghiViec vào ngày hiện tại.",
            "Trigger trg_NhanVien_KhongXoaKhiDaPhatSinhLuong: Chặn xóa nhân viên đã có công/lương."
        ]}
    ],
    'extracted_slide_images/slide_4_img_1.png', "Hình 4.1: Màn hình Quản lý hồ sơ nhân viên (NhanVienPanel)"
)

# SLIDE 5: TV1 - Tối ưu hóa truy vấn & Chỉ mục
build_two_column_slide(
    5, "Phân hệ Nhân sự — TV1 Nguyễn Minh Trí (24110359)", "Tối ưu hóa Truy vấn & Chỉ mục Phân hệ Nhân sự",
    "TV1: Nguyễn Minh Trí — 24110359",
    [
        {"title": "Chỉ mục Nonclustered IX_NHANVIEN_HoTen", "bullets": [
            "Cột khóa tìm kiếm: HoTen phục vụ tra cứu nhanh theo tên nhân viên.",
            "Mệnh đề INCLUDE: MaNV, SoDienThoai, Email, MaPB, MaCV, TrangThai.",
            "Covering Index: Loại bỏ hoàn toàn thao tác Key Lookup về bảng gốc."
        ]},
        {"title": "Đo lường hiệu năng với STATISTICS IO/TIME", "bullets": [
            "Trước tối ưu (Clustered Scan): Đọc 75 logical reads trên bảng nhân sự.",
            "Sau tối ưu (Index Seek): Giảm xuống chỉ còn 10 logical reads (tối ưu 86.7%).",
            "Thời gian CPU: Giảm về ~0ms, phản hồi tức thì trên thanh tìm kiếm UI."
        ]},
        {"title": "Khung nhìn tổng hợp vw_NhanVien_PhongBan_ChucVu", "bullets": [
            "JOIN 3 bảng: NHANVIEN + PHONGBAN + CHUCVU.",
            "Phục vụ hiển thị danh sách nhân sự đầy đủ tên phòng, tên chức vụ trên giao diện."
        ]}
    ],
    'extracted_slide_images/slide_5_img_1.png', "Hình 5.1: Actual Execution Plan truy vấn nhân viên (75 -> 10 reads)"
)

# SLIDE 6: TV2 - Chấm công theo ngày
build_two_column_slide(
    6, "Phân hệ Chấm công — TV2 Phạm Minh Quân (24110311)", "Phân hệ Chấm công & Quy tắc Toàn vẹn theo Ngày",
    "TV2: Phạm Minh Quân — 24110311",
    [
        {"title": "Cấu trúc bảng dữ liệu CHAMCONG", "bullets": [
            "Khóa chính đơn: MaChamCong (IDENTITY INT).",
            "Khóa ngoại tham chiếu: MaNV liên kết bảng NHANVIEN.",
            "Ràng buộc duy nhất UQ_CHAMCONG_MaNV_Ngay: Mỗi nhân viên chỉ có 1 bản ghi mỗi ngày."
        ]},
        {"title": "Bộ ràng buộc toàn vẹn thời gian & trạng thái", "bullets": [
            "CHECK thời gian: NgayChamCong <= CAST(GETDATE() AS DATE) (không chấm tương lai).",
            "CHECK trạng thái: TrangThai IN ('CO_MAT', 'DI_TRE', 'VE_SOM', 'VANG').",
            "Trigger trg_ChamCong_KiemTraGio: Giờ ra GioRa bắt buộc phải lớn hơn giờ vào GioVao."
        ]},
        {"title": "Trigger bảo vệ nhân viên đã nghỉ việc", "bullets": [
            "Trigger trg_ChamCong_KiemTraNhanVien: Chặn chấm công nếu TrangThai <> 'DANG_LAM_VIEC'."
        ]}
    ],
    'extracted_slide_images/slide_6_img_1.png', "Hình 6.1: Giao diện Ghi nhận chấm công chi tiết (ChamCongPanel)"
)

# SLIDE 7: TV2 - Nhập công theo lô Batch JDBC
build_two_column_slide(
    7, "Phân hệ Chấm công — TV2 Phạm Minh Quân (24110311)", "Nhập công theo Lô & Xử lý Giao dịch Batch JDBC",
    "TV2: Phạm Minh Quân — 24110311",
    [
        {"title": "Thủ tục sp_GhiNhanChamCong", "bullets": [
            "Tham số đầu vào: MaNV, NgayChamCong, GioVao, GioRa, TrangThai, GhiChu.",
            "Tham số ngõ ra @MaChamCong OUTPUT: Trả về mã chấm công vừa được cấp phát.",
            "Kiểm tra tính hợp lệ: Báo lỗi chi tiết khi vi phạm quy tắc ca làm việc."
        ]},
        {"title": "Giao dịch Batch JDBC hiệu năng cao", "bullets": [
            "Sử dụng PreparedStatement.addBatch() và executeBatch() nhập công hàng loạt.",
            "Điều khiển giao dịch thủ công: setAutoCommit(false) bảo đảm tính nguyên tử.",
            "Tự động Rollback: Hoàn tác 100% nếu có bất kỳ bản ghi nào trong lô bị lỗi."
        ]},
        {"title": "Khung nhìn tổng hợp vw_TongHopChamCongThang", "bullets": [
            "Gom nhóm theo MaNV, Thang, Nam: Tính tổng ngày có mặt, đi trễ, về sớm, vắng.",
            "Cung cấp số liệu ngày công thực tế cho module tính lương của TV4."
        ]}
    ],
    'extracted_slide_images/slide_7_img_1.png', "Hình 7.1: Giao diện Tổng hợp công tháng & Chấm công theo lô"
)

# SLIDE 8: TV2 - Đo lường Covering Index Chấm công
build_two_column_slide(
    8, "Phân hệ Chấm công — TV2 Phạm Minh Quân (24110311)", "Đo lường Hiệu năng Chỉ mục Phân hệ Chấm công",
    "TV2: Phạm Minh Quân — 24110311",
    [
        {"title": "Chỉ mục Covering IX_CHAMCONG_Ngay_MaNV", "bullets": [
            "Khóa chỉ mục kép: (NgayChamCong, MaNV) tối ưu hóa truy vấn lọc theo ngày.",
            "Mệnh đề INCLUDE: (GioVao, GioRa, TrangThai) chứa đầy đủ dữ liệu hiển thị.",
            "Loại bỏ Key Lookup: Giảm tải đọc trang đĩa xuống mức tối thiểu."
        ]},
        {"title": "So sánh hiệu năng trước và sau tối ưu", "bullets": [
            "Trước tối ưu (Table Scan): Đọc 574 logical reads trên tập dữ liệu kiểm thử.",
            "Sau tối ưu (Index Seek): Giảm mạnh xuống còn 5 logical reads (tối ưu 99.1%).",
            "Execution Plan: Chuyển từ Clustered Scan sang Index Seek hoàn toàn."
        ]},
        {"title": "Ứng dụng thực tế trên ChamCongPanel", "bullets": [
            "Tải bảng chấm công 20 nhân viên trong tháng mượt mà, không giật lag giao diện."
        ]}
    ],
    'extracted_slide_images/slide_8_img_1.png', "Hình 8.1: Actual Execution Plan: Covering Index Seek (574 -> 5 reads)"
)

# SLIDE 9: TV3 - Phụ cấp & Khấu trừ
build_two_column_slide(
    9, "Phụ cấp & Khấu trừ — TV3 Trần Tiến Đạt (24110198)", "Quản lý Khoản Phụ cấp & Khấu trừ theo Kỳ lương",
    "TV3: Trần Tiến Đạt — 24110198",
    [
        {"title": "Phân loại phụ cấp chức vụ vs phụ cấp phát sinh", "bullets": [
            "Phụ cấp chức vụ cố định: Lưu tại CHUCVU.PhuCapChucVu (Giám đốc 5tr, Trưởng phòng 3tr...).",
            "Phụ cấp phát sinh theo kỳ: Lưu tại PHUCAPNHANVIEN (Ăn trưa 730k, dự án, trách nhiệm).",
            "Ràng buộc toàn vẹn: Thang (1-12), Nam (>= 2020), SoTien >= 0."
        ]},
        {"title": "Quản lý khấu trừ minh bạch", "bullets": [
            "Bảng KHAUTRUNHANVIEN: Ghi nhận tạm ứng lương, BHXH, phạt đi trễ kèm lý do cụ thể.",
            "Tự động tính toán tổng khấu trừ cho từng nhân viên trước khi tính lương thực nhận."
        ]},
        {"title": "Scalar Functions tổng hợp khoản tiền", "bullets": [
            "fn_TongPhuCap(@MaNV, @Thang, @Nam): Trả về tổng tiền phụ cấp trong kỳ.",
            "fn_TongKhauTru(@MaNV, @Thang, @Nam): Trả về tổng tiền khấu trừ trong kỳ."
        ]}
    ],
    'extracted_slide_images/slide_9_img_1.png', "Hình 9.1: Màn hình Quản lý Phụ cấp & Khấu trừ (PhuCapKhauTruPanel)"
)

# SLIDE 10: TV3 - Xóa kỳ nháp & Trigger khóa kỳ chốt
build_two_column_slide(
    10, "Phụ cấp & Khấu trừ — TV3 Trần Tiến Đạt (24110198)", "Xóa Kỳ lương Nháp & Trigger Bảo vệ Dữ liệu sau Chốt",
    "TV3: Trần Tiến Đạt — 24110198",
    [
        {"title": "Thủ tục sp_XoaKyLuongChuaChot an toàn", "bullets": [
            "Kiểm tra trạng thái kỳ lương: Chỉ cho phép xóa khi TrangThai = 'CHUA_CHOT'.",
            "Xóa tuần tự toàn vẹn: Xóa CHITIETBANGLUONG trước, sau đó xóa BANGLUONG.",
            "Từ chối tuyệt đối khi kỳ đã chốt: Bảo vệ dữ liệu sổ sách kế toán."
        ]},
        {"title": "Bộ 2 Triggers bảo vệ phụ cấp và khấu trừ", "bullets": [
            "trg_PhuCap_KhongSuaKhiDaChotLuong: Chặn INSERT/UPDATE/DELETE phụ cấp kỳ đã chốt.",
            "trg_KhauTru_KhongSuaKhiDaChotLuong: Chặn INSERT/UPDATE/DELETE khấu trừ kỳ đã chốt.",
            "Cơ chế Set-based: Dùng UNION giữa inserted và deleted kiểm tra toàn bộ dòng."
        ]},
        {"title": "Toàn vẹn dữ liệu sổ sách", "bullets": [
            "Ngăn chặn mọi hành vi can thiệp trái phép vào kỳ lương đã đóng."
        ]}
    ],
    'extracted_slide_images/slide_10_img_1.png', "Hình 10.1: Minh chứng Trigger từ chối thay đổi dữ liệu của kỳ đã chốt"
)

# SLIDE 11: TV3 - Đo lường Composite Index Phụ cấp
build_two_column_slide(
    11, "Phụ cấp & Khấu trừ — TV3 Trần Tiến Đạt (24110198)", "Đo lường Hiệu năng Chỉ mục Phụ cấp & Khấu trừ",
    "TV3: Trần Tiến Đạt — 24110198",
    [
        {"title": "Chỉ mục Composite IX_PHUCAP_MaNV_ThangNam", "bullets": [
            "Khóa phức hợp 3 cột: (MaNV, Thang, Nam) khớp chính xác điều kiện WHERE của UDF.",
            "Mệnh đề INCLUDE: (SoTien, TenPhuCap) triệt tiêu hoàn toàn Key Lookup."
        ]},
        {"title": "Kết quả đo lường thực tế trên SQL Server", "bullets": [
            "Trước tối ưu: Quét toàn bộ bảng phụ cấp với 642 logical reads.",
            "Sau tối ưu: Index Seek chính xác vào đúng kỳ lương với 2 logical reads.",
            "Tỷ lệ cải thiện: Giảm 99.7% số lượt đọc trang đĩa, CPU về 0ms."
        ]},
        {"title": "Tối ưu hóa tính toán bảng lương hàng loạt", "bullets": [
            "Khi sp_TinhBangLuongThang duyệt 20 nhân viên, tốc độ tính toán tăng gấp nhiều lần."
        ]}
    ],
    'extracted_slide_images/slide_11_img_1.png', "Hình 11.1: Bằng chứng Statistics IO giảm từ 642 xuống 2 logical reads"
)

# SLIDE 12: TV4 - Công thức tính lương & Đối soát Fixture
build_two_column_slide(
    12, "Tính toán Tiền lương — TV4 Nguyễn Quang Vinh (24110385)", "Công thức Tính lương & Đối soát Fixture Demo",
    "TV4: Nguyễn Quang Vinh — 24110385",
    [
        {"title": "Công thức tính lương gộp & lương thực nhận", "bullets": [
            "Tiền công: TienCong = (LuongCoBan / NgayCongChuan) * NgayCongThucTe.",
            "Thực nhận: ThucNhan = fn_TinhThucNhan(TienCong, TongPhuCap, TongKhauTru).",
            "Quy tắc kẹp sàn: Nếu khấu trừ > thu nhập thì ThucNhan được kẹp về 0đ (không âm)."
        ]},
        {"title": "Đối soát Fixture Demo tháng 10/2026", "bullets": [
            "Nhân viên A (Nguyễn Tuấn Anh): 1 công (1tr) + 730k phụ cấp - 1tr tạm ứng = 730.000đ.",
            "Nhân viên B (Trần Thị Bảo Bình): 0 công (0đ) + 0 phụ cấp - 0 khấu trừ = 0đ."
        ]},
        {"title": "Hàm vô hướng fn_TinhTienCong", "bullets": [
            "Kiểm tra đầu vào NULL, ngày công chuẩn <= 0 thì trả về 0; làm tròn 2 chữ số thập phân."
        ]}
    ],
    'extracted_slide_images/slide_12_img_1.png', "Hình 12.1: Màn hình Tính lương tháng 10/2026 và kết quả đối soát"
)

# SLIDE 13: TV4 - Thủ tục tính lương & Snapshot
build_two_column_slide(
    13, "Tính toán Tiền lương — TV4 Nguyễn Quang Vinh (24110385)", "Thủ tục Tính lương & Bảo tồn Snapshot Lịch sử",
    "TV4: Nguyễn Quang Vinh — 24110385",
    [
        {"title": "Stored Procedure sp_TinhBangLuongThang", "bullets": [
            "Khóa header bằng UPDLOCK/HOLDLOCK ngăn tranh chấp giữa hai kế toán.",
            "Kiểm tra dữ liệu nguồn: Bắt buộc đã có dữ liệu chấm công của tháng trước khi tính."
        ]},
        {"title": "Snapshot lịch sử lương có thứ tự ưu tiên", "bullets": [
            "Ưu tiên 1: Đọc từ LICHSULUONG với TuThang <= kỳ tính lương.",
            "Ưu tiên 2: Giữ mức lương cũ đã lưu trong CHITIETBANGLUONG nếu là kỳ quá khứ.",
            "Tránh sai lệch: Tăng lương hiện tại không làm thay đổi bảng lương các tháng trước."
        ]},
        {"title": "Giao dịch phân tầng với Savepoint", "bullets": [
            "Kiểm tra @@TRANCOUNT: Nếu caller đã có transaction thì tạo SAVEPOINT TV4_TinhBangLuong."
        ]}
    ],
    'extracted_slide_images/slide_13_img_1.png', "Hình 13.1: Hộp thoại điều chỉnh công và quy trình tính lại bảng lương"
)

# SLIDE 14: TV4 - Tích hợp JDBC & Payroll E2E
build_two_column_slide(
    14, "Tính toán Tiền lương — TV4 Nguyễn Quang Vinh (24110385)", "Tích hợp JDBC & Kiểm thử Toàn trình Payroll E2E",
    "TV4: Nguyễn Quang Vinh — 24110385",
    [
        {"title": "Kết nối JDBC qua BangLuongDAO", "bullets": [
            "Sử dụng CallableStatement gọi {CALL dbo.sp_TinhBangLuongThang(?, ?, ?, ?)}.",
            "Đăng ký tham số ngõ ra registerOutParameter(4, Types.INTEGER) lấy MaBangLuong."
        ]},
        {"title": "Kết quả kiểm thử E2E 10/10 PASS", "bullets": [
            "Kiểm tra công thức lương cơ bản, tính lại kỳ nháp, rollback khi có lỗi nguồn.",
            "Kiểm tra ngăn chặn tính lương trên kỳ đã chốt hoặc thiếu dữ liệu chấm công."
        ]},
        {"title": "Giao diện BangLuongPanel cập nhật thời gian thực", "bullets": [
            "Bảng tổng hợp kỳ lương và bảng chi tiết 20 nhân viên hiển thị đồng bộ."
        ]}
    ],
    'extracted_slide_images/slide_14_img_1.png', "Hình 14.1: Kết quả kiểm thử tự động 10 bước Payroll E2E đều đạt"
)

# ==============================================================================
# SLIDE 15: TV5 - BẢO MẬT 4 ROLES & MA TRẬN PHÂN QUYỀN (RBAC TABLE)
# ==============================================================================
s15 = prs.slides.add_slide(blank_layout)
set_slide_background(s15)
add_header(s15, "Bảo mật & Đồng thời — TV5 Trần Đức Anh (24110155)", "Kiến trúc Bảo mật & Phân quyền 4 Database Roles", 15)

# Left Column Top: Two-factor Auth Card
col_w = Inches(5.7)
left_x = Inches(0.8)
top_y = Inches(1.40)

c_auth = s15.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, top_y, col_w, Inches(1.30))
c_auth.fill.solid()
c_auth.fill.fore_color.rgb = CARD_BG
c_auth.line.color.rgb = CARD_BORDER
c_auth.line.width = Pt(1.25)
tf_au = c_auth.text_frame
tf_au.word_wrap = True
tf_au.margin_left = Inches(0.18)
tf_au.margin_top = Inches(0.12)
p = tf_au.paragraphs[0]
p.text = "1. Cơ chế Xác thực 2 Lớp (Two-Layer Security)"
p.font.name = 'Segoe UI'
p.font.size = Pt(13.5)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(4)

p_b1 = tf_au.add_paragraph()
p_b1.font.size = Pt(12)
p_b1.font.name = 'Segoe UI'
p_b1.font.color.rgb = TEXT_BODY
run1 = p_b1.add_run()
run1.text = "• Lớp 1 (Database Engine): "
run1.font.bold = True
run1.font.color.rgb = TEXT_DARK
run2 = p_b1.add_run()
run2.text = "Đăng nhập bằng SQL Login cá nhân, mapping qua TAIKHOAN.SqlLogin."

p_b2 = tf_au.add_paragraph()
p_b2.font.size = Pt(12)
p_b2.font.name = 'Segoe UI'
p_b2.font.color.rgb = TEXT_BODY
run3 = p_b2.add_run()
run3.text = "• Lớp 2 (Ứng dụng Java): "
run3.font.bold = True
run3.font.color.rgb = TEXT_DARK
run4 = p_b2.add_run()
run4.text = "Băm mật khẩu PBKDF2-HMAC-SHA256 (600.000 vòng, salt 16 bytes ngẫu nhiên)."

# Left Column Middle: RBAC Matrix Table Card
c_matrix = s15.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(2.82), col_w, Inches(2.75))
c_matrix.fill.solid()
c_matrix.fill.fore_color.rgb = CARD_BG
c_matrix.line.color.rgb = CARD_BORDER
c_matrix.line.width = Pt(1.25)

# Add Table for RBAC Matrix inside card
tb_rbac = s15.shapes.add_table(6, 5, left_x + Inches(0.12), Inches(2.94), Inches(5.46), Inches(2.50)).table
tb_rbac.columns[0].width = Inches(1.42)
tb_rbac.columns[1].width = Inches(1.04)
tb_rbac.columns[2].width = Inches(1.16)
tb_rbac.columns[3].width = Inches(0.86)
tb_rbac.columns[4].width = Inches(0.98)

rbac_headers = ["Đối tượng CSDL", "DBAdmin", "HRManager", "Payroll", "Employee"]
for c_i, h in enumerate(rbac_headers):
    c = tb_rbac.cell(0, c_i)
    c.margin_left = Inches(0.02)
    c.margin_right = Inches(0.02)
    c.text = h
    c.fill.solid()
    c.fill.fore_color.rgb = PRIMARY_BG
    p = c.text_frame.paragraphs[0]
    p.font.name = 'Segoe UI'
    p.font.size = Pt(9.5)
    p.font.bold = True
    p.font.color.rgb = PRIMARY
    p.alignment = PP_ALIGN.CENTER if c_i > 0 else PP_ALIGN.LEFT

matrix_rows = [
    ("Hồ sơ Nhân sự", "Toàn quyền", "CRUD", "SELECT", "DENY"),
    ("Chấm công", "Toàn quyền", "CRUD", "SELECT", "DENY"),
    ("Phụ cấp / Khấu trừ", "Toàn quyền", "Nhập liệu", "CRUD", "DENY"),
    ("Tính lương & Chốt", "Toàn quyền", "DENY", "EXECUTE", "DENY"),
    ("Xem phiếu lương", "Tất cả", "Tất cả", "Tất cả", "DÒNG RIÊNG (*)")
]

for r_i, r_data in enumerate(matrix_rows):
    for c_i, val in enumerate(r_data):
        c = tb_rbac.cell(r_i + 1, c_i)
        c.margin_left = Inches(0.02)
        c.margin_right = Inches(0.02)
        c.text = val
        c.fill.solid()
        c.fill.fore_color.rgb = CARD_BG if r_i % 2 == 0 else BG_PAGE
        p = c.text_frame.paragraphs[0]
        p.font.name = 'Segoe UI'
        p.font.size = Pt(9)
        p.alignment = PP_ALIGN.CENTER if c_i > 0 else PP_ALIGN.LEFT
        if val == "DENY":
            p.font.color.rgb = DANGER
            p.font.bold = True
        elif val == "Toàn quyền" or val == "CRUD" or val == "EXECUTE":
            p.font.color.rgb = SUCCESS
            p.font.bold = True
        elif "DÒNG RIÊNG" in val:
            p.font.color.rgb = PRIMARY
            p.font.bold = True
        else:
            p.font.color.rgb = TEXT_BODY

# Left Column Bottom: Row-Level Security Note Pill
c_rls = s15.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(5.68), col_w, Inches(1.08))
c_rls.fill.solid()
c_rls.fill.fore_color.rgb = PRIMARY_BG
c_rls.line.color.rgb = PRIMARY_BORDER
c_rls.line.width = Pt(1)
tf_rls = c_rls.text_frame
tf_rls.word_wrap = True
tf_rls.margin_left = Inches(0.18)
tf_rls.margin_top = Inches(0.10)
p = tf_rls.paragraphs[0]
p.text = "3. Row-Level Security với ORIGINAL_LOGIN()"
p.font.name = 'Segoe UI'
p.font.size = Pt(12)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(2)
p_rls2 = tf_rls.add_paragraph()
p_rls2.text = "• View vw_PhieuLuongCaNhan tự động lọc theo Login SQL thực tế. Nhân viên A (gui_a) tuyệt đối không thể xem phiếu lương của Nhân viên B dù can thiệp Session Java."
p_rls2.font.name = 'Segoe UI'
p_rls2.font.size = Pt(11)
p_rls2.font.color.rgb = TEXT_BODY

# Right Column Image
add_right_image_frame(s15, Inches(6.80), top_y, Inches(5.733), Inches(5.36),
                      'extracted_slide_images/slide_15_img_1.png',
                      "Hình 15.1: Màn hình Quản trị tài khoản & Ánh xạ Database Roles")

add_footer(s15, "TV5: Trần Đức Anh — 24110155")
attach_notes(s15, 15)

# ==============================================================================
# SLIDE 16: TV5 - QUY TRÌNH CHỐT LƯƠNG & XỬ LÝ ĐỒNG THỜI (WORKFLOW)
# ==============================================================================
s16 = prs.slides.add_slide(blank_layout)
set_slide_background(s16)
add_header(s16, "Bảo mật & Đồng thời — TV5 Trần Đức Anh (24110155)", "Quy trình Chốt lương & Kiểm soát Truy cập Đồng thời", 16)

# Left Column Top: Lifecycle Workflow (3 Step Cards)
c_wf_title = s16.shapes.add_textbox(left_x, Inches(1.35), col_w, Inches(0.35))
tf = c_wf_title.text_frame
tf.margin_left = Inches(0)
tf.margin_top = Inches(0)
p = tf.paragraphs[0]
p.text = "1. Vòng đời Chốt kỳ lương (Payroll Lifecycle Workflow)"
p.font.name = 'Segoe UI'
p.font.size = Pt(13.5)
p.font.bold = True
p.font.color.rgb = PRIMARY

# Step 1 Box: CHƯA CHỐT
step1 = s16.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(1.75), Inches(2.65), Inches(1.50))
step1.fill.solid()
step1.fill.fore_color.rgb = AMBER_BG
step1.line.color.rgb = AMBER_BORDER
step1.line.width = Pt(1.25)
tf1 = step1.text_frame
tf1.word_wrap = True
tf1.margin_left = Inches(0.12)
tf1.margin_top = Inches(0.10)
p = tf1.paragraphs[0]
p.text = "BƯỚC 1: KỲ LƯƠNG NHÁP"
p.font.name = 'Segoe UI'
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = AMBER
p.space_after = Pt(3)
p2 = tf1.add_paragraph()
p2.text = "• TrangThai = 'CHUA_CHOT'\n• Cho phép sửa công, phát sinh phụ cấp/khấu trừ.\n• Được phép tính lại lương nhiều lần."
p2.font.name = 'Segoe UI'
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_BODY

# Arrow 1 -> 2
arrow = s16.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, left_x + Inches(2.72), Inches(2.35), Inches(0.26), Inches(0.30))
arrow.fill.solid()
arrow.fill.fore_color.rgb = PRIMARY
arrow.line.fill.background()

# Step 2 Box: ĐÃ CHỐT
step2 = s16.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x + Inches(3.05), Inches(1.75), Inches(2.65), Inches(1.50))
step2.fill.solid()
step2.fill.fore_color.rgb = SUCCESS_BG
step2.line.color.rgb = SUCCESS_BORDER
step2.line.width = Pt(1.25)
tf2 = step2.text_frame
tf2.word_wrap = True
tf2.margin_left = Inches(0.12)
tf2.margin_top = Inches(0.10)
p = tf2.paragraphs[0]
p.text = "BƯỚC 2: ĐÃ CHỐT SỔ"
p.font.name = 'Segoe UI'
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = SUCCESS
p.space_after = Pt(3)
p2 = tf2.add_paragraph()
p2.text = "• TrangThai = 'DA_CHOT'\n• Gọi sp_ChotBangLuong + UPDLOCK\n• Kích hoạt 3 Triggers tự động chặn sửa Chấm công, Phụ cấp, Khấu trừ."
p2.font.name = 'Segoe UI'
p2.font.size = Pt(10)
p2.font.color.rgb = TEXT_BODY

# Left Column Middle: Mở lại kỳ lương
c_reopen = s16.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(3.38), col_w, Inches(1.35))
c_reopen.fill.solid()
c_reopen.fill.fore_color.rgb = CARD_BG
c_reopen.line.color.rgb = CARD_BORDER
c_reopen.line.width = Pt(1.25)
tf_ro = c_reopen.text_frame
tf_ro.word_wrap = True
tf_ro.margin_left = Inches(0.18)
tf_ro.margin_top = Inches(0.12)
p = tf_ro.paragraphs[0]
p.text = "2. Quy trình Mở lại kỳ lương có điều kiện (Reopen)"
p.font.name = 'Segoe UI'
p.font.size = Pt(13)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(4)
p_ro1 = tf_ro.add_paragraph()
p_ro1.font.size = Pt(11.5)
p_ro1.font.name = 'Segoe UI'
p_ro1.font.color.rgb = TEXT_BODY
run = p_ro1.add_run()
run.text = "• Thủ tục sp_HuyChotBangLuong: "
run.font.bold = True
run.font.color.rgb = TEXT_DARK
run2 = p_ro1.add_run()
run2.text = "Trả trạng thái về CHUA_CHOT và xóa NgayChot."
p_ro2 = tf_ro.add_paragraph()
p_ro2.font.size = Pt(11.5)
p_ro2.font.name = 'Segoe UI'
p_ro2.font.color.rgb = TEXT_BODY
run3 = p_ro2.add_run()
run3.text = "• Giới hạn quyền hạn: "
run3.font.bold = True
run3.font.color.rgb = TEXT_DARK
run4 = p_ro2.add_run()
run4.text = "Chỉ role_DBAdmin và role_PayrollOfficer được cấp quyền EXECUTE khi có lý do chính đáng."

# Left Column Bottom: Concurrency Control Card
c_conc = s16.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(4.85), col_w, Inches(1.90))
c_conc.fill.solid()
c_conc.fill.fore_color.rgb = CARD_BG
c_conc.line.color.rgb = CARD_BORDER
c_conc.line.width = Pt(1.25)
tf_cc = c_conc.text_frame
tf_cc.word_wrap = True
tf_cc.margin_left = Inches(0.18)
tf_cc.margin_top = Inches(0.12)
p = tf_cc.paragraphs[0]
p.text = "3. Xử lý Tranh chấp Đồng thời (Concurrency Control)"
p.font.name = 'Segoe UI'
p.font.size = Pt(13)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(4)
p_cc1 = tf_cc.add_paragraph()
p_cc1.font.size = Pt(11.5)
p_cc1.font.name = 'Segoe UI'
p_cc1.font.color.rgb = TEXT_BODY
run = p_cc1.add_run()
run.text = "• Hai phiên cùng tính lương: "
run.font.bold = True
run.font.color.rgb = TEXT_DARK
run2 = p_cc1.add_run()
run2.text = "Session B tự động chờ khóa Session A nhả ra. Kết quả: duy nhất 1 bảng lương, 0 dòng trùng, 0 header trùng."
p_cc2 = tf_cc.add_paragraph()
p_cc2.font.size = Pt(11.5)
p_cc2.font.name = 'Segoe UI'
p_cc2.font.color.rgb = TEXT_BODY
run3 = p_cc2.add_run()
run3.text = "• Chốt kỳ đối đầu sửa công: "
run3.font.bold = True
run3.font.color.rgb = TEXT_DARK
run4 = p_cc2.add_run()
run4.text = "Thao tác sửa công bị giữ lại và từ chối an toàn ngay khi thấy DA_CHOT (source data unchanged)."

# Right Column Image
add_right_image_frame(s16, Inches(6.80), top_y, Inches(5.733), Inches(5.36),
                      'extracted_slide_images/slide_16_img_1.png',
                      "Hình 16.1: Giao diện Báo cáo lương, Chốt kỳ & Mở lại bảng lương")

add_footer(s16, "TV5: Trần Đức Anh — 24110155")
attach_notes(s16, 16)

# ==============================================================================
# SLIDE 17: TV5 - ĐÁNH GIÁ HIỆU NĂNG & KIỂM THỬ TỰ ĐỘNG (BEFORE/AFTER CARDS)
# ==============================================================================
s17 = prs.slides.add_slide(blank_layout)
set_slide_background(s17)
add_header(s17, "Bảo mật & Đồng thời — TV5 Trần Đức Anh (24110155)", "Đánh giá Hiệu năng Chỉ mục & Kiểm thử Tự động", 17)

# Left Column Top: Before vs After Metric Cards
c_bm_title = s17.shapes.add_textbox(left_x, Inches(1.35), col_w, Inches(0.35))
tf = c_bm_title.text_frame
tf.margin_left = Inches(0)
tf.margin_top = Inches(0)
p = tf.paragraphs[0]
p.text = "1. Đo lường Chỉ mục IX_NHANVIEN_MaPB_MaCV (Benchmark 10.000 dòng)"
p.font.name = 'Segoe UI'
p.font.size = Pt(13)
p.font.bold = True
p.font.color.rgb = PRIMARY

# Before Card (Rose/Danger)
card_before = s17.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(1.75), Inches(2.78), Inches(2.10))
card_before.fill.solid()
card_before.fill.fore_color.rgb = DANGER_BG
card_before.line.color.rgb = DANGER_BORDER
card_before.line.width = Pt(1.25)
tf_bf = card_before.text_frame
tf_bf.word_wrap = True
tf_bf.margin_left = Inches(0.14)
tf_bf.margin_top = Inches(0.12)
p = tf_bf.paragraphs[0]
p.text = "TRƯỚC TỐI ƯU"
p.font.name = 'Segoe UI'
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = DANGER

p_op1 = tf_bf.add_paragraph()
p_op1.text = "Clustered Index Scan"
p_op1.font.size = Pt(13)
p_op1.font.bold = True
p_op1.font.color.rgb = TEXT_DARK
p_op1.space_after = Pt(4)

p_m1 = tf_bf.add_paragraph()
p_m1.text = "• Số dòng quét: 10.000 dòng\n• Logical Reads: 142 reads\n• Thời gian CPU: 3 ms\n• Tải đọc đĩa cao toàn bảng."
p_m1.font.size = Pt(11)
p_m1.font.name = 'Segoe UI'
p_m1.font.color.rgb = TEXT_BODY

# After Card (Emerald/Success)
card_after = s17.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x + Inches(2.92), Inches(1.75), Inches(2.78), Inches(2.10))
card_after.fill.solid()
card_after.fill.fore_color.rgb = SUCCESS_BG
card_after.line.color.rgb = SUCCESS_BORDER
card_after.line.width = Pt(1.25)
tf_af = card_after.text_frame
tf_af.word_wrap = True
tf_af.margin_left = Inches(0.14)
tf_af.margin_top = Inches(0.12)
p = tf_af.paragraphs[0]
p.text = "SAU TỐI ƯU (COVERING INDEX)"
p.font.name = 'Segoe UI'
p.font.size = Pt(11)
p.font.bold = True
p.font.color.rgb = SUCCESS

p_op2 = tf_af.add_paragraph()
p_op2.text = "Nonclustered Index Seek"
p_op2.font.size = Pt(13)
p_op2.font.bold = True
p_op2.font.color.rgb = TEXT_DARK
p_op2.space_after = Pt(4)

p_m2 = tf_af.add_paragraph()
p_m2.text = "• Số dòng đọc: Đúng 833 dòng\n• Logical Reads: 15 reads\n• Thời gian CPU: 1 - 2 ms\n• GIẢM 89.7% I/O READS!"
p_m2.font.size = Pt(11)
p_m2.font.name = 'Segoe UI'
p_m2.font.bold = True
p_m2.font.color.rgb = SUCCESS

# Left Column Bottom: Automated Test Suite Card
c_tests = s17.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, left_x, Inches(3.98), col_w, Inches(2.78))
c_tests.fill.solid()
c_tests.fill.fore_color.rgb = CARD_BG
c_tests.line.color.rgb = CARD_BORDER
c_tests.line.width = Pt(1.25)
tf_ts = c_tests.text_frame
tf_ts.word_wrap = True
tf_ts.margin_left = Inches(0.18)
tf_ts.margin_top = Inches(0.14)
p = tf_ts.paragraphs[0]
p.text = "2. Bộ Kiểm thử Tự động Toàn diện (257 Assertions PASS)"
p.font.name = 'Segoe UI'
p.font.size = Pt(13.5)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(6)

tests_bullets = [
    "SecurityRegressionTest: 104 assertions xác thực ma trận phân quyền 4 Roles, kiểm tra 22 permissions và 3 trường hợp bị từ chối.",
    "GuiQaRegressionTest: 88 assertions tự động kiểm tra toàn bộ vòng đời giao diện PeopleOS, focus, navigation và xử lý timeout.",
    "LightTheme & LoginChip: 65 assertions kiểm tra giao diện hiện đại và cơ chế dọn sạch mật khẩu trên bộ nhớ (fill zeros).",
    "Thực nghiệm Concurrency: Đo thời gian chờ khóa chốt thực tế (Công 1.10s, Phụ cấp 1.04s, Khấu trừ 1.04s, Chi tiết 1.03s)."
]

for tb in tests_bullets:
    p_t = tf_ts.add_paragraph()
    p_t.space_after = Pt(3)
    p_t.font.name = 'Segoe UI'
    p_t.font.size = Pt(11)
    p_t.font.color.rgb = TEXT_BODY
    lead, rest = tb.split(':', 1)
    r1 = p_t.add_run()
    r1.text = f"• {lead.strip()}: "
    r1.font.bold = True
    r1.font.color.rgb = TEXT_DARK
    r2 = p_t.add_run()
    r2.text = rest.strip()

# Right Column Image
add_right_image_frame(s17, Inches(6.80), top_y, Inches(5.733), Inches(5.36),
                      'extracted_slide_images/slide_17_img_1.png',
                      "Hình 17.1: Minh chứng kiểm tra tranh chấp đồng thời 2 Sessions trong SSMS")

add_footer(s17, "TV5: Trần Đức Anh — 24110155")
attach_notes(s17, 17)

# ==============================================================================
# SLIDE 18: TỔNG KẾT ĐỀ TÀI & LỜI CẢM ƠN (CONCLUSION & Q&A)
# ==============================================================================
s18 = prs.slides.add_slide(blank_layout)
set_slide_background(s18)

# Top Banner
add_header(s18, "Tổng kết đồ án môn học — Nhóm 06 (DBMS330284)", "Tổng kết Kết quả Đạt được & Sẵn sàng Demo Trực tiếp", 18)

# Top Stat Strip: 5 Metric Cards
stat_w = Inches(2.22)
stat_gap = Inches(0.15)
stat_top = Inches(1.40)
stat_h = Inches(0.95)

stats = [
    ("10 BẢNG", "Chuẩn hóa 3NF", PRIMARY, PRIMARY_BG, PRIMARY_BORDER),
    ("14 PROCEDURES", "Nghiệp vụ giao dịch", SUCCESS, SUCCESS_BG, SUCCESS_BORDER),
    ("9 TRIGGERS", "Bảo vệ toàn vẹn", AMBER, AMBER_BG, AMBER_BORDER),
    ("5 FUNCTIONS", "Tính toán & Tổng hợp", CYAN, CYAN_BG, CYAN_BORDER),
    ("4 ROLES", "Bảo mật đa tầng", PRIMARY, PRIMARY_BG, PRIMARY_BORDER)
]

for idx, (num, label, col, bg, bdr) in enumerate(stats):
    st_x = Inches(0.8) + idx * (stat_w + stat_gap)
    card_st = s18.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, st_x, stat_top, stat_w, stat_h)
    card_st.fill.solid()
    card_st.fill.fore_color.rgb = bg
    card_st.line.color.rgb = bdr
    card_st.line.width = Pt(1.25)
    
    tf_s = card_st.text_frame
    tf_s.vertical_anchor = MSO_ANCHOR.MIDDLE
    tf_s.margin_left = Inches(0.08)
    tf_s.margin_top = Inches(0.06)
    
    ps1 = tf_s.paragraphs[0]
    ps1.text = num
    ps1.alignment = PP_ALIGN.CENTER
    ps1.font.name = 'Segoe UI'
    ps1.font.size = Pt(14)
    ps1.font.bold = True
    ps1.font.color.rgb = col
    
    ps2 = tf_s.add_paragraph()
    ps2.text = label
    ps2.alignment = PP_ALIGN.CENTER
    ps2.font.name = 'Segoe UI'
    ps2.font.size = Pt(9.5)
    ps2.font.color.rgb = TEXT_MUTED

# Left Column (Bottom): 2 Cards (Results & Future Scope)
left_w = Inches(6.8)
left_top = Inches(2.50)

# Card Results
c_res = s18.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), left_top, left_w, Inches(2.35))
c_res.fill.solid()
c_res.fill.fore_color.rgb = CARD_BG
c_res.line.color.rgb = CARD_BORDER
c_res.line.width = Pt(1.25)
tf_r = c_res.text_frame
tf_r.word_wrap = True
tf_r.margin_left = Inches(0.20)
tf_r.margin_top = Inches(0.14)

p = tf_r.paragraphs[0]
p.text = "1. Các Kết quả Nổi bật Đạt được"
p.font.name = 'Segoe UI'
p.font.size = Pt(13.5)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(4)

res_bullets = [
    "Hoàn thành 100% đề cương: CSDL 10 bảng đạt 3NF, 14 Stored Procedures, 5 Functions, 9 Triggers, 6 Views, 5 Indexes.",
    "Bảo vệ toàn vẹn chặt chẽ: Trigger chặn can thiệp kỳ chốt, Savepoint phân tầng, kiểm soát đồng thời không trùng lặp.",
    "Bảo mật 2 lớp chuyên sâu: SQL Login + PBKDF2 600.000 vòng, Row-Level Security bảo vệ quyền riêng tư nhân viên.",
    "Ứng dụng PeopleOS hoàn thiện: Giao diện Light Theme hiện đại, kết nối thực tế mượt mà, đầy đủ dữ liệu 20 nhân sự."
]
for rb in res_bullets:
    p_rb = tf_r.add_paragraph()
    p_rb.space_after = Pt(2)
    p_rb.font.name = 'Segoe UI'
    p_rb.font.size = Pt(11)
    p_rb.font.color.rgb = TEXT_BODY
    lead, rest = rb.split(':', 1)
    r1 = p_rb.add_run()
    r1.text = f"• {lead.strip()}: "
    r1.font.bold = True
    r1.font.color.rgb = TEXT_DARK
    r2 = p_rb.add_run()
    r2.text = rest.strip()

# Card Future
c_fut = s18.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(4.98), left_w, Inches(1.78))
c_fut.fill.solid()
c_fut.fill.fore_color.rgb = CARD_BG
c_fut.line.color.rgb = CARD_BORDER
c_fut.line.width = Pt(1.25)
tf_f = c_fut.text_frame
tf_f.word_wrap = True
tf_f.margin_left = Inches(0.20)
tf_f.margin_top = Inches(0.14)

p = tf_f.paragraphs[0]
p.text = "2. Hạn chế & Định hướng Phát triển"
p.font.name = 'Segoe UI'
p.font.size = Pt(13.5)
p.font.bold = True
p.font.color.rgb = PRIMARY
p.space_after = Pt(4)

fut_bullets = [
    "Mở rộng phê duyệt đa cấp: Tích hợp luồng xét duyệt đơn nghỉ phép, tạm ứng lương nhiều cấp quản lý.",
    "Tự động hóa chính sách thuế: Tự động tính thuế TNCN lũy tiến từng phần và trích nộp quỹ bảo hiểm theo luật.",
    "Đa nền tảng Web & Mobile: Triển khai RESTful Web API để nhân viên tự chấm công qua GPS/khuôn mặt."
]
for fb in fut_bullets:
    p_fb = tf_f.add_paragraph()
    p_fb.space_after = Pt(2)
    p_fb.font.name = 'Segoe UI'
    p_fb.font.size = Pt(11)
    p_fb.font.color.rgb = TEXT_BODY
    lead, rest = fb.split(':', 1)
    r1 = p_fb.add_run()
    r1.text = f"• {lead.strip()}: "
    r1.font.bold = True
    r1.font.color.rgb = TEXT_DARK
    r2 = p_fb.add_run()
    r2.text = rest.strip()

# Right Column: Thank You & Demo Invitation Card
right_w = Inches(4.78)
right_x = Inches(7.75)
c_thank = s18.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, right_x, left_top, right_w, Inches(4.26))
c_thank.fill.solid()
c_thank.fill.fore_color.rgb = PRIMARY_BG
c_thank.line.color.rgb = PRIMARY_BORDER
c_thank.line.width = Pt(1.5)

tf_th = c_thank.text_frame
tf_th.word_wrap = True
tf_th.vertical_anchor = MSO_ANCHOR.MIDDLE
tf_th.margin_left = Inches(0.30)
tf_th.margin_right = Inches(0.30)

pth1 = tf_th.paragraphs[0]
pth1.text = "XIN TRÂN TRỌNG CẢM ƠN!"
pth1.alignment = PP_ALIGN.CENTER
pth1.font.name = 'Segoe UI'
pth1.font.size = Pt(22)
pth1.font.bold = True
pth1.font.color.rgb = PRIMARY
pth1.space_after = Pt(10)

pth2 = tf_th.add_paragraph()
pth2.text = "Nhóm 06 xin bày tỏ lòng biết ơn sâu sắc đến TS. Phan Thị Thể đã tận tình hướng dẫn và định hướng chuyên môn trong suốt học phần."
pth2.alignment = PP_ALIGN.CENTER
pth2.font.name = 'Segoe UI'
pth2.font.size = Pt(12)
pth2.font.color.rgb = TEXT_BODY
pth2.space_after = Pt(14)

pth3 = tf_th.add_paragraph()
pth3.text = "KÍNH MỜI QUÝ THẦY CÔ ĐẶT CÂU HỎI & CHUYỂN SANG PHẦN DEMO TRỰC TIẾP TRÊN HỆ THỐNG PEOPLEOS"
pth3.alignment = PP_ALIGN.CENTER
pth3.font.name = 'Segoe UI'
pth3.font.size = Pt(13)
pth3.font.bold = True
pth3.font.color.rgb = TEXT_DARK

add_footer(s18, "Nhóm 06 — DBMS330284  |  Báo cáo cuối kỳ Hoàn tất")
attach_notes(s18, 18)

# Save presentation
output_path = 'Bo_nop_Nhom06/slides/Nhom06_ThuyetTrinh_15_Slides.pptx'
prs.save(output_path)
print(f"Successfully generated 18 modern slides at: {output_path}")
