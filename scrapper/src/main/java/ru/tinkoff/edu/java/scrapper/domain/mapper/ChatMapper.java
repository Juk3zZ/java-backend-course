package ru.tinkoff.edu.java.scrapper.domain.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.scrapper.domain.model.Chat;

import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class ChatMapper implements RowMapper<Chat> {

    @Override
    public Chat mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Chat(rs.getLong(1), rs.getString(2));
    }
}
