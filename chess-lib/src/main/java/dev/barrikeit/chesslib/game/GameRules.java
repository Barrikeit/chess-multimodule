package dev.barrikeit.chesslib.game;

import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.pieces.Bishop;
import dev.barrikeit.chesslib.pieces.King;
import dev.barrikeit.chesslib.pieces.Knight;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

public final class GameRules {

  private GameRules() {}

  public static boolean hasThreefoldRepetition(List<GameSnapshotState> states) {
    return states.stream().anyMatch(state -> Collections.frequency(states, state) > 2);
  }

  public static boolean hasInsufficientMaterial(Map<Square, Piece> pieces) {
    int size = pieces.size();
    boolean hasWhiteKing = hasWhiteKing(pieces.values());
    boolean hasBlackKing = hasBlackKing(pieces.values());
    if (size == 2 && hasWhiteKing && hasBlackKing) return true;
    if (size == 3 && hasWhiteKing && hasBlackKing && hasBishop(pieces.values())) return true;
    if (size == 3 && hasWhiteKing && hasBlackKing && hasKnight(pieces.values())) return true;
    return size == 4 && hasWhiteKing && hasBlackKing && hasBishopsOnSameColor(pieces);
  }

  private static boolean hasWhiteKing(Collection<Piece> pieces) {
    return pieces.stream().anyMatch(piece -> piece.getSide() == Side.WHITE && piece instanceof King);
  }

  private static boolean hasBlackKing(Collection<Piece> pieces) {
    return pieces.stream().anyMatch(piece -> piece.getSide() == Side.BLACK && piece instanceof King);
  }

  private static boolean hasBishop(Collection<Piece> pieces) {
    return pieces.stream().anyMatch(piece -> piece instanceof Bishop);
  }

  private static boolean hasKnight(Collection<Piece> pieces) {
    return pieces.stream().anyMatch(piece -> piece instanceof Knight);
  }

  private static boolean hasBishopsOnSameColor(Map<Square, Piece> pieces) {
    List<Square> bishopSquares = pieces.entrySet().stream()
        .filter(entry -> entry.getValue() instanceof Bishop)
        .map(Map.Entry::getKey)
        .toList();

    return bishopSquares.size() > 1
        && (bishopSquares.stream().allMatch(Square::isLightSquare)
            || bishopSquares.stream().allMatch(Square::isDarkSquare));
  }

  public static BoardMove applyAmbiguity(BoardMove boardMove, GameSnapshotState gameSnapshotState) {
    List<Square> similarPieceSquares = gameSnapshotState.getBoard().pieces(gameSnapshotState.getTurn())
        .entrySet().stream()
        .filter(entry -> entry.getValue().getFenSymbol().equals(boardMove.getPiece().getFenSymbol()))
        .flatMap(entry -> entry.getValue().pseudoLegalMoves(gameSnapshotState, false).stream())
        .filter(move -> move.getTo() == boardMove.getTo())
        .map(BoardMove::getFrom)
        .distinct()
        .toList();

    if (similarPieceSquares.size() == 1) return boardMove;

    EnumSet<BoardMove.Ambiguity> ambiguity = EnumSet.noneOf(BoardMove.Ambiguity.class);
    List<Square> onSameFile = similarPieceSquares.stream()
        .filter(square -> square.getFile() == boardMove.getFrom().getFile())
        .toList();
    if (onSameFile.size() == 1) {
      ambiguity.add(BoardMove.Ambiguity.AMBIGUOUS_FILE);
    } else {
      List<Square> onSameRank = similarPieceSquares.stream()
          .filter(square -> square.getRank() == boardMove.getFrom().getRank())
          .toList();
      if (onSameRank.size() == 1) {
        ambiguity.add(BoardMove.Ambiguity.AMBIGUOUS_RANK);
      } else {
        ambiguity.add(BoardMove.Ambiguity.AMBIGUOUS_FILE);
        ambiguity.add(BoardMove.Ambiguity.AMBIGUOUS_RANK);
      }
    }
    return boardMove.withAmbiguity(ambiguity);
  }
}
