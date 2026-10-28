# Báo cáo kiểm thử hiệu năng ứng dụng quản lý thư viện nhạc bằng Apache JMeter

## 1. Mục tiêu

- Thực hành cấu hình Thread Group và đọc Summary Report trên Apache JMeter.
- Mô phỏng nhiều người dùng truy cập ứng dụng quản lý thư viện nhạc trực tuyến.
- Ghi nhận số request, thời gian phản hồi, tỷ lệ lỗi và thông lượng ở lần chạy được chụp.

## 2. Công cụ và môi trường kiểm thử

| Thành phần | Thông tin |
| --- | --- |
| Công cụ | Apache JMeter 5.6.3 |
| Ứng dụng | Music Library, Spring Boot |
| Địa chỉ kiểm thử | `http://localhost:8080` |
| Kịch bản | `app/MainProject/QuanLyThuVienNhacOnline/web/src/test/jmeter/music-library-load-test.jmx` |
| Loại thao tác | Đăng nhập và đọc dữ liệu |

Kịch bản sử dụng tài khoản kiểm thử đã đăng ký. Mỗi người dùng ảo giữ session riêng; các request đọc dữ liệu gồm trang chủ, thể loại, tìm kiếm thể loại, nghệ sĩ, bài hát và tìm kiếm bài hát.

## 3. Cấu hình Thread Group

Ảnh cấu hình trong JMeter hiển thị Thread Group **Music library visitors** với các thuộc tính:

| Thuộc tính | Giá trị trong Test Plan | Ý nghĩa |
| --- | --- | --- |
| Number of Threads (users) | `${__P(threads,20)}` | Số người dùng ảo; mặc định 20 |
| Ramp-up period | `${__P(rampup,10)}` giây | Thời gian khởi chạy đủ số luồng; mặc định 10 giây |
| Loop Count | Infinite | Lặp hành trình trong thời gian chạy |
| Same user on each iteration | Bật | Giữ cùng người dùng và session cho các vòng lặp |
| Specify Thread lifetime | Bật | Dừng theo Duration |
| Duration | `${__P(duration,180)}` giây | Thời lượng mặc định 180 giây |

Các giá trị `${__P(...)}` là giá trị mặc định của tham số JMeter, không nhất thiết là cấu hình thực tế của một lần chạy. Theo lệnh chạy đã cung cấp, lần chạy trong Summary Report dùng **120 luồng**, ramp-up **1 giây**, thời lượng cấu hình **15 giây**. Vì vậy cấu hình thực tế khác giá trị mặc định đang hiển thị trên ảnh Thread Group.

## 4. Kết quả Summary Report

Số liệu dưới đây được ghi lại từ ảnh Summary Report:

| Request | Samples | Average (ms) | Min (ms) | Max (ms) | Error % |
| --- | ---: | ---: | ---: | ---: | ---: |
| GET login page | 120 | 4 | 2 | 8 | 0.00% |
| POST login | 120 | 282 | 226 | 492 | 0.00% |
| R01 Home page | 120 | 4 | 2 | 8 | 0.00% |
| R02 Genre list | 120 | 9,291 | 1,482 | 16,757 | 0.00% |
| R03 Search genres | 31 | 13,656 | 5,427 | 15,228 | 0.00% |
| **TOTAL** | **511** | **3,078** | **2** | **16,757** | **0.00%** |

Thông lượng tổng trong ảnh khoảng **18.3 samples/giây**. Summary Report cũng hiển thị tốc độ nhận khoảng **107.92 KB/giây**, tốc độ gửi khoảng **4.68 KB/giây** và kích thước phản hồi trung bình khoảng **5,870.7 bytes**.

## 5. Nhận xét

- Các request xuất hiện trong bảng có Error `0.00%`; đây là kết quả theo các mẫu được ghi nhận và assertion của kịch bản.
- Thời gian trung bình của request danh sách thể loại là **9,291 ms**; tìm kiếm thể loại là **13,656 ms**. Đây là hai thao tác đọc chậm nhất trong các dòng đang hiển thị.
- Tổng có **511 samples**, nhưng Summary Report chỉ hiển thị 31 mẫu tìm kiếm thể loại và không có dòng cho danh sách nghệ sĩ, danh sách bài hát hoặc tìm kiếm bài hát. Do đó ảnh này chưa thể hiện đủ toàn bộ hành trình đã khai báo trong Test Plan.
- Kết quả này chỉ mô tả lần chạy được chụp. Không thể từ Error `0.00%` kết luận ứng dụng đáp ứng mọi mức tải hoặc mọi chức năng.
- Bảng không hiển thị P90/P95; không suy ra các percentile này từ cột Average.

## 6. Kết luận

Hai ảnh minh họa cấu hình Thread Group dạng tham số hóa và kết quả Summary Report của một lần chạy JMeter. Lần chạy ghi nhận 511 samples, Error `0.00%`, Average tổng **3,078 ms** và throughput khoảng **18.3 samples/giây**. Thời gian phản hồi của danh sách và tìm kiếm thể loại cao trong các mẫu hiển thị; cần kiểm tra thêm bằng một lần chạy hoàn chỉnh, đảm bảo tất cả sampler có mẫu trước khi đưa ra kết luận tổng quát về hiệu năng.
