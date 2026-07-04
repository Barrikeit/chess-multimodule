package dev.barrikeit.chesslib;

import dev.barrikeit.chesslib.pieces.Bishop;
import dev.barrikeit.chesslib.pieces.King;
import dev.barrikeit.chesslib.pieces.Knight;
import dev.barrikeit.chesslib.pieces.Pawn;
import dev.barrikeit.chesslib.pieces.Piece;
import dev.barrikeit.chesslib.pieces.Queen;
import dev.barrikeit.chesslib.pieces.Rook;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.EqualsAndHashCode;

/**
 * The board: which piece (if any) occupies each square.
 *
 * <p>Backed by a 64-slot mailbox array ({@code occupation}, indexed by {@link Square#ordinal()})
 * for O(1) piece-at-square lookups, plus per-side occupancy bitboards for O(1) occupancy tests -
 * both are the hot path during move generation. Unlike a typical bitboard engine, this stays
 * <b>immutable</b>: every mutation ({@link #withPieces}, {@link #apply}) returns a new {@code
 * Board} rather than mutating in place, since {@link dev.barrikeit.chesslib.game.GameSnapshotState}
 * relies on independent, structurally-comparable board snapshots per ply for threefold-repetition
 * detection.
 */
@EqualsAndHashCode(of = "occupation")
public final class Board {

  private final Piece[] occupation;
  private final long whiteOccupancy;
  private final long blackOccupancy;

  public Board() {
    this(initialPieces());
  }

  public Board(Map<Square, Piece> pieces) {
    this.occupation = new Piece[64];
    long white = 0L;
    long black = 0L;
    for (Map.Entry<Square, Piece> entry : pieces.entrySet()) {
      Square square = entry.getKey();
      Piece piece = entry.getValue();
      occupation[square.ordinal()] = piece;
      if (piece.getSide() == Side.WHITE) white |= square.getBitboard();
      else black |= square.getBitboard();
    }
    this.whiteOccupancy = white;
    this.blackOccupancy = black;
  }

  public Board withPieces(Map<Square, Piece> pieces) {
    return new Board(pieces);
  }

  public Map<Square, Piece> getPieces() {
    Map<Square, Piece> pieces = new LinkedHashMap<>();
    for (int i = 0; i < 64; i++) {
      if (occupation[i] != null) pieces.put(Square.allSquares[i], occupation[i]);
    }
    return pieces;
  }

  public Piece getPiece(Square square) {
    return occupation[square.ordinal()];
  }

  public boolean isEmpty(Square square) {
    return occupation[square.ordinal()] == null;
  }

  public boolean hasPiece(Square square, Side side) {
    long occupancy = side == Side.WHITE ? whiteOccupancy : blackOccupancy;
    return (occupancy & square.getBitboard()) != 0L;
  }

  /** Bounds-safe square lookup for move-generation stepping; {@code null} if off the board. */
  public Square getSquare(int file, int rank) {
    return Square.fromOrNull(file, rank);
  }

  public Square find(Piece piece) {
    for (int i = 0; i < 64; i++) {
      if (occupation[i] == piece) return Square.allSquares[i];
    }
    return null;
  }

  public <T extends Piece> List<Square> find(Class<T> type, Side side) {
    List<Square> result = new ArrayList<>();
    for (int i = 0; i < 64; i++) {
      Piece piece = occupation[i];
      if (type.isInstance(piece) && piece.getSide() == side) result.add(Square.allSquares[i]);
    }
    return result;
  }

  public Map<Square, Piece> pieces(Side side) {
    Map<Square, Piece> result = new LinkedHashMap<>();
    long remaining = side == Side.WHITE ? whiteOccupancy : blackOccupancy;
    while (remaining != 0L) {
      int index = Bitboard.bitScanForward(remaining);
      result.put(Square.allSquares[index], occupation[index]);
      remaining = Bitboard.extractLsb(remaining);
    }
    return result;
  }

  public Board apply(BoardEvent effect) {
    return effect == null ? this : effect.applyOn(this);
  }

  private static Map<Square, Piece> initialPieces() {
    Map<Square, Piece> pieces = new LinkedHashMap<>();
    pieces.put(Square.A8, new Rook(Side.BLACK));
    pieces.put(Square.B8, new Knight(Side.BLACK));
    pieces.put(Square.C8, new Bishop(Side.BLACK));
    pieces.put(Square.D8, new Queen(Side.BLACK));
    pieces.put(Square.E8, new King(Side.BLACK));
    pieces.put(Square.F8, new Bishop(Side.BLACK));
    pieces.put(Square.G8, new Knight(Side.BLACK));
    pieces.put(Square.H8, new Rook(Side.BLACK));

    pieces.put(Square.A7, new Pawn(Side.BLACK));
    pieces.put(Square.B7, new Pawn(Side.BLACK));
    pieces.put(Square.C7, new Pawn(Side.BLACK));
    pieces.put(Square.D7, new Pawn(Side.BLACK));
    pieces.put(Square.E7, new Pawn(Side.BLACK));
    pieces.put(Square.F7, new Pawn(Side.BLACK));
    pieces.put(Square.G7, new Pawn(Side.BLACK));
    pieces.put(Square.H7, new Pawn(Side.BLACK));

    pieces.put(Square.A2, new Pawn(Side.WHITE));
    pieces.put(Square.B2, new Pawn(Side.WHITE));
    pieces.put(Square.C2, new Pawn(Side.WHITE));
    pieces.put(Square.D2, new Pawn(Side.WHITE));
    pieces.put(Square.E2, new Pawn(Side.WHITE));
    pieces.put(Square.F2, new Pawn(Side.WHITE));
    pieces.put(Square.G2, new Pawn(Side.WHITE));
    pieces.put(Square.H2, new Pawn(Side.WHITE));

    pieces.put(Square.A1, new Rook(Side.WHITE));
    pieces.put(Square.B1, new Knight(Side.WHITE));
    pieces.put(Square.C1, new Bishop(Side.WHITE));
    pieces.put(Square.D1, new Queen(Side.WHITE));
    pieces.put(Square.E1, new King(Side.WHITE));
    pieces.put(Square.F1, new Bishop(Side.WHITE));
    pieces.put(Square.G1, new Knight(Side.WHITE));
    pieces.put(Square.H1, new Rook(Side.WHITE));

    return pieces;
  }
}
