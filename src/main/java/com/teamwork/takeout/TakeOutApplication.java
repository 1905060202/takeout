package com.teamwork.takeout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Slf4j
@SpringBootApplication
@ServletComponentScan
@EnableTransactionManagement
//保持事务一致性，要么不做，要么都做。
public class TakeOutApplication {

    public static void main(String[] args) {

        SpringApplication.run(TakeOutApplication.class, args);
        log.info("瑞吉外卖已经启动了");
    }

}
