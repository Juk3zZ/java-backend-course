package ru.tinkoff.edu.java.scrapper.domain.jooq.repository;

import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;
import ru.tinkoff.edu.java.scrapper.domain.model.Chat;

import java.util.List;

import static ru.tinkoff.edu.java.scrapper.domain.jooq.Tables.CHATS;

@Repository
public class JooqChatRepository {

    private final DSLContext dslContext;

    public JooqChatRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<Chat> findAll() {
        return dslContext
                .selectFrom(CHATS)
                .fetch().map(chatsRecord -> new Chat(chatsRecord.getTgChatId(), chatsRecord.getNickname()));
    }

    public Chat findById(long tgChatId) {
        var chatsRecord = dslContext
                .selectFrom(CHATS)
                .where(CHATS.TG_CHAT_ID.eq(tgChatId))
                .fetchOne();
        return chatsRecord == null ?
                null : chatsRecord.map(record -> new Chat(record.get(CHATS.TG_CHAT_ID), record.get(CHATS.NICKNAME)));
    }

    public boolean add(Chat chat) {
        return dslContext
                .insertInto(CHATS).columns(CHATS.TG_CHAT_ID, CHATS.NICKNAME)
                .values(chat.tgChatId(), chat.nickname()).execute() != 0;
    }

    public boolean remove(long tgChatId) {
        return dslContext
                .delete(CHATS).where(CHATS.TG_CHAT_ID.eq(tgChatId)).execute() != 0;
    }
}
