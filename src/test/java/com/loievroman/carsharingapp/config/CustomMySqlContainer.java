package com.loievroman.carsharingapp.config;

import java.time.Duration;
import org.testcontainers.containers.MySQLContainer;

public class CustomMySqlContainer extends MySQLContainer<CustomMySqlContainer> {
    private static final String DB_IMAGE = "mysql:8.0.33";
    private static final String DATABASE_NAME = "testdb";
    private static final String USERNAME = "test";
    private static final String PASSWORD = "test";

    private static CustomMySqlContainer mysqlContainer;

    private CustomMySqlContainer() {
        super(DB_IMAGE);
        this.withDatabaseName(DATABASE_NAME).withUsername(USERNAME).withPassword(PASSWORD)
                .withReuse(true).withStartupTimeout(Duration.ofSeconds(120))
                .withCommand("mysqld", "--character-set-server=utf8mb4",
                        "--collation-server=utf8mb4_unicode_ci",
                        "--default-authentication-plugin=mysql_native_password",
                        "--innodb-flush-method=fsync", "--innodb-use-native-aio=0",
                        "--log-bin-trust-function-creators=1")

                .withEnv("MYSQL_ROOT_HOST", "%").withEnv("MYSQL_ROOT_PASSWORD", PASSWORD);
    }

    public static synchronized CustomMySqlContainer getInstance() {
        if (mysqlContainer == null) {
            mysqlContainer = new CustomMySqlContainer();
        }
        return mysqlContainer;
    }

    @Override
    public void start() {
        super.start();
        System.setProperty("TEST_DB_URL", this.getJdbcUrl());
        System.setProperty("TEST_DB_USERNAME", this.getUsername());
        System.setProperty("TEST_DB_PASSWORD", this.getPassword());
    }

    @Override
    public void stop() {

    }
}
