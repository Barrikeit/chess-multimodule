package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.pieces.Offset;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.ArrayList;
import java.util.List;

public final class Moves {

  private Moves() {}

  public static List<Square> targetPositions(List<BoardMove> moves) {
    return moves.stream().map(BoardMove::getTo).toList();
  }

  /** Simple, single-step move of a piece. */
  public static BoardMove singleMove(Piece piece, GameSnapshotState gameSnapshotState, int fileOffset, int rankOffset) {
    Board board = gameSnapshotState.getBoard();
    Square square = board.find(piece);
    if (square == null) return null;

    Square target = board.getSquare(square.file() + fileOffset, square.rank() + rankOffset);
    if (target == null || board.hasPiece(target, piece.getSide())) return null;

    Piece targetPiece = board.getPiece(target);
    PreMove preMove = targetPiece != null ? new Capture(targetPiece, target) : null;
    return new BoardMove(new Move(piece, square, target), preMove, null);
  }

  /** All the moves in every given direction for a piece, stopping at the first blocking piece. */
  public static List<BoardMove> lineMoves(Piece piece, GameSnapshotState gameSnapshotState, List<Offset> directions) {
    Board board = gameSnapshotState.getBoard();
    Square square = board.find(piece);
    if (square == null) return List.of();

    List<BoardMove> moves = new ArrayList<>();
    for (Offset offset : directions) {
      moves.addAll(lineMoves(board, piece, square, offset.getFileOffset(), offset.getRankOffset()));
    }
    return moves;
  }

  private static List<BoardMove> lineMoves(Board board, Piece piece, Square square, int deltaFile, int deltaRank) {
    List<BoardMove> moves = new ArrayList<>();
    int i = 0;
    while (true) {
      i++;
      Square target = board.getSquare(square.file() + deltaFile * i, square.rank() + deltaRank * i);
      if (target == null) break;
      if (board.hasPiece(target, piece.getSide())) break;

      Move move = new Move(piece, square, target);
      Piece targetPiece = board.getPiece(target);
      if (targetPiece == null) {
        moves.add(new BoardMove(move));
        continue;
      }
      moves.add(new BoardMove(move, new Capture(targetPiece, target), null));
      break;
    }
    return moves;
  }
}
