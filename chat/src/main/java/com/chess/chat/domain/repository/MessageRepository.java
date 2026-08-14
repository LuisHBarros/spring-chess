package com.chess.chat.domain.repository;

import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.Message;
import com.chess.chat.domain.model.MessageId;
import com.chess.chat.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface MessageRepository {
    Message save(Message message);

    Optional<Message> findById(MessageId id);

    List<Message> findByChatRoomId(ChatRoomId chatRoomId, int page, int size);

    List<Message> findUnreadMessages(ChatRoomId chatRoomId, UserId userId);

    long countUnreadMessages(ChatRoomId chatRoomId, UserId userId);

    Optional<Message> findTopByChatRoomIdOrderBySequenceDesc(ChatRoomId chatRoomId);

    void delete(Message message);

    void deleteAllByChatRoomId(ChatRoomId chatRoomId);
}
