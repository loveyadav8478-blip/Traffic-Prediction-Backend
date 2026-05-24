package com.loveyadav.traffic_pred.controller;

import com.loveyadav.traffic_pred.dto.GoogleAuthRequest;
import com.loveyadav.traffic_pred.dto.UserDto;
import com.loveyadav.traffic_pred.entity.BlockListedTokens;
import com.loveyadav.traffic_pred.entity.Role;
import com.loveyadav.traffic_pred.entity.TrafficRecord;
import com.loveyadav.traffic_pred.entity.User;
import com.loveyadav.traffic_pred.repository.BlockListRepository;
import com.loveyadav.traffic_pred.repository.TrafficRepository;
import com.loveyadav.traffic_pred.repository.UserRepository;
import com.loveyadav.traffic_pred.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RequestMapping("api/user")
@RestController
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private TrafficRepository trafficRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BlockListRepository blackListRepository;
    @Autowired
    private RedisService redisService;
//    @Autowired private OtpService otpService;
//    @Autowired private EmailService emailService;

    @PostMapping("/register")
    public User register(@RequestBody UserDto userDto){
        return userService.register(userDto);
    }

    @PostMapping("/login")
    public Map<String,String> login(@RequestBody User user){
        return userService.login(user.getEmail(), user.getPassword());
    }

    @PutMapping("/profile")
    public User updateProfile(@RequestBody Map<String, String> body, Authentication auth) {

        String email = auth.getName(); // logged-in user
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String name = body.get("name");

        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Name cannot be empty");
        }

        user.setName(name.trim());

        return userRepository.save(user);
    }

    // ── Google OAuth ──────────────────────────────────
    // Frontend sends { email, name, googleId, picture }
    // We find or create the user, then return our own JWT
    @PostMapping("/google")
    public Map<String, Object> googleAuth(@RequestBody GoogleAuthRequest req) {
        return userService.loginWithGoogle(req);
    }



    @GetMapping("/history")
    public List<TrafficRecord> getUserHistory(Authentication auth){
        String email = auth.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return trafficRepository.findByUser(user);
    }

    @DeleteMapping("/logout")
    public String logout(HttpServletRequest request){
        String header = request.getHeader("Authorization");
        if(header!=null && header.startsWith("Bearer ")){
            String token = header.substring(7);
            redisService.saveToken(token,15*60*1000);
        }
        return "Logout Successful";
    }
//    // ─────────────────────────────────────────────────────
//    // ✅ NEW: Email Change via OTP
//    // ─────────────────────────────────────────────────────
//
//    /**
//     * Step 1 — Send OTP to the NEW email address the user wants to switch to.
//     *
//     * POST /api/user/email/otp
//     * Body: { "newEmail": "new@example.com" }
//     */
//    @PostMapping("/email/otp")
//    public ResponseEntity<Map<String, String>> sendEmailChangeOtp(
//            @RequestBody Map<String, String> body,
//            Authentication auth) {
//
//        String currentEmail = auth.getName();
//        String newEmail = body.get("newEmail");
//
//        if (newEmail == null || !newEmail.contains("@")) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Invalid email address"));
//        }
//
//        // Reject if new email is same as current
//        if (newEmail.equalsIgnoreCase(currentEmail)) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "New email must differ from current email"));
//        }
//
//        // Reject if new email already registered
//        if (userRepository.findByEmail(newEmail).isPresent()) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Email already in use"));
//        }
//
//        String otp = otpService.generateEmailOtp(currentEmail, newEmail);
//        emailService.sendEmailChangeOtp(newEmail, otp, otpService.getTtlSeconds());
//
//        return ResponseEntity.ok(Map.of(
//                "message", "OTP sent to " + newEmail,
//                "ttlSeconds", String.valueOf(otpService.getTtlSeconds())
//        ));
//    }
//
//    /**
//     * Step 2 — Verify OTP and update the email if correct.
//     *
//     * PUT /api/user/email/verify
//     * Body: { "newEmail": "new@example.com", "otp": "123456" }
//     */
//    @PutMapping("/email/verify")
//    public ResponseEntity<Map<String, Object>> verifyEmailChange(
//            @RequestBody Map<String, String> body,
//            Authentication auth) {
//
//        String currentEmail = auth.getName();
//        String newEmail = body.get("newEmail");
//        String otp = body.get("otp");
//
//        if (newEmail == null || otp == null) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "newEmail and otp are required"));
//        }
//
//        boolean valid = otpService.verifyEmailOtp(currentEmail, newEmail, otp);
//        if (!valid) {
//            return ResponseEntity.badRequest()
//                    .body(Map.of("message", "Invalid or expired OTP"));
//        }
//
//        // Update email in DB
//        User user = userRepository.findByEmail(currentEmail)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//        user.setEmail(newEmail);
//        userRepository.save(user);
//
//        // Return updated user data so frontend can refresh its store
//        return ResponseEntity.ok(Map.of(
//                "message", "Email updated successfully",
//                "email", newEmail,
//                "name", user.getName() != null ? user.getName() : ""
//        ));
//    }
}
