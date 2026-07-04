package dev.barrikeit.chesslib.game;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.moves.AppliedMove;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.Move;
import dev.barrikeit.chesslib.pieces.Pawn;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.LinkedHashMap;
import java.util.Map;

/** FEN (Forsyth-Edwards Notation) encode/decode for a {@link GameSnapshotState}. */
public final class Fen {

  private Fen() {}

  public static String encode(GameSnapshotState state) {
    return encodePiecePlacement(state.getBoard())
        + ' ' + (state.getTurn() == Side.WHITE ? 'w' : 'b')
        + ' ' + encodeCastlingRights(state.getCastlingState())
        + ' ' + encodeEnPassantTarget(state.getLastMove())
        + ' ' + state.getHalfMoveClock()
        + ' ' + state.getFullMoveNumber();
  }

  public static GameSnapshotState decode(String fen) {
    String[] fields = fen.trim().split("\\s+");
    Map<Square, Piece> pieces = decodePiecePlacement(fields[0]);
    Side turn = fields[1].equals("w") ? Side.WHITE : Side.BLACK;
    CastlingState castlingState = CastlingState.fromFenRights(fields[2]);
    AppliedMove lastMove = fields[3].equals("-") ? null : synthesizeEnPassantLastMove(Square.fromValue(fields[3].toUpperCase()));
    int halfMoveClock = Integer.parseInt(fields[4]);
    int fullMoveNumber = Integer.parseInt(fields[5]);

    return GameSnapshotState.builder()
        .board(new Board(pieces))
        .turn(turn)
        .castlingState(castlingState)
        .lastMove(lastMove)
        .halfMoveClock(halfMoveClock)
        .fullMoveNumber(fullMoveNumber)
        .build();
  }

  private static String encodePiecePlacement(Board board) {
    StringBuilder sb = new StringBuilder();
    for (int rank = 7; rank >= 0; rank--) {
      int emptyCount = 0;
      for (int file = 0; file < 8; file++) {
        Piece piece = board.getPiece(Square.from(file, rank));
        if (piece == null) {
          emptyCount++;
          continue;
        }
        if (emptyCount > 0) {
          sb.append(emptyCount);
          emptyCount = 0;
        }
        sb.append(piece.getFenSymbol());
      }
      if (emptyCount > 0) sb.append(emptyCount);
      if (rank > 0) sb.append('/');
    }
    return sb.toString();
  }

  private static Map<Square, Piece> decodePiecePlacement(String placement) {
    Map<Square, Piece> pieces = new LinkedHashMap<>();
    String[] rows = placement.split("/");
    for (int rowIndex = 0; rowIndex < rows.length; rowIndex++) {
      int rank = 7 - rowIndex;
      int file = 0;
      for (char c : rows[rowIndex].toCharArray()) {
        if (Character.isDigit(c)) {
          file += Character.getNumericValue(c);
        } else {
          pieces.put(Square.from(file, rank), Piece.fromFenSymbol(c));
          file++;
        }
      }
    }
    return pieces;
  }

  private static String encodeCastlingRights(CastlingState castlingState) {
    StringBuilder sb = new StringBuilder();
    CastlingState.CastlingInfo white = castlingState.get(Side.WHITE);
    CastlingState.CastlingInfo black = castlingState.get(Side.BLACK);
    if (white.canCastleKingSide()) sb.append('K');
    if (white.canCastleQueenSide()) sb.append('Q');
    if (black.canCastleKingSide()) sb.append('k');
    if (black.canCastleQueenSide()) sb.append('q');
    return sb.isEmpty() ? "-" : sb.toString();
  }

  private static String encodeEnPassantTarget(AppliedMove lastMove) {
    if (lastMove == null || !(lastMove.piece() instanceof Pawn)) return "-";
    int fromRank = lastMove.from().rank();
    int toRank = lastMove.to().rank();
    if (Math.abs(fromRank - toRank) != 2) return "-";
    int midRank = (fromRank + toRank) / 2;
    return Square.from(lastMove.from().file(), midRank).name().toLowerCase();
  }

  /**
   * Reconstructs the pawn double-step that would produce the given en-passant target square, so
   * that {@link Pawn}'s existing en-passant logic (which reads {@code
   * GameSnapshotState.lastMove}) works unchanged for a freshly-decoded FEN position. This is
   * unambiguous: a given target rank (2 or 5, 0-based) can only be produced by one side's
   * double-step.
   */
  private static AppliedMove synthesizeEnPassantLastMove(Square target) {
    Side movingSide = target.rank() == 2 ? Side.WHITE : Side.BLACK;
    int fromRank = movingSide == Side.WHITE ? 1 : 6;
    int toRank = movingSide == Side.WHITE ? 3 : 4;
    Square from = Square.from(target.file(), fromRank);
    Square to = Square.from(target.file(), toRank);
    BoardMove boardMove = new BoardMove(new Move(new Pawn(movingSide), from, to));
    return new AppliedMove(boardMove);
  }
}
