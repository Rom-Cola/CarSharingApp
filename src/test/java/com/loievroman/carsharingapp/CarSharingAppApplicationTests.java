package com.loievroman.carsharingapp;

import com.loievroman.carsharingapp.config.CustomMySqlContainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CarSharingAppApplicationTests {

    @Autowired
    private org.springframework.core.env.Environment env;

    static {
        CustomMySqlContainer.getInstance().start();
    }

    @Test
    void contextLoads() {
    }

}
