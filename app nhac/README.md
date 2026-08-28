# Ứng dụng Quản lý Thư Viện Nhạc

Ứng dụng quản lý thư viện nhạc cho phép quản lý về Người dùng, thông tin về Bài hát, Nghệ sĩ và Thể loại.

## Tính năng chính

* **Quản lý Bài hát:** Thêm, xem, sửa, xóa các bài hát.
* **Quản lý Nghệ sĩ:** Thêm, xem, sửa, xóa thông tin nghệ sĩ.
* **Quản lý Thể loại:** Thêm, xem, sửa, xóa các thể loại nhạc.
* **Tìm kiếm:** Tìm kiếm bài hát nhanh chóng theo tên.

---

## ️ Công nghệ sử dụng

* **Backend:**
    * Java 17
    * Spring Boot 3.x
    * Spring Data JPA (Hibernate)
* **Frontend (Server-side):**
    * Thymeleaf
    * HTML & CSS
    * Bootstrap 5
* **Database:**
    * Cloud MySQL (Aiven)

---

## Hướng dẫn chạy chương trình

Để chạy dự án này local, làm theo các bước dưới đây.

### 1. Yêu cầu Môi trường

Đảm bảo bạn đã cài đặt các công cụ sau trên hệ thống:

* Java JDK 17
* Maven
* Git
* Một IDE (IntelliJ IDEA, VS Code, ...)

### 2. Clone Repository

Sử dụng Git để sao chép dự án về máy:

https://github.com/tson304/OOP__N03_25_26_TSon_AQuan_ADuy

### 3. Cấu hình Database:

Mở file
`src/main/resources/application.properties`

Cập nhật thông tin kết nối
```properties
spring.datasource.url=jdbc:mysql://[YOUR_HOST]:[PORT]/[YOUR_DB_NAME]
spring.datasource.username=[YOUR_USERNAME]
spring.datasource.password=[YOUR_PASSWORD]

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 4. Chạy ứng dụng:
Tìm theo đường dẫn 
`src/main/java/com/musicmanager/web/MusicManagerApplication.java`

Chạy
`MusicManagerApplication.java`

Truy cập
http://localhost:8080
