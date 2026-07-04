package dev.barrikeit.chesslib.pieces;

import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.game.GameSnapshotState;
import dev.barrikeit.chesslib.moves.BoardMove;
import java.util.List;

public interface Piece {
  Side getSide();

  int getValue();

  String getFanSymbol();

  String getFenSymbol();

  /** List of all possible moves for this piece without applying pin / check constraints. */
  default List<BoardMove> pseudoLegalMoves(GameSnapshotState gameSnapshotState, boolean check) {
    return List.of();
  }

  /** Builds the piece for a FEN piece-placement letter (case encodes side, e.g. {@code 'P'}/{@code 'p'}). */
  static Piece fromFenSymbol(char symbol) {
    Side side = Character.isUpperCase(symbol) ? Side.WHITE : Side.BLACK;
    return switch (Character.toUpperCase(symbol)) {
      case 'P' -> new Pawn(side);
      case 'N' -> new Knight(side);
      case 'B' -> new Bishop(side);
      case 'R' -> new Rook(side);
      case 'Q' -> new Queen(side);
      case 'K' -> new King(side);
      default -> throw new IllegalArgumentException("Invalid FEN piece symbol: " + symbol);
    };
  }

  /** Builds the piece for a figurine (FAN) glyph - side is already implied by the glyph itself. */
  static Piece fromFanSymbol(String symbol) {
    return switch (symbol) {
      case "♙" -> new Pawn(Side.WHITE);
      case "♘" -> new Knight(Side.WHITE);
      case "♗" -> new Bishop(Side.WHITE);
      case "♖" -> new Rook(Side.WHITE);
      case "♕" -> new Queen(Side.WHITE);
      case "♔" -> new King(Side.WHITE);
      case "♟" -> new Pawn(Side.BLACK);
      case "♞" -> new Knight(Side.BLACK);
      case "♝" -> new Bishop(Side.BLACK);
      case "♜" -> new Rook(Side.BLACK);
      case "♛" -> new Queen(Side.BLACK);
      case "♚" -> new King(Side.BLACK);
      default -> throw new IllegalArgumentException("Invalid FAN piece symbol: " + symbol);
    };
  }
}
