package dev.barrikeit.chesslib.pieces;

import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.Moves;
import java.util.List;
import java.util.stream.Stream;

public class Queen implements Piece {

  public static final List<Offset> DIRECTIONS =
      Stream.concat(Bishop.OFFSETS.stream(), Rook.OFFSETS.stream()).toList();

  private final Side side;
  private final String fanSymbol;
  private final String fenSymbol;

  public Queen(Side side) {
    this.side = side;
    this.fanSymbol = side == Side.WHITE ? "♕" : "♛";
    this.fenSymbol = side == Side.WHITE ? "Q" : "q";
  }

  @Override
  public Side getSide() {
    return side;
  }

  @Override
  public int getValue() {
    return 9;
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
    return Moves.lineMoves(this, gameSnapshotState, DIRECTIONS);
  }
}
