package com.chess.chat.domain.repository;

import com.chess.chat.domain.model.ChatRoom;
import com.chess.chat.domain.model.ChatRoomId;
import com.chess.chat.domain.model.UserId;

import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository {
    ChatRoom save(ChatRoom chatRoom);

    Optional<ChatRoom> findById(ChatRoomId id);

    Optional<ChatRoom> findDirectRoomBetweenUsers(UserId userA, UserId userB);

    List<ChatRoom> findByParticipantUserId(UserId userId);

    List<ChatRoom> findByTargetReferenceId(String targetReferenceId);

    void delete(ChatRoom chatRoom);
}
