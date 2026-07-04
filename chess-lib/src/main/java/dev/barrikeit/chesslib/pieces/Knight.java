package dev.barrikeit.chesslib.pieces;

import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.Moves;
import java.util.List;
import java.util.Objects;

public class Knight implements Piece {

  public static final List<Offset> TARGET_LOCATIONS = List.of(
      Offset.JUMP_UP_RIGHT, Offset.JUMP_DOWN_RIGHT,
      Offset.JUMP_UP_LEFT, Offset.JUMP_DOWN_LEFT,
      Offset.JUMP_RIGHT_UP, Offset.JUMP_LEFT_UP,
      Offset.JUMP_RIGHT_DOWN, Offset.JUMP_LEFT_DOWN);

  private final Side side;
  private final String fanSymbol;
  private final String fenSymbol;

  public Knight(Side side) {
    this.side = side;
    this.fanSymbol = side == Side.WHITE ? "♘" : "♞";
    this.fenSymbol = side == Side.WHITE ? "N" : "n";
  }

  @Override
  public Side getSide() {
    return side;
  }

  @Override
  public int getValue() {
    return 3;
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
    return TARGET_LOCATIONS.stream()
        .map(offset -> Moves.singleMove(this, gameSnapshotState, offset.getFileOffset(), offset.getRankOffset()))
        .filter(Objects::nonNull)
        .toList();
  }
}
