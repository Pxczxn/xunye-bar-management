package com.xunye.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 寻野酒吧管理系统启动类
 */
@SpringBootApplication
@MapperScan("com.xunye.admin.mapper")
public class XunyeAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(XunyeAdminApplication.class, args);
    }

}
