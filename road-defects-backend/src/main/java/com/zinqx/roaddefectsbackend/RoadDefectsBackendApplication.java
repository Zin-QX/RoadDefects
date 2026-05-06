package com.zinqx.roaddefectsbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.zinqx.roaddefectsbackend.mapper")
@EnableScheduling
public class RoadDefectsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoadDefectsBackendApplication.class, args);
    }

}
