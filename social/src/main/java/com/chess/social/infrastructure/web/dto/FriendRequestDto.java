package com.chess.social.infrastructure.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class FriendRequestDto {

    @NotNull(message = "RequesterId cannot be null")
    private UUID requesterId;

    @NotNull(message = "AddresseeId cannot be null")
    private UUID addresseeId;

    public FriendRequestDto() {
    }

    public FriendRequestDto(UUID requesterId, UUID addresseeId) {
        this.requesterId = requesterId;
        this.addresseeId = addresseeId;
    }

    public UUID getRequesterId() {
        return requesterId;
    }

    public void setRequesterId(UUID requesterId) {
        this.requesterId = requesterId;
    }

    public UUID getAddresseeId() {
        return addresseeId;
    }

    public void setAddresseeId(UUID addresseeId) {
        this.addresseeId = addresseeId;
    }
}
