package com.example.library.controller;

import com.example.library.common.ApiResponse;
import com.example.library.common.CurrentUserHolder;
import com.example.library.common.JwtUtil;
import com.example.library.entity.User;
import com.example.library.model.dto.LoginRequest;
import com.example.library.model.vo.AuthUserVO;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, String>> login(@Valid @RequestBody LoginRequest request) {
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            return ApiResponse.failure("Invalid username or password");
        }
        // Simple MD5 check for demo purposes
        String rawPwd = request.getPassword();
        String md5 = DigestUtils.md5DigestAsHex(rawPwd.getBytes(StandardCharsets.UTF_8));
        if (!md5.equalsIgnoreCase(user.getPassword())) {
            return ApiResponse.failure("Invalid username or password");
        }
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return ApiResponse.success(Map.of("token", token));
    }

    @GetMapping("/me")
    public ApiResponse<AuthUserVO> me(@RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader) {
        if (CurrentUserHolder.get() == null) {
            return ApiResponse.failure("Unauthorized");
        }
        User user = userService.findByUsername(CurrentUserHolder.get().getUsername());
        if (user == null) {
            return ApiResponse.failure("Unauthorized");
        }
        AuthUserVO vo = AuthUserVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .role(user.getRole())
                .build();
        return ApiResponse.success(vo);
    }
}
