package dev.barrikeit.chesslib.pieces;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.File;
import dev.barrikeit.chesslib.Rank;
import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.KingSideCastle;
import dev.barrikeit.chesslib.moves.Move;
import dev.barrikeit.chesslib.moves.Moves;
import dev.barrikeit.chesslib.moves.QueenSideCastle;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class King implements Piece {

  public static final List<Offset> TARGET_LOCATIONS =
      Stream.concat(Bishop.OFFSETS.stream(), Rook.OFFSETS.stream()).toList();

  private final Side side;
  private final String fanSymbol;
  private final String fenSymbol;

  public King(Side side) {
    this.side = side;
    this.fanSymbol = side == Side.WHITE ? "♔" : "♚";
    this.fenSymbol = side == Side.WHITE ? "K" : "k";
  }

  @Override
  public Side getSide() {
    return side;
  }

  @Override
  public int getValue() {
    return Integer.MAX_VALUE;
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
    List<BoardMove> moves = new ArrayList<>();
    for (Offset offset : TARGET_LOCATIONS) {
      BoardMove move = Moves.singleMove(this, gameSnapshotState, offset.getFileOffset(), offset.getRankOffset());
      if (move != null) moves.add(move);
    }

    if (!check) {
      BoardMove kingSide = castleKingSide(gameSnapshotState);
      if (kingSide != null) moves.add(kingSide);
      BoardMove queenSide = castleQueenSide(gameSnapshotState);
      if (queenSide != null) moves.add(queenSide);
    }

    return moves;
  }

  private BoardMove castleKingSide(GameSnapshotState gameSnapshotState) {
    if (gameSnapshotState.hasCheck()) return null;
    if (!gameSnapshotState.getCastlingState().get(side).canCastleKingSide()) return null;

    Board board = gameSnapshotState.getBoard();
    Rank rank = side == Side.WHITE ? Rank.ONE : Rank.EIGHT;
    Square eSquare = Square.from(File.E, rank);
    Square fSquare = Square.from(File.F, rank);
    Square gSquare = Square.from(File.G, rank);
    Square hSquare = Square.from(File.H, rank);
    if (board.getPiece(fSquare) != null || board.getPiece(gSquare) != null) return null;
    if (gameSnapshotState.hasCheckFor(fSquare) || gameSnapshotState.hasCheckFor(gSquare)) return null;
    if (!(board.getPiece(hSquare) instanceof Rook)) return null;

    return new BoardMove(
        new KingSideCastle(this, eSquare, gSquare),
        null,
        new Move(board.getPiece(hSquare), hSquare, fSquare));
  }

  private BoardMove castleQueenSide(GameSnapshotState gameSnapshotState) {
    if (gameSnapshotState.hasCheck()) return null;
    if (!gameSnapshotState.getCastlingState().get(side).canCastleQueenSide()) return null;

    Board board = gameSnapshotState.getBoard();
    Rank rank = side == Side.WHITE ? Rank.ONE : Rank.EIGHT;
    Square eSquare = Square.from(File.E, rank);
    Square dSquare = Square.from(File.D, rank);
    Square cSquare = Square.from(File.C, rank);
    Square bSquare = Square.from(File.B, rank);
    Square aSquare = Square.from(File.A, rank);
    if (board.getPiece(dSquare) != null || board.getPiece(cSquare) != null || board.getPiece(bSquare) != null) return null;
    if (gameSnapshotState.hasCheckFor(dSquare) || gameSnapshotState.hasCheckFor(cSquare)) return null;
    if (!(board.getPiece(aSquare) instanceof Rook)) return null;

    return new BoardMove(
        new QueenSideCastle(this, eSquare, cSquare),
        null,
        new Move(board.getPiece(aSquare), aSquare, dSquare));
  }
}
