package ru.tinkoff.edu.java.scrapper.domain.jooq.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jooq.DSLContext;
import org.jooq.JSONB;
import ru.tinkoff.edu.java.scrapper.domain.jooq.mapper.LinkFieldsMapper;
import ru.tinkoff.edu.java.scrapper.domain.model.TableLink;
import ru.tinkoff.edu.java.scrapper.domain.util.MappingUtils;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import static ru.tinkoff.edu.java.scrapper.domain.jooq.Tables.LINKS;
import static ru.tinkoff.edu.java.scrapper.domain.jooq.Tables.TRACKINGS;

public class JooqLinkRepository {

    private final DSLContext dslContext;
    private final MappingUtils mappingUtils;
    private final ObjectMapper objectMapper;
    private final LinkFieldsMapper linkFieldsMapper;

    public JooqLinkRepository(DSLContext dslContext, MappingUtils mappingUtils, ObjectMapper objectMapper, LinkFieldsMapper linkFieldsMapper) {
        this.dslContext = dslContext;
        this.mappingUtils = mappingUtils;
        this.objectMapper = objectMapper;
        this.linkFieldsMapper = linkFieldsMapper;
    }

    public List<TableLink> findAll() {
        return dslContext
                .select(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).from(LINKS).fetch().map(linkFieldsMapper);
    }

    public List<TableLink> findAll(long tgChatId) {
        return dslContext
                .select(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO)
                .from(LINKS)
                .join(TRACKINGS).on(LINKS.ID.eq(TRACKINGS.LINK_ID.coerce(LINKS.ID)))
                .where(TRACKINGS.TG_CHAT_ID.eq(tgChatId)).fetch().map(linkFieldsMapper);
    }

    public Map<TableLink, List<Long>> findOld(long expiration, long currentTime) {
        var timeBorder = Instant.ofEpochMilli(currentTime - expiration);
        var entries = dslContext
                .select(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO, TRACKINGS.TG_CHAT_ID)
                .from(LINKS).join(TRACKINGS).on(TRACKINGS.LINK_ID.eq(LINKS.ID.coerce(TRACKINGS.LINK_ID)))
                .where(LINKS.UPDATED_AT.le(LocalDateTime.ofInstant(timeBorder, ZoneId.systemDefault())))
                .fetch().map(
                        record -> Map.entry(
                                linkFieldsMapper.map(record.into(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO)),
                                record.component5()
                        )
                );
        return mappingUtils.createMapFromEntries(entries);
    }

    public TableLink add(long tgChatId, String link) {
        var foundLinks = dslContext.select(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).from(LINKS)
                .where(LINKS.LINK.eq(link)).fetch().map(linkFieldsMapper);

        if (!foundLinks.isEmpty()) {
            var storedLink = foundLinks.get(0);
            dslContext
                    .insertInto(TRACKINGS).columns(TRACKINGS.LINK_ID, TRACKINGS.TG_CHAT_ID)
                    .values(storedLink.id(), tgChatId).execute();
            return storedLink;
        } else {
            var newLink = dslContext.insertInto(LINKS).columns(LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO)
                    .values(link, Timestamp.from(Instant.ofEpochMilli(System.currentTimeMillis())).toLocalDateTime(), null)
                    .returning(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).fetch().map(linkFieldsMapper).get(0);

            dslContext.insertInto(TRACKINGS).columns(TRACKINGS.LINK_ID, TRACKINGS.TG_CHAT_ID).values(newLink.id(), tgChatId).execute();
            return newLink;
        }
    }

    public TableLink save(String link, Timestamp updatedAt, Map<String, Object> updateInfo) {
        var candidates = dslContext
                .select(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).from(LINKS)
                .where(LINKS.LINK.eq(link)).fetch().map(linkFieldsMapper);

        var updateInfoJson = mapToJson(updateInfo);

        if (!candidates.isEmpty()) {
            var currentLink = candidates.get(0);
            currentLink = new TableLink(currentLink.id(), currentLink.link(), updatedAt, updateInfo);
            return dslContext
                    .update(LINKS)
                    .set(LINKS.LINK, currentLink.link())
                    .set(LINKS.UPDATED_AT, currentLink.updatedAt().toLocalDateTime())
                    .set(LINKS.UPDATE_INFO, JSONB.valueOf(updateInfoJson))
                    .returning(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).fetch().map(linkFieldsMapper).get(0);
        } else {
            return dslContext
                    .insertInto(LINKS).columns(LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO)
                    .values(link, updatedAt.toLocalDateTime(), JSONB.valueOf(updateInfoJson))
                    .returning(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).fetch().map(linkFieldsMapper).get(0);
        }
    }

    private String mapToJson(Map<String, Object> data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    public TableLink remove(long tgChatId, String link) {
        var candidates = dslContext
                .select(LINKS.ID, LINKS.LINK, LINKS.UPDATED_AT, LINKS.UPDATE_INFO).from(LINKS)
                .where(LINKS.LINK.eq(link)).fetch().map(linkFieldsMapper);
        if (!candidates.isEmpty()) {
            dslContext
                    .deleteFrom(TRACKINGS)
                    .where(TRACKINGS.LINK_ID.eq(candidates.get(0).id())).and(TRACKINGS.TG_CHAT_ID.eq(tgChatId))
                    .execute();
        }
        return candidates.stream().findFirst().orElse(null);
    }
}
