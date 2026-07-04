package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.LinkedHashMap;
import java.util.Map;

public record QueenSideCastle(Piece piece, Square from, Square to) implements SimpleMove {

  @Override
  public Board applyOn(Board board) {
    Map<Square, Piece> pieces = new LinkedHashMap<>(board.getPieces());
    pieces.remove(from);
    pieces.put(to, piece);
    return board.withPieces(pieces);
  }
}
