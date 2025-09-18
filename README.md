<h2 align="center">
    <a href="https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin">
    🎓 Faculty of Information Technology (DaiNam University)
    </a>
</h2>
<h2 align="center">
    Client-Server chat application using TCP
</h2>
<div align="center">
    <p align="center">
        <img alt="AIoTLab Logo" width="170" src="https://github.com/user-attachments/assets/711a2cd8-7eb4-4dae-9d90-12c0a0a208a2" />
        <img alt="AIoTLab Logo" width="180" src="https://github.com/user-attachments/assets/dc2ef2b8-9a70-4cfa-9b4b-f6c2f25f1660" />
        <img alt="DaiNam University Logo" width="200" src="https://github.com/user-attachments/assets/77fe0fd1-2e55-4032-be3c-b1a705a1b574" />
    </p>

[![AIoTLab](https://img.shields.io/badge/AIoTLab-green?style=for-the-badge)](https://www.facebook.com/DNUAIoTLab)
[![Faculty of Information Technology](https://img.shields.io/badge/Faculty%20of%20Information%20Technology-blue?style=for-the-badge)](https://dainam.edu.vn/vi/khoa-cong-nghe-thong-tin)
[![DaiNam University](https://img.shields.io/badge/DaiNam%20University-orange?style=for-the-badge)](https://dainam.edu.vn)

</div>

---
<div style="text-align: left; padding: 10px;">
    <h2>Đề tài: Ứng dụng chat Client-Server sử dụng TCP</h2>
    
 <h3>1.Giới thiệu hệ thống</h3>
    <p style="font-size: 18px; line-height: 1.6;">
        Đây là hệ thống chat theo thời gian thực, cho phép đăng ký, đăng nhập, gửi tin nhắn văn bản, hình ảnh, file và nhãn dán giữa các user. Hệ thống cũng hiển thị trạng thái online/offline của người dùng, giúp việc trao đổi thông tin trở nên thuận tiện.
    </p>
    
 <p style="font-size: 18px; font-weight: bold;">Các chức năng chính:</p>
    <ul style="font-size: 16px; line-height: 1.6;">
        <li>Đăng ký và đăng nhập tài khoản người dùng</li>
        <li>Gửi/nhận tin nhắn văn bản giữa nhiều người dùng</li>
        <li>Gửi/nhận hình ảnh, file và nhãn dán (emoji/sticker)</li>
        <li>Hiển thị danh sách người dùng online/offline</li>
        <li>Thông báo khi người dùng đang gõ tin nhắn (typing indicator)</li>
        <li>Chat riêng lẻ hoặc chat nhóm (broadcast)</li>
    </ul>

  <p style="font-size: 16px; font-style: italic;">
  Ngôn ngữ lập trình: Java <br>
   - Giao thức mạng: TCP (Socket programming) <br>
   - Giao diện người dùng: Swing (Java GUI) <br>
   - Xử lý dữ liệu: ObjectInputStream/ObjectOutputStream <br>
   - Một số thư viện hỗ trợ: javax.swing, java.net, java.io, java.util.concurrent
    </p>
</div>



### 2.Ngôn ngữ & Công nghệ chính
<div align="left">
    
 Ngôn ngữ lập trình: Java
  - Giao thức mạng: TCP (Socket programming)
  - Giao diện người dùng: Swing (Java GUI)
  - Xử lý dữ liệu: ObjectInputStream/ObjectOutputStream
  - Một số thư viện hỗ trợ: javax.swing, java.net, java.io, java.util.concurrent
</div>

## 3. Hình ảnh các chức năng
<div align="center">
    <p align="center">
        <img src="docs/1.jpg" alt="" width="800"/><br/>
        <i>Hình 1: Chức năng đăng nhập<i><br/><br/>
        <img src="docs/2.jpg" alt="" width="800"/><br/>
         <i>Hình 2: Chức năng đăng ký<i><br/><br/>
        <img src="docs/3.jpg" alt="" width="800"/><br/>
        <i>Hình 3: Chức năng gửi sticker<i><br/><br/>
         <img src="docs/4.jpg" alt="" width="800"/><br/>
        <i>Hình 4: Chức năng gửi ảnh <i><br/><br/>
         <img src="docs/5.jpg" alt="" width="800"/><br/>
         <i>Hình 5: Chức năng gửi file<i><br/><br/>
    </p>
</div>


## 🚀 4.Mục tiêu đề 
Tạo môi trường chat thời gian thực, thân thiện với người dùng.
Học cách lập trình mạng với TCP, quản lý nhiều client song song.
Thực hành lập trình giao diện GUI và xử lý dữ liệu trong Java.

## 5. 📝 Các bước sử dụng ứng dụng chat TCP

<pre>
5.1. Khởi động chương trình
    - Bước 1: Chạy Server trước (server sẽ lắng nghe các kết nối).
    - Bước 2: Chạy Client (có thể mở nhiều client để nhiều người dùng chat với nhau).

5.2. Đăng ký tài khoản (lần đầu)
    - Nhấn nút Register trên giao diện client.
    - Nhập thông tin:
        + Username (tên tài khoản, duy nhất)
        + Password (mật khẩu)
    - (Tùy chọn) Chọn avatar để hiển thị.
    - Nhấn Xác nhận / Đăng ký → Nếu thành công sẽ hiện thông báo "Đăng ký thành công".

5.3. Đăng nhập
    - Nhập Username và Password đã đăng ký.
    - Nhấn Login → nếu đúng, hệ thống sẽ đưa bạn vào cửa sổ chat chính.

5.4.Giao diện chat chính
    - Danh sách người dùng: hiển thị tất cả tài khoản (online / offline).
    - Khu vực chat: nơi hiển thị tin nhắn gửi và nhận.
    - Khung nhập: nơi nhập nội dung tin nhắn.

5. Các nút chức năng
    - Gửi tin nhắn văn bản.
    - Gửi hình ảnh.
    - Gửi file.
    - Gửi emoji / sticker.
</pre>
---

## 6.Công nghệ bổ trợ
- Java Swing cho GUI.
- Socket TCP cho truyền dữ liệu.
- ObjectInputStream/ObjectOutputStream để truyền đối tượng Message.
- ConcurrentHashMap để quản lý danh sách user và trạng thái online.

## 7.Phương thức liên hệ
- Họ và tên: Nguyễn Thu Hồng
- Email: nguyenhongnunu@gmail.com
- Số điện thoại: 0853972752

