package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.pieces.Piece;
import dev.barrikeit.chesslib.Side;

/** A move applied to the board. */
public record AppliedMove(BoardMove boardMove, MoveEffect effect) {

  public AppliedMove(BoardMove boardMove) {
    this(boardMove, null);
  }

  public SimpleMove move() {
    return boardMove.getMove();
  }

  public Square from() {
    return boardMove.getFrom();
  }

  public Square to() {
    return boardMove.getTo();
  }

  public Piece piece() {
    return boardMove.getPiece();
  }

  @Override
  public String toString() {
    return toString(true, true);
  }

  public String toString(boolean useFigurineNotation, boolean includeResult) {
    String postFix;
    if (effect == MoveEffect.CHECK) {
      postFix = "+";
    } else if (includeResult && effect == MoveEffect.CHECKMATE) {
      postFix = "#  " + (piece().getSide() == Side.WHITE ? "1-0" : "0-1");
    } else if (includeResult && effect == MoveEffect.DRAW) {
      postFix = "  ½ - ½";
    } else {
      postFix = "";
    }
    return boardMove.toString(useFigurineNotation) + postFix;
  }
}
