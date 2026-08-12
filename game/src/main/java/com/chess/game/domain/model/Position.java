package com.chess.game.domain.model;

import java.util.Objects;

public final class Position {
    private final int file; // 0-7 (a-h)
    private final int rank; // 0-7 (1-8)

    private Position(int file, int rank) {
        if (file < 0 || file > 7 || rank < 0 || rank > 7) {
            throw new IllegalArgumentException(
                    String.format("Invalid position: file=%d, rank=%d. Must be 0-7.", file, rank));
        }
        this.file = file;
        this.rank = rank;
    }

    public static Position of(int file, int rank) {
        return new Position(file, rank);
    }

    public static Position fromAlgebraic(String notation) {
        if (notation == null || notation.length() != 2) {
            throw new IllegalArgumentException("Invalid algebraic notation: " + notation);
        }
        int file = notation.charAt(0) - 'a';
        int rank = notation.charAt(1) - '1';
        return new Position(file, rank);
    }

    public static boolean isValid(int file, int rank) {
        return file >= 0 && file <= 7 && rank >= 0 && rank <= 7;
    }

    public String toAlgebraic() {
        return "" + (char) ('a' + file) + (char) ('1' + rank);
    }

    public int getFile() { return file; }
    public int getRank() { return rank; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Position position = (Position) o;
        return file == position.file && rank == position.rank;
    }

    @Override
    public int hashCode() { return Objects.hash(file, rank); }

    @Override
    public String toString() { return toAlgebraic(); }
}
