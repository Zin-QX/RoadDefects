package com.zinqx.roaddefectsbackend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("com.zinqx.roaddefectsbackend.mapper")
public class RoadDefectsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(RoadDefectsBackendApplication.class, args);
    }

}
