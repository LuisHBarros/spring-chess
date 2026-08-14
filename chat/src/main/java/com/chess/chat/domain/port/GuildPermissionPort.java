package com.chess.chat.domain.port;

import com.chess.chat.domain.model.UserId;

public interface GuildPermissionPort {
    boolean hasChatAccess(UserId userId, String guildId);
}
