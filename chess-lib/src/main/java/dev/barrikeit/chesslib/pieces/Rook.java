package dev.barrikeit.chesslib.pieces;

import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.Moves;
import java.util.List;

public class Rook implements Piece {

  public static final List<Offset> OFFSETS = List.of(Offset.UP, Offset.DOWN, Offset.LEFT, Offset.RIGHT);

  private final Side side;
  private final String fanSymbol;
  private final String fenSymbol;

  public Rook(Side side) {
    this.side = side;
    this.fanSymbol = side == Side.WHITE ? "♖" : "♜";
    this.fenSymbol = side == Side.WHITE ? "R" : "r";
  }

  @Override
  public Side getSide() {
    return side;
  }

  @Override
  public int getValue() {
    return 5;
  }

  @Override
  public String getFanSymbol() {
    return fanSymbol;
  }

  @Override
  public String getFenSymbol() {
    return fenSymbol;
  }

  @Override
  public List<BoardMove> pseudoLegalMoves(GameSnapshotState gameSnapshotState, boolean check) {
    return Moves.lineMoves(this, gameSnapshotState, OFFSETS);
  }
}
