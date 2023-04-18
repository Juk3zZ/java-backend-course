package ru.tinkoff.edu.java.scrapper.domain.jooq.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.jooq.JSONB;
import org.jooq.Record4;
import org.jooq.RecordMapper;
import org.springframework.stereotype.Component;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;

import java.net.URI;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

@Component
public class LinkFieldsMapper implements RecordMapper<Record4<Integer, String, LocalDateTime, JSONB>, Link> {

    private final ObjectMapper objectMapper;

    public LinkFieldsMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Link map(Record4<Integer, String, LocalDateTime, JSONB> record) {
        Map<String, Object> updateInfo = null;

        if (record.component4() != null) {
            try {
                updateInfo = objectMapper.readValue(
                        record.component4().data(),
                        TypeFactory.defaultInstance().constructMapType(HashMap.class, String.class, Object.class)
                );
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }

        return new Link(
                record.component1(),
                URI.create(record.component2()),
                record.component3() == null ? null : Timestamp.from(record.component3().atZone(ZoneId.systemDefault()).toInstant()),
                updateInfo == null ? new HashMap<>() : updateInfo
        );
    }
}
