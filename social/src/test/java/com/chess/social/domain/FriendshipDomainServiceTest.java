package com.chess.social.domain;

import com.chess.social.domain.exception.FriendshipAlreadyExistsException;
import com.chess.social.domain.exception.FriendshipNotFoundException;
import com.chess.social.domain.exception.InvalidFriendshipTransitionException;
import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipId;
import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.domain.model.UserId;
import com.chess.social.domain.repository.FriendshipRepository;
import com.chess.social.domain.service.FriendshipDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FriendshipDomainServiceTest {

    @Mock
    private FriendshipRepository friendshipRepository;

    private FriendshipDomainService service;

    @BeforeEach
    void setUp() {
        service = new FriendshipDomainService(friendshipRepository);
    }

    @Test
    @DisplayName("Should successfully send friend request when no existing relationship exists")
    void shouldSendFriendRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();

        when(friendshipRepository.findBetweenUsers(requester, addressee)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship friendship = service.sendFriendRequest(requester, addressee);

        assertNotNull(friendship);
        assertEquals(requester, friendship.getRequesterId());
        assertEquals(addressee, friendship.getAddresseeId());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    @DisplayName("Should throw exception when friend request is already pending")
    void shouldThrowWhenRequestAlreadyPending() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship existing = Friendship.request(requester, addressee);

        when(friendshipRepository.findBetweenUsers(requester, addressee)).thenReturn(Optional.of(existing));

        assertThrows(FriendshipAlreadyExistsException.class, () ->
                service.sendFriendRequest(requester, addressee)
        );
    }

    @Test
    @DisplayName("Should throw exception when relationship is blocked")
    void shouldThrowWhenRelationshipIsBlocked() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship existing = Friendship.request(requester, addressee);
        existing.block(addressee);

        when(friendshipRepository.findBetweenUsers(requester, addressee)).thenReturn(Optional.of(existing));

        assertThrows(InvalidFriendshipTransitionException.class, () ->
                service.sendFriendRequest(requester, addressee)
        );
    }

    @Test
    @DisplayName("Should accept friend request via service")
    void shouldAcceptFriendRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        when(friendshipRepository.findById(friendship.getId())).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship result = service.acceptFriendRequest(addressee, friendship.getId());

        assertEquals(FriendshipStatus.ACCEPTED, result.getStatus());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    @DisplayName("Should decline friend request via service")
    void shouldDeclineFriendRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        when(friendshipRepository.findById(friendship.getId())).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship result = service.declineFriendRequest(addressee, friendship.getId());

        assertEquals(FriendshipStatus.DECLINED, result.getStatus());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    @DisplayName("Should block user via service")
    void shouldBlockUser() {
        UserId actor = UserId.generate();
        UserId target = UserId.generate();

        when(friendshipRepository.findBetweenUsers(actor, target)).thenReturn(Optional.empty());
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship result = service.blockUser(actor, target);

        assertEquals(FriendshipStatus.BLOCKED, result.getStatus());
        assertEquals(actor, result.getActionUserId());
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    @DisplayName("Should unblock user via service")
    void shouldUnblockUser() {
        UserId actor = UserId.generate();
        UserId target = UserId.generate();
        Friendship friendship = Friendship.request(actor, target);
        friendship.block(actor);

        when(friendshipRepository.findBetweenUsers(actor, target)).thenReturn(Optional.of(friendship));
        when(friendshipRepository.save(any(Friendship.class))).thenAnswer(inv -> inv.getArgument(0));

        Friendship result = service.unblockUser(actor, target);

        assertEquals(FriendshipStatus.DECLINED, result.getStatus());
        verify(friendshipRepository).save(friendship);
    }

    @Test
    @DisplayName("Should remove friendship via service")
    void shouldRemoveFriendship() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        when(friendshipRepository.findById(friendship.getId())).thenReturn(Optional.of(friendship));

        service.removeFriendship(requester, friendship.getId());

        verify(friendshipRepository).delete(friendship);
    }

    @Test
    @DisplayName("Should throw exception when non-participant tries to remove friendship")
    void shouldThrowWhenNonParticipantRemovesFriendship() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        UserId outsider = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        when(friendshipRepository.findById(friendship.getId())).thenReturn(Optional.of(friendship));

        assertThrows(InvalidFriendshipTransitionException.class, () ->
                service.removeFriendship(outsider, friendship.getId())
        );
        verify(friendshipRepository, never()).delete(any());
    }
}
