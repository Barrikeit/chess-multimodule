package dev.barrikeit.chesslib.pieces;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.moves.AppliedMove;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.Capture;
import dev.barrikeit.chesslib.moves.Move;
import dev.barrikeit.chesslib.moves.Promotion;
import java.util.ArrayList;
import java.util.List;

public class Pawn implements Piece {

  private final Side side;
  private final String fanSymbol;
  private final String fenSymbol;

  public Pawn(Side side) {
    this.side = side;
    this.fanSymbol = side == Side.WHITE ? "♙" : "♟";
    this.fenSymbol = side == Side.WHITE ? "P" : "p";
  }

  @Override
  public Side getSide() {
    return side;
  }

  @Override
  public int getValue() {
    return 1;
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
    Board board = gameSnapshotState.getBoard();
    Square square = board.find(this);
    if (square == null) return List.of();

    List<BoardMove> moves = new ArrayList<>();
    BoardMove single = advanceSingle(board, square);
    if (single != null) moves.add(single);
    BoardMove twoSquares = advanceTwoSquares(board, square);
    if (twoSquares != null) moves.add(twoSquares);
    BoardMove captureLeft = captureDiagonal(board, square, -1);
    if (captureLeft != null) moves.add(captureLeft);
    BoardMove captureRight = captureDiagonal(board, square, 1);
    if (captureRight != null) moves.add(captureRight);
    BoardMove enPassantLeft = enPassantDiagonal(gameSnapshotState, square, -1);
    if (enPassantLeft != null) moves.add(enPassantLeft);
    BoardMove enPassantRight = enPassantDiagonal(gameSnapshotState, square, 1);
    if (enPassantRight != null) moves.add(enPassantRight);

    List<BoardMove> result = new ArrayList<>();
    for (BoardMove move : moves) result.addAll(checkForPromotion(move));
    return result;
  }

  private BoardMove advanceSingle(Board board, Square square) {
    int rankOffset = side == Side.WHITE ? 1 : -1;
    Square target = board.getSquare(square.file(), square.rank() + rankOffset);
    return target != null && board.isEmpty(target)
        ? new BoardMove(new Move(this, square, target))
        : null;
  }

  private BoardMove advanceTwoSquares(Board board, Square square) {
    // Starting rank, 0-based: rank 1 (2nd rank) for white, rank 6 (7th rank) for black.
    boolean onStartingRank = (side == Side.WHITE && square.rank() == 1) || (side == Side.BLACK && square.rank() == 6);
    if (!onStartingRank) return null;

    int rankOffset1 = side == Side.WHITE ? 1 : -1;
    int rankOffset2 = side == Side.WHITE ? 2 : -2;
    Square target1 = board.getSquare(square.file(), square.rank() + rankOffset1);
    Square target2 = board.getSquare(square.file(), square.rank() + rankOffset2);
    return target1 != null && board.isEmpty(target1) && target2 != null && board.isEmpty(target2)
        ? new BoardMove(new Move(this, square, target2))
        : null;
  }

  private BoardMove captureDiagonal(Board board, Square square, int fileOffset) {
    int rankOffset = side == Side.WHITE ? 1 : -1;
    Square target = board.getSquare(square.file() + fileOffset, square.rank() + rankOffset);
    if (target == null || !board.hasPiece(target, side.flip())) return null;

    return new BoardMove(
        new Move(this, square, target),
        new Capture(board.getPiece(target), target),
        null);
  }

  private BoardMove enPassantDiagonal(GameSnapshotState gameSnapshotState, Square square, int fileOffset) {
    Board board = gameSnapshotState.getBoard();
    // En-passant capturing rank, 0-based: rank 4 (5th rank) for white, rank 3 (4th rank) for black.
    int enPassantRank = side == Side.WHITE ? 4 : 3;
    if (square.rank() != enPassantRank) return null;

    AppliedMove lastMove = gameSnapshotState.getLastMove();
    if (lastMove == null || !(lastMove.piece() instanceof Pawn)) return null;

    // Pawn's own starting rank, 0-based: rank 6 (7th rank) for white, rank 1 (2nd rank) for black.
    int initialRank = side == Side.WHITE ? 6 : 1;
    boolean fromInitialSquare = lastMove.from().rank() == initialRank;
    boolean twoSquareMove = lastMove.to().rank() == square.rank();
    boolean isOnNextFile = lastMove.to().file() == square.file() + fileOffset;
    if (!fromInitialSquare || !twoSquareMove || !isOnNextFile) return null;

    int rankOffset = side == Side.WHITE ? 1 : -1;
    Square enPassantTarget = board.getSquare(square.file() + fileOffset, square.rank() + rankOffset);
    Square capturedPieceSquare = board.getSquare(square.file() + fileOffset, square.rank());
    if (enPassantTarget == null || capturedPieceSquare == null) {
      throw new IllegalStateException("En passant target/captured squares must exist");
    }

    return new BoardMove(
        new Move(this, square, enPassantTarget),
        new Capture(board.getPiece(capturedPieceSquare), capturedPieceSquare),
        null);
  }

  private static List<BoardMove> checkForPromotion(BoardMove move) {
    // Back rank, 0-based: rank 7 (8th rank) for white, rank 0 (1st rank) for black.
    int promotionRank = move.getPiece().getSide() == Side.WHITE ? 7 : 0;
    if (move.getTo().rank() != promotionRank) return List.of(move);

    Side side = move.getPiece().getSide();
    return List.of(
        move.withConsequence(new Promotion(move.getTo(), new Queen(side))),
        move.withConsequence(new Promotion(move.getTo(), new Rook(side))),
        move.withConsequence(new Promotion(move.getTo(), new Bishop(side))),
        move.withConsequence(new Promotion(move.getTo(), new Knight(side))));
  }
}
