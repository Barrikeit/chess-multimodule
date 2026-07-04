package dev.barrikeit.chesslib.game;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.pieces.King;
import dev.barrikeit.chesslib.pieces.Piece;
import dev.barrikeit.chesslib.pieces.Rook;
import java.util.Map;

/**
 * The information needed to know whether castling is available: for each side, whether the king
 * has moved, and whether the king-side / queen-side rook has moved.
 */
public record CastlingState(Map<Side, CastlingInfo> states) {

  public record CastlingInfo(
      boolean kingHasMoved, boolean kingSideRookHasMoved, boolean queenSideRookHasMoved) {

    public boolean canCastleKingSide() {
      return !kingHasMoved && !kingSideRookHasMoved;
    }

    public boolean canCastleQueenSide() {
      return !kingHasMoved && !queenSideRookHasMoved;
    }
  }

  public CastlingInfo get(Side side) {
    return states.get(side);
  }

  public CastlingState apply(BoardMove boardMove) {
    Piece piece = boardMove.getPiece();
    Side side = piece.getSide();
    CastlingInfo state = states.get(side);

    Square kingSideRookInitialSquare = side == Side.WHITE ? Square.H1 : Square.H8;
    Square queenSideRookInitialSquare = side == Side.WHITE ? Square.A1 : Square.A8;

    CastlingInfo updated = new CastlingInfo(
        state.kingHasMoved() || piece instanceof King,
        state.kingSideRookHasMoved()
            || (piece instanceof Rook && boardMove.getFrom() == kingSideRookInitialSquare),
        state.queenSideRookHasMoved()
            || (piece instanceof Rook && boardMove.getFrom() == queenSideRookInitialSquare));

    return new CastlingState(Map.of(Side.WHITE, side == Side.WHITE ? updated : states.get(Side.WHITE),
        Side.BLACK, side == Side.BLACK ? updated : states.get(Side.BLACK)));
  }

  public static CastlingState from(Board board) {
    Map<Square, Piece> white = board.pieces(Side.WHITE);
    CastlingInfo whiteCastlingInfo = new CastlingInfo(
        !(white.get(Square.E1) instanceof King),
        !(white.get(Square.H1) instanceof Rook),
        !(white.get(Square.A1) instanceof Rook));

    Map<Square, Piece> black = board.pieces(Side.BLACK);
    CastlingInfo blackCastlingInfo = new CastlingInfo(
        !(black.get(Square.E8) instanceof King),
        !(black.get(Square.H8) instanceof Rook),
        !(black.get(Square.A8) instanceof Rook));

    return new CastlingState(Map.of(Side.WHITE, whiteCastlingInfo, Side.BLACK, blackCastlingInfo));
  }

  /** Parses a FEN castling-availability field (e.g. {@code "KQkq"}, {@code "Kq"}, {@code "-"}). */
  public static CastlingState fromFenRights(String rights) {
    boolean whiteKingSide = rights.contains("K");
    boolean whiteQueenSide = rights.contains("Q");
    boolean blackKingSide = rights.contains("k");
    boolean blackQueenSide = rights.contains("q");

    CastlingInfo white = new CastlingInfo(!(whiteKingSide || whiteQueenSide), !whiteKingSide, !whiteQueenSide);
    CastlingInfo black = new CastlingInfo(!(blackKingSide || blackQueenSide), !blackKingSide, !blackQueenSide);

    return new CastlingState(Map.of(Side.WHITE, white, Side.BLACK, black));
  }
}
