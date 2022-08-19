package com.zsck.bot;

import love.forte.simbot.spring.autoconfigure.EnableSimbot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;


@EnableAspectJAutoProxy
@EnableSimbot
@SpringBootApplication
public class SimbotApplication {

    public static void main(String[] args) {
        //Security.addProvider( new BouncyCastleProvider());
        SpringApplication.run(SimbotApplication.class, args);
    }

}
