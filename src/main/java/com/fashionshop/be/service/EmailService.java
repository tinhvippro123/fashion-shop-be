package com.fashionshop.be.service;

import com.fashionshop.be.domain.entity.Order;
import com.fashionshop.be.domain.entity.OrderItem;
import jakarta.annotation.PreDestroy;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    private final ExecutorService mailExecutor = Executors.newFixedThreadPool(4, r -> {
        Thread thread = new Thread(r, "luxe-mail-sender");
        thread.setDaemon(true);
        return thread;
    });

    @PreDestroy
    public void shutdown() {
        mailExecutor.shutdown();
    }

    public boolean isSmtpConfigured() {
        return mailSender != null && fromEmail != null && !fromEmail.isBlank();
    }

    /**
     * Gửi email OTP xác thực cho Đăng ký hoặc Quên mật khẩu bất đồng bộ (non-blocking).
     * Phản hồi ngay lập tức cho client mà không bắt người dùng phải đợi SMTP handshake 3-5s.
     * @return true nếu hệ thống SMTP đã cấu hình sẵn sàng gửi ngầm, false nếu chạy chế độ fallback/mô phỏng.
     */
    public boolean sendOtpEmail(String toEmail, String otpCode, String actionTitle, String actionDesc) {
        log.info("[LUXE EMAIL OTP] Đã phát sinh mã {} cho {} là: {}", actionTitle, toEmail, otpCode);

        if (!isSmtpConfigured()) {
            log.info("[LUXE EMAIL] Chưa cấu hình spring.mail.username hoặc đang chạy TEST, mô phỏng phát sinh OTP: {}", otpCode);
            return false;
        }

        // Bỏ qua gửi SMTP thực tế cho các email test nội bộ (ví dụ: newmember@luxe.vn) để tránh lỗi Mail Delivery Subsystem
        String lowerEmail = toEmail != null ? toEmail.trim().toLowerCase() : "";
        if (lowerEmail.endsWith("@luxe.vn") || lowerEmail.endsWith("@example.com") || lowerEmail.endsWith("@test.com")) {
            log.info("[LUXE EMAIL] Bỏ qua gửi SMTP thực tế cho domain test nội bộ: {}", toEmail);
            return true;
        }

        // Bất đồng bộ: Đẩy công việc gửi SMTP vào background thread pool, giải phóng ngay luồng HTTP
        CompletableFuture.runAsync(() -> {
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
            } catch (Exception e) {
                log.error("[LUXE EMAIL] Lỗi khi gửi email thật qua SMTP tới {}: {}", toEmail, e.getMessage());
            }
        }, mailExecutor);

        return true;
    }

    /**
     * Gửi email thông báo đơn hàng đã được giao thành công bất đồng bộ.
     */
    public boolean sendOrderCompletedEmail(Order order) {
        if (order == null) return false;

        String toEmail = order.getRecipientEmail();
        if ((toEmail == null || toEmail.isBlank()) && order.getUser() != null) {
            toEmail = order.getUser().getEmail();
        }
        if (toEmail == null || toEmail.isBlank()) {
            log.info("[LUXE EMAIL] Đơn hàng {} không có email người nhận, bỏ qua gửi email", order.getOrderCode());
            return false;
        }

        String recipientEmail = toEmail.trim();
        String orderCode = order.getOrderCode() != null ? order.getOrderCode() : ("#" + order.getId());
        log.info("[LUXE EMAIL] Chuẩn bị gửi email giao hàng thành công đơn {} tới {}", orderCode, recipientEmail);

        if (!isSmtpConfigured()) {
            log.info("[LUXE EMAIL] Chưa cấu hình spring.mail.username hoặc đang chạy TEST, mô phỏng gửi email đơn {} thành công", orderCode);
            return false;
        }

        String lowerEmail = recipientEmail.toLowerCase();
        if (lowerEmail.endsWith("@luxe.vn") || lowerEmail.endsWith("@example.com") || lowerEmail.endsWith("@test.com")) {
            log.info("[LUXE EMAIL] Bỏ qua gửi SMTP thực tế cho domain test nội bộ: {}", recipientEmail);
            return true;
        }

        final String finalEmail = recipientEmail;
        CompletableFuture.runAsync(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(new InternetAddress(fromEmail, "LUXE Fashion"));
                helper.setTo(finalEmail);
                helper.setSubject("[LUXE Fashion] Đơn hàng #" + orderCode + " đã được giao thành công!");

                String htmlContent = buildOrderCompletedHtmlContent(order, orderCode);
                helper.setText(htmlContent, true);

                mailSender.send(message);
                log.info("[LUXE EMAIL] Đã gửi email giao hàng thành công đơn {} tới: {}", orderCode, finalEmail);
            } catch (Exception e) {
                log.error("[LUXE EMAIL] Lỗi khi gửi email giao hàng thành công tới {}: {}", finalEmail, e.getMessage());
            }
        }, mailExecutor);

        return true;
    }

    private String buildOrderCompletedHtmlContent(Order order, String orderCode) {
        String recipientName = order.getRecipientName() != null ? order.getRecipientName() : "Quý khách";
        String recipientPhone = order.getRecipientPhone() != null ? order.getRecipientPhone() : "";
        String shippingAddress = order.getShippingAddress() != null ? order.getShippingAddress() : "";
        String paymentMethod = order.getPaymentMethod() != null ? order.getPaymentMethod().name() : "COD";
        String totalFormatted = order.getTotalAmount() != null
                ? String.format(Locale.GERMANY, "%,d đ", order.getTotalAmount().longValue())
                : "0 đ";

        StringBuilder itemsHtml = new StringBuilder();
        if (order.getItems() != null && !order.getItems().isEmpty()) {
            for (OrderItem item : order.getItems()) {
                String variantInfo = "";
                if (item.getColorName() != null || item.getSizeName() != null) {
                    variantInfo = " (" + (item.getColorName() != null ? item.getColorName() : "")
                            + (item.getSizeName() != null ? " / " + item.getSizeName() : "") + ")";
                }
                String itemPrice = item.getUnitPrice() != null
                        ? String.format(Locale.GERMANY, "%,d đ", item.getUnitPrice().longValue())
                        : "";
                itemsHtml.append("<tr>")
                        .append("<td style='padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:13px;color:#333;'>")
                        .append("<strong>").append(item.getProductName()).append("</strong>")
                        .append("<div style='font-size:12px;color:#777;'>").append(variantInfo).append(" x ").append(item.getQuantity()).append("</div>")
                        .append("</td>")
                        .append("<td style='padding:10px 0;border-bottom:1px solid #f0f0f0;font-size:13px;color:#111;text-align:right;font-weight:600;'>")
                        .append(itemPrice)
                        .append("</td>")
                        .append("</tr>");
            }
        }

        return "<!DOCTYPE html>"
                + "<html lang='vi'>"
                + "<head><meta charset='UTF-8'></head>"
                + "<body style='margin:0;padding:0;background-color:#f4f5f7;font-family:Arial,Helvetica,sans-serif;color:#333;'>"
                + "  <table width='100%' cellpadding='0' cellspacing='0' style='background-color:#f4f5f7;padding:30px 15px;'>"
                + "    <tr><td align='center'>"
                + "      <table width='100%' max-width='580px' cellpadding='0' cellspacing='0' style='max-width:580px;background:#ffffff;border-radius:12px;overflow:hidden;box-shadow:0 4px 15px rgba(0,0,0,0.06);'>"
                + "        <!-- Header -->"
                + "        <tr><td style='background:#111111;padding:24px 30px;text-align:center;'>"
                + "          <h1 style='margin:0;font-size:24px;letter-spacing:4px;color:#ffffff;font-weight:700;'>LUXE FASHION</h1>"
                + "          <p style='margin:4px 0 0;font-size:11px;color:#c5a880;letter-spacing:2px;text-transform:uppercase;'>Đẳng Cấp Thời Trang Hiện Đại</p>"
                + "        </td></tr>"
                + "        <!-- Body -->"
                + "        <tr><td style='padding:35px 30px;'>"
                + "          <div style='text-align:center;margin-bottom:20px;'>"
                + "            <span style='display:inline-block;background:#e8f5e9;color:#2e7d32;font-size:12px;font-weight:700;padding:6px 14px;border-radius:20px;text-transform:uppercase;letter-spacing:1px;'>✓ Giao Hàng Thành Công</span>"
                + "            <h2 style='margin:12px 0 6px;font-size:20px;color:#111111;font-weight:700;'>ĐƠN HÀNG #" + orderCode + "</h2>"
                + "          </div>"
                + "          <p style='margin:0 0 18px;font-size:14px;line-height:22px;color:#555555;'>"
                + "            Xin chào <strong>" + recipientName + "</strong>,<br/>"
                + "            LUXE Fashion xin thông báo đơn hàng của bạn đã được giao hoàn tất. Hy vọng bạn hài lòng với những sản phẩm thời trang từ LUXE!"
                + "          </p>"
                + "          <!-- Shipping Details Box -->"
                + "          <div style='background:#f9fafb;border:1px solid #e5e7eb;border-radius:8px;padding:16px;margin:20px 0;font-size:13px;line-height:20px;color:#444;'>"
                + "            <div style='font-weight:700;color:#111;margin-bottom:8px;text-transform:uppercase;font-size:11px;letter-spacing:1px;'>THÔNG TIN GIAO HÀNG</div>"
                + "            <div><strong>Người nhận:</strong> " + recipientName + " (" + recipientPhone + ")</div>"
                + "            <div><strong>Địa chỉ:</strong> " + shippingAddress + "</div>"
                + "            <div><strong>Hình thức thanh toán:</strong> " + paymentMethod + "</div>"
                + "          </div>"
                + "          <!-- Order Items -->"
                + "          <div style='margin:25px 0 15px;'>"
                + "            <div style='font-weight:700;color:#111;margin-bottom:10px;text-transform:uppercase;font-size:11px;letter-spacing:1px;'>CHI TIẾT SẢN PHẨM</div>"
                + "            <table width='100%' cellpadding='0' cellspacing='0'>"
                +                itemsHtml.toString()
                + "            </table>"
                + "          </div>"
                + "          <!-- Total Amount -->"
                + "          <div style='background:#faf7f2;border-left:4px solid #c5a880;padding:12px 16px;margin:20px 0;display:flex;justify-content:space-between;'>"
                + "            <span style='font-size:14px;font-weight:700;color:#111;'>TỔNG THANH TOÁN:</span>"
                + "            <span style='font-size:16px;font-weight:800;color:#e10600;float:right;'>" + totalFormatted + "</span>"
                + "            <div style='clear:both;'></div>"
                + "          </div>"
                + "          <p style='margin:20px 0 0;font-size:13px;color:#777777;line-height:20px;'>"
                + "            📞 Nếu quý khách có bất kỳ thắc mắc hoặc cần hỗ trợ đổi trả sản phẩm, xin vui lòng liên hệ hotline <strong>1800 2026</strong> hoặc gửi phản hồi trực tiếp qua email này.<br/>"
                + "            Chân thành cảm ơn bạn đã đồng hành cùng LUXE Fashion!"
                + "          </p>"
                + "        </td></tr>"
                + "        <!-- Footer -->"
                + "        <tr><td style='background:#fafafa;padding:20px 30px;border-top:1px solid #eeeeee;text-align:center;font-size:12px;color:#999999;'>"
                + "          <p style='margin:0 0 4px;'>Email tự động từ hệ thống LUXE Fashion Store.</p>"
                + "          <p style='margin:0;'>© 2026 LUXE Fashion Store. Mọi quyền được bảo lưu.</p>"
                + "        </td></tr>"
                + "      </table>"
                + "    </td></tr>"
                + "  </table>"
                + "</body></html>";
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

