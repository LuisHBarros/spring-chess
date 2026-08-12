package com.chess.social.infrastructure.web.controller;

import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.service.FriendshipDomainService;
import com.chess.social.infrastructure.web.dto.ApiResponseDto;
import com.chess.social.infrastructure.web.dto.FriendRequestDto;
import com.chess.social.infrastructure.web.dto.FriendshipResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/friendships")
@Tag(name = "Friendships", description = "Endpoints for managing user friend requests and relationships")
public class FriendshipController {

    private final FriendshipDomainService friendshipDomainService;

    public FriendshipController(FriendshipDomainService friendshipDomainService) {
        this.friendshipDomainService = friendshipDomainService;
    }

    @PostMapping("/request")
    @Operation(summary = "Send a friend request", description = "Sends a new friend request from requester to addressee")
    public ResponseEntity<ApiResponseDto<FriendshipResponseDto>> sendFriendRequest(
            @Valid @RequestBody FriendRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getRequesterId());
        Friendship friendship = friendshipDomainService.sendFriendRequest(
                UserId.from(dto.getRequesterId()),
                UserId.from(dto.getAddresseeId())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Friend request sent successfully", FriendshipResponseDto.fromDomain(friendship)));
    }

    @PutMapping("/{id}/accept")
    @Operation(summary = "Accept a friend request", description = "Accepts a pending friend request")
    public ResponseEntity<ApiResponseDto<FriendshipResponseDto>> acceptFriendRequest(
            @PathVariable("id") UUID friendshipId,
            @RequestParam("addresseeId") UUID addresseeId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(addresseeId);
        Friendship friendship = friendshipDomainService.acceptFriendRequest(
                UserId.from(addresseeId),
                FriendshipId.from(friendshipId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Friend request accepted", FriendshipResponseDto.fromDomain(friendship)));
    }

    @PutMapping("/{id}/decline")
    @Operation(summary = "Decline a friend request", description = "Declines a pending friend request")
    public ResponseEntity<ApiResponseDto<FriendshipResponseDto>> declineFriendRequest(
            @PathVariable("id") UUID friendshipId,
            @RequestParam("addresseeId") UUID addresseeId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(addresseeId);
        Friendship friendship = friendshipDomainService.declineFriendRequest(
                UserId.from(addresseeId),
                FriendshipId.from(friendshipId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Friend request declined", FriendshipResponseDto.fromDomain(friendship)));
    }

    @PostMapping("/block")
    @Operation(summary = "Block a user", description = "Blocks any friendship relationship between actor and target user")
    public ResponseEntity<ApiResponseDto<FriendshipResponseDto>> blockUser(
            @RequestParam("actorId") UUID actorId,
            @RequestParam("targetUserId") UUID targetUserId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        Friendship friendship = friendshipDomainService.blockUser(
                UserId.from(actorId),
                UserId.from(targetUserId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("User blocked successfully", FriendshipResponseDto.fromDomain(friendship)));
    }

    @PostMapping("/unblock")
    @Operation(summary = "Unblock a user", description = "Unblocks a blocked user relationship")
    public ResponseEntity<ApiResponseDto<FriendshipResponseDto>> unblockUser(
            @RequestParam("actorId") UUID actorId,
            @RequestParam("targetUserId") UUID targetUserId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        Friendship friendship = friendshipDomainService.unblockUser(
                UserId.from(actorId),
                UserId.from(targetUserId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("User unblocked successfully", FriendshipResponseDto.fromDomain(friendship)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remove a friendship", description = "Deletes an existing friendship relationship")
    public ResponseEntity<ApiResponseDto<Void>> removeFriendship(
            @PathVariable("id") UUID friendshipId,
            @RequestParam("actorId") UUID actorId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        friendshipDomainService.removeFriendship(
                UserId.from(actorId),
                FriendshipId.from(friendshipId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Friendship removed successfully", null));
    }
}
