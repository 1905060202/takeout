package com.teamwork.takeout;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@Slf4j
@SpringBootApplication
@ServletComponentScan
public class TakeOutApplication {

    public static void main(String[] args) {

        SpringApplication.run(TakeOutApplication.class, args);
        log.info("瑞吉外卖已经启动了");
    }

}
