package dev.barrikeit.chesslib.pieces;

import lombok.Getter;

@Getter
public enum Offset {
  UP(0, 1), DOWN(0, -1),
  LEFT(-1, 0), RIGHT(1, 0),
  DIAGONAL_UP_LEFT(-1, 1), DIAGONAL_UP_RIGHT(1, 1),
  DIAGONAL_DOWN_LEFT(-1, -1), DIAGONAL_DOWN_RIGHT(1, -1),
  JUMP_UP_LEFT(-1, 2), JUMP_UP_RIGHT(1, 2),
  JUMP_LEFT_UP(-2, 1), JUMP_RIGHT_UP(2, 1),
  JUMP_LEFT_DOWN(-2, -1), JUMP_RIGHT_DOWN(2, -1),
  JUMP_DOWN_LEFT(-1, -2), JUMP_DOWN_RIGHT(1, -2);

  private final int fileOffset;
  private final int rankOffset;

  Offset(int fileOffset, int rankOffset) {
    this.fileOffset = fileOffset;
    this.rankOffset = rankOffset;
  }

}
