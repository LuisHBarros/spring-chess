package com.chess.social.infrastructure.web.controller;

import com.chess.social.domain.model.Avatar;
import com.chess.social.domain.model.CategoryId;
import com.chess.social.domain.model.CategoryName;
import com.chess.social.domain.model.Guild;
import com.chess.social.domain.model.GuildCategory;
import com.chess.social.domain.model.GuildId;
import com.chess.social.domain.model.GuildMember;
import com.chess.social.domain.model.GuildName;
import com.chess.social.domain.model.GuildRank;
import com.chess.social.domain.model.RankId;
import com.chess.social.domain.model.RankName;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.GuildRepository;
import com.chess.social.domain.service.GuildDomainService;
import com.chess.social.infrastructure.web.dto.AddMemberRequestDto;
import com.chess.social.infrastructure.web.dto.ApiResponseDto;
import com.chess.social.infrastructure.web.dto.CategoryRequestDto;
import com.chess.social.infrastructure.web.dto.CreateGuildRequestDto;
import com.chess.social.infrastructure.web.dto.GuildResponseDto;
import com.chess.social.infrastructure.web.dto.RankRequestDto;
import com.chess.social.infrastructure.web.dto.TransferOwnershipRequestDto;
import com.chess.social.infrastructure.web.dto.UpdateAvatarRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/guilds")
@Tag(name = "Guilds", description = "Endpoints for managing Guilds, Categories, Ranks, and Guild Memberships")
public class GuildController {

    private final GuildDomainService guildDomainService;
    private final GuildRepository guildRepository;

    public GuildController(GuildDomainService guildDomainService, GuildRepository guildRepository) {
        this.guildDomainService = guildDomainService;
        this.guildRepository = guildRepository;
    }

    @PostMapping
    @Operation(summary = "Create a new Guild", description = "Creates a new Guild with an Owner and default General category")
    public ResponseEntity<ApiResponseDto<GuildResponseDto>> createGuild(@Valid @RequestBody CreateGuildRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getCreatorId());
        Avatar avatar = dto.getAvatarUrl() != null && !dto.getAvatarUrl().isBlank()
                ? Avatar.of(dto.getAvatarUrl())
                : Avatar.defaultAvatar();

        Guild guild = guildDomainService.createGuild(
                GuildName.of(dto.getName()),
                dto.getDescription(),
                UserId.from(dto.getCreatorId()),
                avatar
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Guild created successfully", GuildResponseDto.fromDomain(guild)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get Guild by ID", description = "Fetches a Guild along with its categories, ranks, and members")
    public ResponseEntity<ApiResponseDto<GuildResponseDto>> getGuildById(@PathVariable("id") UUID id) {
        Guild guild = guildRepository.findById(GuildId.from(id))
                .orElseThrow(() -> new com.chess.social.domain.exception.GuildNotFoundException("Guild not found with ID: " + id));

        return ResponseEntity.ok(ApiResponseDto.success("Guild retrieved successfully", GuildResponseDto.fromDomain(guild)));
    }

    @PutMapping("/{id}/ownership")
    @Operation(summary = "Transfer Guild ownership", description = "Transfers ownership of a Guild to another member")
    public ResponseEntity<ApiResponseDto<GuildResponseDto>> transferOwnership(
            @PathVariable("id") UUID guildId,
            @Valid @RequestBody TransferOwnershipRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        Guild guild = guildDomainService.transferOwnership(
                UserId.from(dto.getActorId()),
                GuildId.from(guildId),
                UserId.from(dto.getNewOwnerId())
        );

        return ResponseEntity.ok(ApiResponseDto.success("Ownership transferred successfully", GuildResponseDto.fromDomain(guild)));
    }

    @PutMapping("/{id}/avatar")
    @Operation(summary = "Update Guild Avatar", description = "Updates the avatar icon URL for a Guild")
    public ResponseEntity<ApiResponseDto<GuildResponseDto>> updateAvatar(
            @PathVariable("id") UUID guildId,
            @Valid @RequestBody UpdateAvatarRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        Guild guild = guildDomainService.updateGuildAvatar(
                UserId.from(dto.getActorId()),
                GuildId.from(guildId),
                Avatar.of(dto.getAvatarUrl())
        );

        return ResponseEntity.ok(ApiResponseDto.success("Guild avatar updated successfully", GuildResponseDto.fromDomain(guild)));
    }

