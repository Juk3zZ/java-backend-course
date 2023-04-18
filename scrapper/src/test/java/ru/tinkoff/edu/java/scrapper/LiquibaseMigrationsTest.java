package ru.tinkoff.edu.java.scrapper;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.DirectoryResourceAccessor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiquibaseMigrationsTest extends IntegrationEnvironment {

    private static Connection connection;

    @BeforeAll
    public static void init() throws SQLException, LiquibaseException, FileNotFoundException {
        connection = DriverManager.getConnection(
                POSTGRESQL_CONTAINER.getJdbcUrl(),
                POSTGRESQL_CONTAINER.getUsername(),
                POSTGRESQL_CONTAINER.getPassword()
        );

        var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

        var migrations = new File(".").toPath().toAbsolutePath()
                .getParent().getParent().resolve("migrations");

        var liquibase = new Liquibase("master.xml", new DirectoryResourceAccessor(migrations), database);
        liquibase.update(new Contexts(), new LabelExpression());
    }

    @AfterAll
    public static void destroy() throws SQLException {
        connection.close();
        POSTGRESQL_CONTAINER.stop();
    }

    @Test
    public void testInsert() throws SQLException {
        var preparedStatement = connection.createStatement();
        var updateResult = preparedStatement.executeUpdate("INSERT INTO app.chats(tg_chat_id, nickname) VALUES (1, 'Vladimir')");

        var statement = connection.createStatement();
        var rs = statement.executeQuery("SELECT * FROM app.chats");
        rs.next();
        assertAll(
                () -> assertEquals(1, updateResult),
                () -> assertEquals(1L, rs.getLong(1)),
                () -> assertEquals("Vladimir", rs.getString(2))
        );
    }
}
