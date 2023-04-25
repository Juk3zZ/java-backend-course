package ru.tinkoff.edu.java.scrapper.domain.jdbc.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.postgresql.util.PGobject;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.util.QueriesSource;
import ru.tinkoff.edu.java.scrapper.domain.jdbc.mapper.LinkMapper;
import ru.tinkoff.edu.java.scrapper.domain.util.MappingUtils;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Repository
@Profile("!test")
public class JdbcLinkRepository {

    public static final String SELECT_KEY = "app.links.select";
    public static final String SELECT_BY_TG_ID_KEY = "app.links.selectByTgId";
    public static final String SELECT_BY_LINK_KEY = "app.links.selectByLink";
    public static final String INSERT_TRACKING_KEY = "app.links.insertTrackings";
    public static final String INSERT_WITH_TIMESTAMP_KEY = "app.links.insertWithTimestamp";
    public static final String SELECT_TRACKING_BY_LINK_KEY = "app.links.selectTrackingByLink";
    public static final String DELETE_TRACKING_KEY = "app.links.deleteTracking";
    public static final String UPDATE_KEY = "app.links.update";
    public static final String SELECT_BY_UPDATED_AT_KEY = "app.links.selectByUpdatedAt";
    private final JdbcTemplate jdbcTemplate;
    private final LinkMapper linkMapper;
    private final QueriesSource queriesSource;
    private final MappingUtils mappingUtils;
    private final ObjectMapper objectMapper;

    public JdbcLinkRepository(JdbcTemplate jdbcTemplate, LinkMapper linkMapper, QueriesSource queriesSource, MappingUtils mappingUtils, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.linkMapper = linkMapper;
        this.queriesSource = queriesSource;
        this.mappingUtils = mappingUtils;
        this.objectMapper = objectMapper;
    }

    public List<Link> findAll() {
        return jdbcTemplate.query(queriesSource.getQuery(SELECT_KEY), linkMapper);
    }

    public List<Link> findAll(long tgChatId) {
        return jdbcTemplate.query(queriesSource.getQuery(SELECT_BY_TG_ID_KEY), linkMapper, tgChatId);
    }

    public Map<Link, List<Long>> findOld(long expiration, long currentTime) {
        var timeBorder = Timestamp.from(Instant.ofEpochMilli(currentTime - expiration));
        List<Map.Entry<Link, Long>> entries = jdbcTemplate.query(
                queriesSource.getQuery(SELECT_BY_UPDATED_AT_KEY),
                (rs, rowNum) -> Map.entry(linkMapper.mapRow(rs, rowNum), rs.getLong(5)),
                timeBorder
        );
        return mappingUtils.createMapFromEntries(entries);
    }

    public Link add(long tgChatId, String link) {
        var linksFound = jdbcTemplate.query(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
        if (!linksFound.isEmpty()) {
            var storedLink = linksFound.get(0);
            jdbcTemplate.update(queriesSource.getQuery(INSERT_TRACKING_KEY), tgChatId, storedLink.id());
            return storedLink;
        } else {
            jdbcTemplate.update(
                    queriesSource.getQuery(INSERT_WITH_TIMESTAMP_KEY),
                    link,
                    Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis())),
                    null
            );
            var newLink = jdbcTemplate.queryForObject(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
            jdbcTemplate.update(queriesSource.getQuery(INSERT_TRACKING_KEY), tgChatId, newLink.id());
            return newLink;
        }
    }

    public Link save(String link, Timestamp updatedAt, Map<String, Object> updateInfo) {
        var linksFound = jdbcTemplate.query(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
        var updateInfoPG = mapToPGObject(mapToJson(updateInfo));

        if (!linksFound.isEmpty()) {
            var currentLink = linksFound.get(0);
            currentLink = new Link(currentLink.id(), currentLink.link(), updatedAt, updateInfo);
            jdbcTemplate.update(queriesSource.getQuery(UPDATE_KEY), updatedAt, updateInfoPG, currentLink.id());
            return currentLink;
        } else {
            jdbcTemplate.update(queriesSource.getQuery(INSERT_WITH_TIMESTAMP_KEY), link, updatedAt, updateInfoPG);
            return jdbcTemplate.queryForObject(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
        }
    }

    private String mapToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private PGobject mapToPGObject(String json) {
        var pGobject = new PGobject();
        pGobject.setType("jsonb");
        try {
            pGobject.setValue(json);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return pGobject;
    }

    public Link remove(long tgChatId, String link) {
        var candidates = jdbcTemplate.query(queriesSource.getQuery(SELECT_TRACKING_BY_LINK_KEY), linkMapper, link, tgChatId);
        if (!candidates.isEmpty()) {
            jdbcTemplate.update(queriesSource.getQuery(DELETE_TRACKING_KEY), tgChatId, link);
        }
        return candidates.stream().findFirst().orElse(null);
    }
}
