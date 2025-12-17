package com.example.library.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("reader")
public class Reader {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String type;

    private String department;

    private String email;

    private String phone;

    private LocalDateTime createdAt;
}
