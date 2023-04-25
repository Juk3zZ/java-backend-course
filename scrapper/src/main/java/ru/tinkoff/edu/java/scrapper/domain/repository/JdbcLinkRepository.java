package ru.tinkoff.edu.java.scrapper.domain.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.domain.mapper.LinkMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.Link;
import ru.tinkoff.edu.java.scrapper.domain.util.QueriesSource;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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

    public JdbcLinkRepository(JdbcTemplate jdbcTemplate, LinkMapper linkMapper, QueriesSource queriesSource) {
        this.jdbcTemplate = jdbcTemplate;
        this.linkMapper = linkMapper;
        this.queriesSource = queriesSource;
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
                (rs, rowNum) -> Map.entry(linkMapper.mapRow(rs, rowNum), rs.getLong(4)),
                timeBorder
        );
        return createMapFromEntries(entries);
    }

    private Map<Link, List<Long>> createMapFromEntries(List<Map.Entry<Link, Long>> entries) {
        var result = new HashMap<Link, List<Long>>();
        for (var entry : entries) {
            var list = result.get(entry.getKey());
            if (list == null) {
                var ids = new ArrayList<Long>();
                ids.add(entry.getValue());
                result.put(entry.getKey(), ids);
            } else {
                list.add(entry.getValue());
            }
        }
        return result;
    }

    public Link add(long tgChatId, String link) {
        var linksFound = jdbcTemplate.query(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
        if (!linksFound.isEmpty()) {
            var storedLink = linksFound.get(0);
            jdbcTemplate.update(queriesSource.getQuery(INSERT_TRACKING_KEY), tgChatId, storedLink.id());
            return storedLink;
        } else {
            jdbcTemplate.update(queriesSource.getQuery(INSERT_WITH_TIMESTAMP_KEY), link, Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis())));
            var newLink = jdbcTemplate.queryForObject(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
            jdbcTemplate.update(queriesSource.getQuery(INSERT_TRACKING_KEY), tgChatId, newLink.id());
            return newLink;
        }
    }

    public Link save(String link, Timestamp updatedAt) {
        var linksFound = jdbcTemplate.query(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
        if (!linksFound.isEmpty()) {
            var currentLink = linksFound.get(0);
            if (currentLink.updatedAt() == null) {
                currentLink = new Link(currentLink.id(), currentLink.link(), updatedAt);
                jdbcTemplate.update(queriesSource.getQuery(UPDATE_KEY), updatedAt, currentLink.id());
            }
            return currentLink;
        } else {
            jdbcTemplate.update(queriesSource.getQuery(INSERT_WITH_TIMESTAMP_KEY), link, updatedAt);
            return jdbcTemplate.queryForObject(queriesSource.getQuery(SELECT_BY_LINK_KEY), linkMapper, link);
        }
    }

    public Link remove(long tgChatId, String link) {
        var candidates = jdbcTemplate.query(queriesSource.getQuery(SELECT_TRACKING_BY_LINK_KEY), linkMapper, link, tgChatId);
        if (!candidates.isEmpty()) {
            jdbcTemplate.update(queriesSource.getQuery(DELETE_TRACKING_KEY), tgChatId, link);
        }
        return candidates.stream().findFirst().orElse(null);
    }
}
