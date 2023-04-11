CREATE SCHEMA IF NOT EXISTS app;

CREATE TABLE IF NOT EXISTS app.chats
(
    tg_chat_id bigint NOT NULL,
    nickname varchar(70) NOT NULL,
    CONSTRAINT chats_pkey PRIMARY KEY (tg_chat_id),
    CONSTRAINT unique_name UNIQUE (nickname)
);

CREATE TABLE IF NOT EXISTS app.links
(
    id serial primary key,
    link varchar(200) NOT NULL,
    CONSTRAINT unique_link UNIQUE (link)
);

CREATE TABLE IF NOT EXISTS app.trackings
(
    tg_chat_id bigint NOT NULL,
    link_id bigint NOT NULL,
    CONSTRAINT "trackings_PK" PRIMARY KEY (tg_chat_id, link_id),
    CONSTRAINT "chat_FK" FOREIGN KEY (tg_chat_id)
        REFERENCES app.chats (tg_chat_id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT "link_FK" FOREIGN KEY (tg_chat_id)
        REFERENCES app.links (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);