package ru.tinkoff.edu.java.scrapper.domain.model.jpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(schema = "app", name = "chats")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
public class JpaChat {

    @Id
    @Column(name = "tg_chat_id")
    private long tgChatId;

    @Column(name = "nickname")
    private String nickname;

//    @ManyToMany(mappedBy = "trackingJpaChats")
//    private Set<JpaLink> trackedJpaLinks;
}
