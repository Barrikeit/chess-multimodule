package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.LinkedHashMap;
import java.util.Map;

public record Promotion(Square position, Piece piece) implements Consequence {

  @Override
  public Board applyOn(Board board) {
    Map<Square, Piece> pieces = new LinkedHashMap<>(board.getPieces());
    pieces.remove(position);
    pieces.put(position, piece);
    return board.withPieces(pieces);
  }
}
