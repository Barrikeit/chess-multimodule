package dev.barrikeit.chesslib.game;

import dev.barrikeit.chesslib.moves.AppliedMove;

/** The transition from one game state to the next, and the move that caused it. */
public record GameStateTransition(
    GameSnapshotState fromGameState, GameSnapshotState toGameState, AppliedMove move) {}
