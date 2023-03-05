package com.seven.springcloud;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableFeignClients
@EnableDiscoveryClient
@EnableTransactionManagement
@SpringBootApplication
public class UserManageMain {
    public static void main(String[] args) {
        SpringApplication.run(UserManageMain.class,args);
    }
}
