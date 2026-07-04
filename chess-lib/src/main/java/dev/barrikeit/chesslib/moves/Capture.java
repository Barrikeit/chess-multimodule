package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.LinkedHashMap;
import java.util.Map;

public record Capture(Piece piece, Square position) implements PreMove {

  @Override
  public Board applyOn(Board board) {
    Map<Square, Piece> pieces = new LinkedHashMap<>(board.getPieces());
    pieces.remove(position);
    return board.withPieces(pieces);
  }
}
