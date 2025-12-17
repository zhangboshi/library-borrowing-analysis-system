package com.example.library.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.library.common.ApiResponse;
import com.example.library.common.PageResponse;
import com.example.library.common.RequireRole;
import com.example.library.entity.User;
import com.example.library.model.dto.UpdateUserRoleRequest;
import com.example.library.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Validated
@RequireRole("ADMIN")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<PageResponse<User>> list(
            @RequestParam(defaultValue = "1") @Min(value = 1, message = "page must be >= 1") long page,
            @RequestParam(defaultValue = "10") @Min(value = 1, message = "pageSize must be >= 1") long pageSize) {
        Page<User> result = userService.page(new Page<>(page, pageSize));
        List<User> records = result.getRecords().stream().map(this::maskPassword).collect(Collectors.toList());
        PageResponse<User> resp = PageResponse.of(result.getTotal(), result.getCurrent(), result.getSize(), records);
        return ApiResponse.success(resp);
    }

    @PutMapping("/{id}/role")
    public ApiResponse<Void> updateRole(@PathVariable @Min(value = 1, message = "id must be positive") Long id,
                                        @Valid @RequestBody UpdateUserRoleRequest request) {
        User user = userService.getById(id);
        if (user == null) {
            return ApiResponse.failure("User not found");
        }
        user.setRole(request.getRole());
        userService.updateById(user);
        return ApiResponse.success();
    }

    private User maskPassword(User user) {
        User safe = new User();
        BeanUtils.copyProperties(user, safe);
        safe.setPassword("****");
        return safe;
    }
}
