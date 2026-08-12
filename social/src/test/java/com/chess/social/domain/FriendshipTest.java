package com.chess.social.domain.test;

import com.chess.social.domain.exception.InvalidFriendshipTransitionException;
import com.chess.social.domain.exception.SelfFriendshipException;
import com.chess.social.domain.model.Friendship;
import com.chess.social.domain.model.FriendshipStatus;
import com.chess.social.domain.model.UserId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FriendshipTest {

    @Test
    @DisplayName("Should successfully create a pending friend request")
    void shouldCreatePendingFriendRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();

        Friendship friendship = Friendship.request(requester, addressee);

        assertNotNull(friendship.getId());
        assertEquals(requester, friendship.getRequesterId());
        assertEquals(addressee, friendship.getAddresseeId());
        assertEquals(FriendshipStatus.PENDING, friendship.getStatus());
        assertEquals(requester, friendship.getActionUserId());
        assertTrue(friendship.isParticipant(requester));
        assertTrue(friendship.isParticipant(addressee));
        assertTrue(friendship.isBetween(requester, addressee));
    }

    @Test
    @DisplayName("Should throw exception when sending friend request to oneself")
    void shouldThrowWhenSendingRequestToSelf() {
        UserId user = UserId.generate();

        assertThrows(SelfFriendshipException.class, () -> Friendship.request(user, user));
    }

    @Test
    @DisplayName("Should allow recipient to accept pending request")
    void shouldAllowRecipientToAcceptRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        friendship.accept(addressee);

        assertEquals(FriendshipStatus.ACCEPTED, friendship.getStatus());
        assertEquals(addressee, friendship.getActionUserId());
    }

    @Test
    @DisplayName("Should throw exception when requester tries to accept own request")
    void shouldThrowWhenRequesterAcceptsOwnRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        assertThrows(InvalidFriendshipTransitionException.class, () -> friendship.accept(requester));
    }

    @Test
    @DisplayName("Should allow recipient to decline pending request")
    void shouldAllowRecipientToDeclineRequest() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        friendship.decline(addressee);

        assertEquals(FriendshipStatus.DECLINED, friendship.getStatus());
        assertEquals(addressee, friendship.getActionUserId());
    }

    @Test
    @DisplayName("Should allow any participant to block friendship")
    void shouldAllowParticipantToBlockFriendship() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);

        friendship.block(requester);

        assertEquals(FriendshipStatus.BLOCKED, friendship.getStatus());
        assertEquals(requester, friendship.getActionUserId());
    }

    @Test
    @DisplayName("Should allow user who blocked to unblock friendship")
    void shouldAllowUserWhoBlockedToUnblock() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);
        friendship.block(requester);

        friendship.unblock(requester);

        assertEquals(FriendshipStatus.DECLINED, friendship.getStatus());
    }

    @Test
    @DisplayName("Should throw exception if non-blocker attempts to unblock")
    void shouldThrowIfNonBlockerAttemptsUnblock() {
        UserId requester = UserId.generate();
        UserId addressee = UserId.generate();
        Friendship friendship = Friendship.request(requester, addressee);
        friendship.block(requester);

        assertThrows(InvalidFriendshipTransitionException.class, () -> friendship.unblock(addressee));
    }
}
