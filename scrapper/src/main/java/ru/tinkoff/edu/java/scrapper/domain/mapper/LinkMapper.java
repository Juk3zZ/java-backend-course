package ru.tinkoff.edu.java.scrapper.domain.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;

import java.net.URI;
import java.sql.ResultSet;
import java.sql.SQLException;

@Component
public class LinkMapper implements RowMapper<Link> {

    @Override
    public Link mapRow(ResultSet rs, int rowNum) throws SQLException {
        return new Link(rs.getLong(1), URI.create(rs.getString(2)), rs.getTimestamp(3));
    }
}
