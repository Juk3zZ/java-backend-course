package ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.scrapper.domain.model.TableLink;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class LinkMapper implements RowMapper<TableLink> {

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public TableLink mapRow(ResultSet rs, int rowNum) throws SQLException {
        var object = (PGobject) rs.getObject(4);
        Map<String, Object> updateInfoFinal = null;

        if (object != null) {
            var updateInfo = object.getValue();
            try {
                updateInfoFinal = mapper.readValue(
                        updateInfo,
                        TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return new TableLink(
                rs.getLong(1),
                rs.getString(2),
                rs.getTimestamp(3),
                updateInfoFinal == null ? new HashMap<>() : updateInfoFinal
        );
    }
}