    @PostMapping("/{id}/categories")
    @Operation(summary = "Add a Category to Guild", description = "Creates a new customized category within a Guild")
    public ResponseEntity<ApiResponseDto<GuildResponseDto.CategoryResponseDto>> addCategory(
            @PathVariable("id") UUID guildId,
            @Valid @RequestBody CategoryRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        GuildCategory category = guildDomainService.addCategory(
                UserId.from(dto.getActorId()),
                GuildId.from(guildId),
                CategoryName.of(dto.getName()),
                dto.getDescription()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Category added successfully", GuildResponseDto.CategoryResponseDto.fromDomain(category)));
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    @Operation(summary = "Remove a Category from Guild", description = "Deletes a category from a Guild")
    public ResponseEntity<ApiResponseDto<Void>> removeCategory(
            @PathVariable("id") UUID guildId,
            @PathVariable("categoryId") UUID categoryId,
            @RequestParam("actorId") UUID actorId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        guildDomainService.removeCategory(
                UserId.from(actorId),
                GuildId.from(guildId),
                CategoryId.from(categoryId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Category removed successfully", null));
    }

    @PostMapping("/{id}/categories/{categoryId}/ranks")
    @Operation(summary = "Add a Rank to Category", description = "Creates a new customized rank inside a specific Guild category")
    public ResponseEntity<ApiResponseDto<GuildResponseDto.RankResponseDto>> addRankToCategory(
            @PathVariable("id") UUID guildId,
            @PathVariable("categoryId") UUID categoryId,
            @Valid @RequestBody RankRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        GuildRank rank = guildDomainService.addRankToCategory(
                UserId.from(dto.getActorId()),
                GuildId.from(guildId),
                CategoryId.from(categoryId),
                RankName.of(dto.getName()),
                dto.getPriority(),
                dto.getPermissions()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Rank added successfully", GuildResponseDto.RankResponseDto.fromDomain(rank)));
    }

    @DeleteMapping("/{id}/categories/{categoryId}/ranks/{rankId}")
    @Operation(summary = "Remove a Rank from Category", description = "Deletes a rank from a Guild category")
    public ResponseEntity<ApiResponseDto<Void>> removeRankFromCategory(
            @PathVariable("id") UUID guildId,
            @PathVariable("categoryId") UUID categoryId,
            @PathVariable("rankId") UUID rankId,
            @RequestParam("actorId") UUID actorId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        guildDomainService.removeRankFromCategory(
                UserId.from(actorId),
                GuildId.from(guildId),
                CategoryId.from(categoryId),
                RankId.from(rankId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Rank removed successfully", null));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "Add Member to Guild", description = "Enrolls a new member in a Guild with optional initial rank")
    public ResponseEntity<ApiResponseDto<GuildResponseDto.GuildMemberResponseDto>> addMember(
            @PathVariable("id") UUID guildId,
            @Valid @RequestBody AddMemberRequestDto dto) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(dto.getActorId());
        GuildMember member = guildDomainService.addMember(
                UserId.from(dto.getActorId()),
                GuildId.from(guildId),
                UserId.from(dto.getNewMemberId()),
                dto.getInitialRankId() != null ? RankId.from(dto.getInitialRankId()) : null
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDto.success("Member added successfully", GuildResponseDto.GuildMemberResponseDto.fromDomain(member)));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @Operation(summary = "Remove Member from Guild", description = "Removes a member from a Guild")
    public ResponseEntity<ApiResponseDto<Void>> removeMember(
            @PathVariable("id") UUID guildId,
            @PathVariable("memberId") UUID targetMemberId,
            @RequestParam("actorId") UUID actorId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        guildDomainService.removeMember(
                UserId.from(actorId),
                GuildId.from(guildId),
                UserId.from(targetMemberId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Member removed successfully", null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete Guild", description = "Deletes a Guild (Owner only)")
    public ResponseEntity<ApiResponseDto<Void>> deleteGuild(
            @PathVariable("id") UUID guildId,
            @RequestParam("actorId") UUID actorId) {
        com.chess.social.infrastructure.security.SecurityUtils.validateUserIdentity(actorId);
        guildDomainService.deleteGuild(
                UserId.from(actorId),
                GuildId.from(guildId)
        );

        return ResponseEntity.ok(ApiResponseDto.success("Guild deleted successfully", null));
    }
}
