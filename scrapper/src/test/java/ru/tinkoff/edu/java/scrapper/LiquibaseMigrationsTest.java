package ru.tinkoff.edu.java.scrapper;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class LiquibaseMigrationsTest extends IntegrationEnvironment {

    private static Connection connection;

    @AfterAll
    public static void destroy() throws SQLException {
        connection.close();
    }

    @Test
    public void testInsert() throws SQLException {
        connection = POSTGRESQL_CONTAINER.createConnection("");
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
