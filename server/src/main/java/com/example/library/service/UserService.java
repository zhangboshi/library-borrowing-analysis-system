package com.example.library.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.library.entity.User;

public interface UserService extends IService<User> {

    User findByUsername(String username);
}
