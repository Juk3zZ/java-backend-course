package ru.tinkoff.edu.java.scrapper.domain.jooq.repository;

import org.jooq.DSLContext;
import ru.tinkoff.edu.java.scrapper.domain.model.TableChat;

import java.util.List;

import static ru.tinkoff.edu.java.scrapper.domain.jooq.Tables.CHATS;

public class JooqChatRepository {

    private final DSLContext dslContext;

    public JooqChatRepository(DSLContext dslContext) {
        this.dslContext = dslContext;
    }

    public List<TableChat> findAll() {
        return dslContext
                .selectFrom(CHATS)
                .fetch().map(chatsRecord -> new TableChat(chatsRecord.getTgChatId(), chatsRecord.getNickname()));
    }

    public TableChat findById(long tgChatId) {
        var chatsRecord = dslContext
                .selectFrom(CHATS)
                .where(CHATS.TG_CHAT_ID.eq(tgChatId))
                .fetchOne();
        return chatsRecord == null ?
                null : chatsRecord.map(record -> new TableChat(record.get(CHATS.TG_CHAT_ID), record.get(CHATS.NICKNAME)));
    }

    public boolean add(TableChat chat) {
        return dslContext
                .insertInto(CHATS).columns(CHATS.TG_CHAT_ID, CHATS.NICKNAME)
                .values(chat.tgChatId(), chat.nickname()).execute() != 0;
    }

    public boolean remove(long tgChatId) {
        return dslContext
                .delete(CHATS).where(CHATS.TG_CHAT_ID.eq(tgChatId)).execute() != 0;
    }
}
