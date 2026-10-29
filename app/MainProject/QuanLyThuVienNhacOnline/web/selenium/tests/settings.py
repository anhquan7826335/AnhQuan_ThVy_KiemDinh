"""Cấu hình chung cho bộ test Selenium (đọc từ biến môi trường)."""
import os
import uuid

# Địa chỉ ứng dụng đang chạy (mvn spring-boot:run -> cổng 8080)
BASE_URL = os.getenv("BASE_URL", "http://localhost:8080").rstrip("/")

# Tài khoản ADMIN có sẵn trong DB (không thể tạo qua form đăng ký vì role bị ép về User).
# Không đặt -> các test có marker `admin` tự động bị bỏ qua.
ADMIN_USER = os.getenv("ADMIN_USER")
ADMIN_PASSWORD = os.getenv("ADMIN_PASSWORD")

# Mật khẩu dùng cho các tài khoản test tự tạo (>= 8 ký tự theo quy tắc của hệ thống)
PASSWORD = "Selenium@123"


def unique(prefix: str = "sel") -> str:
    """Chuỗi duy nhất cho mỗi lần chạy để dữ liệu test không đụng dữ liệu thật/lần chạy trước."""
    return f"{prefix}{uuid.uuid4().hex[:8]}"
