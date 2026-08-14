package com.chess.game.infrastructure.persistence;

import com.chess.game.domain.model.Board;
import com.chess.game.domain.model.Color;
import com.chess.game.domain.model.Move;
import com.chess.game.domain.model.MoveType;
import com.chess.game.domain.model.Piece;
import com.chess.game.domain.model.PieceType;
import com.chess.game.domain.model.Position;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Serializes the chess {@link Board} and move history to/from JSON so that a
 * game can be persisted and reconstructed across service restarts.
 *
 * <p>Lives in the infrastructure layer to keep the domain model free of
 * framework dependencies. Legacy rows that stored {@code "{}"} / {@code "[]"}
 * fall back to an initial board / empty move list for backward compatibility.
 */
public final class GameStateSerializer {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private record PieceJson(String type, String color, String square, boolean moved) {}
    private record MoveJson(String from, String to, String type, String piece, String captured, String promotion) {}

    private GameStateSerializer() {}

    public static String serializeBoard(Board board) {
        if (board == null) {
            return "[]";
        }
        List<PieceJson> entries = board.getAllPieces().stream()
                .map(p -> new PieceJson(
                        p.getPieceType().name(),
                        p.getColor().name(),
                        p.getPosition().toAlgebraic(),
                        p.hasMoved()))
                .collect(Collectors.toList());
        return writeString(entries);
    }

    public static Board deserializeBoard(String json) {
        if (isBlankOr(json, "{}")) {
            return Board.create();
        }
        List<PieceJson> entries = readString(json, new TypeReference<List<PieceJson>>() {});
        Map<Position, Piece> pieces = new HashMap<>();
        for (PieceJson e : entries) {
            Position pos = Position.fromAlgebraic(e.square());
            pieces.put(pos, Piece.reconstitute(
                    PieceType.valueOf(e.type()),
                    Color.valueOf(e.color()),
                    pos,
                    e.moved()));
        }
        return Board.reconstitute(pieces);
    }

    public static String serializeMoves(List<Move> moves) {
        if (moves == null || moves.isEmpty()) {
            return "[]";
        }
        List<MoveJson> entries = moves.stream()
                .map(m -> new MoveJson(
                        m.getFrom().toAlgebraic(),
                        m.getTo().toAlgebraic(),
                        m.getMoveType().name(),
                        m.getPieceType().name(),
                        m.getCapturedPieceType() != null ? m.getCapturedPieceType().name() : null,
                        m.getPromotionPieceType() != null ? m.getPromotionPieceType().name() : null))
                .collect(Collectors.toList());
        return writeString(entries);
    }

    public static List<Move> deserializeMoves(String json) {
        if (isBlankOr(json, "[]")) {
            return new ArrayList<>();
        }
        List<MoveJson> entries = readString(json, new TypeReference<List<MoveJson>>() {});
        List<Move> moves = new ArrayList<>();
        for (MoveJson e : entries) {
            moves.add(Move.reconstitute(
                    Position.fromAlgebraic(e.from()),
                    Position.fromAlgebraic(e.to()),
                    PieceType.valueOf(e.piece()),
                    MoveType.valueOf(e.type()),
                    e.captured() != null ? PieceType.valueOf(e.captured()) : null,
                    e.promotion() != null ? PieceType.valueOf(e.promotion()) : null));
        }
        return moves;
    }

    private static String writeString(Object value) {
        try {
            return MAPPER.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize game state", e);
        }
    }

    private static <T> T readString(String json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to deserialize game state: " + json, e);
        }
    }

    private static boolean isBlankOr(String json, String legacyLiteral) {
        if (json == null || json.isBlank()) {
            return true;
        }
        return json.trim().equals(legacyLiteral);
    }
}
