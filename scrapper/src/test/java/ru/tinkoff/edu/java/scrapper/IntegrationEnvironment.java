package ru.tinkoff.edu.java.scrapper;

import org.testcontainers.containers.PostgreSQLContainer;

public class IntegrationEnvironment {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");
        POSTGRESQL_CONTAINER.withDatabaseName("scrapper");
        POSTGRESQL_CONTAINER.start();
    }
}
