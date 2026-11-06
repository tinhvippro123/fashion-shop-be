package com.fashionshop.be.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    /**
     * Gửi email OTP xác thực cho Đăng ký hoặc Quên mật khẩu.
     * @return true nếu gửi SMTP thật thành công, false nếu chạy chế độ fallback/mô phỏng.
     */
    public boolean sendOtpEmail(String toEmail, String otpCode, String actionTitle, String actionDesc) {
        log.info("[LUXE EMAIL OTP] Mã xác thực {} cho {} là: {}", actionTitle, toEmail, otpCode);

        if (mailSender == null || fromEmail == null || fromEmail.isBlank()) {
            log.warn("[LUXE EMAIL] Chưa cấu hình spring.mail.username, chạy chế độ mô phỏng OTP");
            return false;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(new InternetAddress(fromEmail, "LUXE Fashion"));
            helper.setTo(toEmail);
            helper.setSubject("[LUXE Fashion] " + actionTitle + " - Mã OTP: " + otpCode);

            String htmlContent = buildOtpHtmlContent(toEmail, otpCode, actionTitle, actionDesc);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            log.info("[LUXE EMAIL] Đã gửi email OTP thành công tới: {}", toEmail);
            return true;
        } catch (Exception e) {
            log.error("[LUXE EMAIL] Lỗi khi gửi email thật qua SMTP: {}. Hệ thống tiếp tục với mã OTP fallback.", e.getMessage());
            return false;
        }
    }

    private String buildOtpHtmlContent(String toEmail, String otpCode, String actionTitle, String actionDesc) {
        return "<!DOCTYPE html>"
                + "<html lang='vi'>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background-color:#f4f5f7;font-family:Arial,Helvetica,sans-serif;color:#333;'>"
                + "  <table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f5f7;padding:30px 15px;'>"
                + "    <tr><td align='center'>"
                + "      <table width='100%' max-width='560px' cellpadding='0' cellspacing='0' style='max-width:560px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.06);'>"
                + "        <!-- Header -->"
                + "        <tr><td style='background:#111111;padding:24px 30px;text-align:center;'>"
                + "          <h1 style='margin:0;font-size:24px;letter-spacing:4px;color:#ffffff;font-weight:700;'>LUXE FASHION</h1>"
                + "          <p style='margin:4px 0 0;font-size:11px;color:#c5a880;letter-spacing:2px;text-transform:uppercase;'>Đẳng Cấp Thời Trang Hiện Đại</p>"
                + "        </td></tr>"
                + "        <!-- Body -->"
                + "        <tr><td style='padding:35px 30px;'>"
                + "          <h2 style='margin:0 0 16px;font-size:20px;color:#111111;font-weight:700;'>" + actionTitle + "</h2>"
                + "          <p style='margin:0 0 20px;font-size:14px;line-height:22px;color:#555555;'>"
                + "            Xin chào <strong>" + toEmail + "</strong>,<br/>"
                +              actionDesc + ". Vui lòng sử dụng mã xác thực OTP bên dưới để tiếp tục:"
                + "          </p>"
                + "          <!-- OTP Box -->"
                + "          <div style='background:#faf7f2;border:1px dashed #d6b285;border-radius:8px;padding:20px;text-align:center;margin:25px 0;'>"
                + "            <span style='font-size:12px;color:#8c6d3e;text-transform:uppercase;letter-spacing:1.5px;font-weight:600;'>MÃ XÁC THỰC CỦA BẠN</span>"
                + "            <div style='font-size:34px;font-weight:800;letter-spacing:8px;color:#111111;margin-top:8px;font-family:monospace;'>" + otpCode + "</div>"
                + "          </div>"
                + "          <p style='margin:0 0 10px;font-size:13px;color:#777777;line-height:20px;'>"
                + "            ⏱️ Mã xác thực này có hiệu lực trong vòng <strong>5 phút</strong>.<br/>"
                + "            🔒 Để bảo đảm an toàn, vui lòng không chia sẻ mã này cho bất kỳ ai khác."
                + "          </p>"
                + "        </td></tr>"
                + "        <!-- Footer -->"
                + "        <tr><td style='background:#fafafa;padding:20px 30px;border-top:1px solid #eeeeee;text-align:center;font-size:12px;color:#999999;'>"
                + "          <p style='margin:0 0 4px;'>Nếu bạn không thực hiện yêu cầu này, vui lòng bỏ qua email.</p>"
                + "          <p style='margin:0;'>© 2026 LUXE Fashion Store. Mọi quyền được bảo lưu.</p>"
                + "        </td></tr>"
                + "      </table>"
                + "    </td></tr>"
                + "  </table>"
                + "</body></html>";
    }
}
