package com.chess.game.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GameClockTest {

    @Test
    @DisplayName("Should initialize GameClock with initial time and increment")
    void shouldInitializeGameClock() {
        GameClock clock = GameClock.create(600, 5);

        assertThat(clock.getInitialTimeSeconds()).isEqualTo(600);
        assertThat(clock.getIncrementSeconds()).isEqualTo(5);
        assertThat(clock.getWhiteTimeRemainingMs()).isEqualTo(600000L);
        assertThat(clock.getBlackTimeRemainingMs()).isEqualTo(600000L);
        assertThat(clock.isTimeUp(Color.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Should decrement time and add increment")
    void shouldDecrementAndIncrement() {
        GameClock clock = GameClock.create(600, 5);

        // White uses 10 seconds (10,000 ms)
        GameClock afterMove = clock.decrementTime(Color.WHITE, 10000L);
        assertThat(afterMove.getWhiteTimeRemainingMs()).isEqualTo(590000L);

        // Add 5s (5,000 ms) increment
        GameClock withIncrement = afterMove.addIncrement(Color.WHITE);
        assertThat(withIncrement.getWhiteTimeRemainingMs()).isEqualTo(595000L);
    }

    @Test
    @DisplayName("Should detect when player time is up")
    void shouldDetectTimeUp() {
        GameClock clock = GameClock.create(10, 0); // 10s clock
        GameClock timeUpClock = clock.decrementTime(Color.BLACK, 10000L);

        assertThat(timeUpClock.isTimeUp(Color.BLACK)).isTrue();
        assertThat(timeUpClock.isTimeUp(Color.WHITE)).isFalse();
    }

    @Test
    @DisplayName("Should reject negative initial time or increment")
    void shouldRejectNegativeValues() {
        assertThatThrownBy(() -> GameClock.create(-1, 5))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> GameClock.create(600, -1))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
