package dev.barrikeit.chesslib;

import dev.barrikeit.chesslib.pieces.Piece;

public interface BoardEvent {
  Piece piece();

  Board applyOn(Board board);
}
