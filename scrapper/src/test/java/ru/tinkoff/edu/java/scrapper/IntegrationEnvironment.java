package ru.tinkoff.edu.java.scrapper;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.DirectoryResourceAccessor;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.File;
import java.sql.DriverManager;

public class IntegrationEnvironment {

    protected static final PostgreSQLContainer<?> POSTGRESQL_CONTAINER;

    static {
        POSTGRESQL_CONTAINER = new PostgreSQLContainer<>("postgres:15");
        POSTGRESQL_CONTAINER.withDatabaseName("scrapper");
        POSTGRESQL_CONTAINER.start();
        //TODO: сделать обработку покрасивее
        try {
            var connection = DriverManager.getConnection(
                    POSTGRESQL_CONTAINER.getJdbcUrl(),
                    POSTGRESQL_CONTAINER.getUsername(),
                    POSTGRESQL_CONTAINER.getPassword()
            );

            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            var migrations = new File(".").toPath().toAbsolutePath()
                    .getParent().getParent().resolve("migrations");

            var liquibase = new Liquibase("master.xml", new DirectoryResourceAccessor(migrations), database);
            liquibase.update(new Contexts(), new LabelExpression());
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
