//package com.loveyadav.traffic_pred.service;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.MimeMessageHelper;
//import org.springframework.stereotype.Service;
//
//import jakarta.mail.internet.MimeMessage;
//
//@Service
//public class EmailService {
//
//    @Autowired
//    private JavaMailSender mailSender;
//
//    @Value("${spring.mail.username}")
//    private String fromAddress;
//
//    /**
//     * Sends a styled OTP email for email-change verification.
//     */
//    public void sendEmailChangeOtp(String toEmail, String otp, long ttlSeconds) {
//        try {
//            MimeMessage msg = mailSender.createMimeMessage();
//            MimeMessageHelper helper = new MimeMessageHelper(msg, true, "UTF-8");
//
//            helper.setFrom(fromAddress, "TrafficPred Security");
//            helper.setTo(toEmail);
//            helper.setSubject("[ TrafficPred ] Email Change Verification Code");
//            helper.setText(buildEmailHtml(otp, ttlSeconds), true);  // true = HTML
//
//            mailSender.send(msg);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to send OTP email: " + e.getMessage(), e);
//        }
//    }
//
//    private String buildEmailHtml(String otp, long ttlSeconds) {
//        long minutes = ttlSeconds / 60;
//        return """
//            <!DOCTYPE html>
//            <html>
//            <body style="margin:0;padding:0;background:#0d1117;font-family:'Courier New',monospace;">
//              <table width="100%%" cellpadding="0" cellspacing="0">
//                <tr>
//                  <td align="center" style="padding:40px 20px;">
//                    <table width="480" cellpadding="0" cellspacing="0"
//                      style="background:#161b22;border:1px solid #21262d;border-radius:12px;">
//                      <tr>
//                        <td style="padding:32px 36px;">
//                          <!-- Header -->
//                          <div style="margin-bottom:24px;">
//                            <span style="color:#00e5cc;font-size:18px;font-weight:700;letter-spacing:2px;">
//                              TP · TRAFFICPRED
//                            </span>
//                          </div>
//                          <!-- Title -->
//                          <div style="color:#e6edf3;font-size:15px;font-weight:600;
//                                      letter-spacing:1px;margin-bottom:8px;">
//                            EMAIL CHANGE VERIFICATION
//                          </div>
//                          <div style="color:#8b949e;font-size:12px;margin-bottom:28px;
//                                      line-height:1.6;">
//                            A request was made to change your email address.<br/>
//                            Use the code below to confirm this change.
//                          </div>
//                          <!-- OTP Box -->
//                          <div style="background:#0d1117;border:1px solid #00e5cc33;
//                                      border-radius:8px;padding:24px;text-align:center;
//                                      margin-bottom:24px;">
//                            <div style="color:#8b949e;font-size:10px;letter-spacing:3px;
//                                        margin-bottom:12px;">VERIFICATION CODE</div>
//                            <div style="color:#00e5cc;font-size:36px;font-weight:700;
//                                        letter-spacing:12px;">%s</div>
//                            <div style="color:#8b949e;font-size:10px;margin-top:12px;
//                                        letter-spacing:1px;">
//                              EXPIRES IN %d MINUTES
//                            </div>
//                          </div>
//                          <!-- Warning -->
//                          <div style="color:#8b949e;font-size:11px;line-height:1.6;
//                                      border-top:1px solid #21262d;padding-top:16px;">
//                            If you did not request this change, ignore this email.
//                            Your account remains secure.
//                          </div>
//                        </td>
//                      </tr>
//                    </table>
//                  </td>
//                </tr>
//              </table>
//            </body>
//            </html>
//            """.formatted(otp, minutes);
//    }
//}