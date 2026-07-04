package dev.barrikeit.chesslib;

/**
 * Bitboard masks and bit-twiddling helpers used by {@link Board} for O(1) occupancy checks.
 *
 * <p>This intentionally does not include sliding-piece attack tables (magic bitboards) - move
 * generation for sliding pieces is done by stepping along {@link dev.barrikeit.chesslib.pieces.Offset}
 * directions instead, so only the occupancy-oriented masks/helpers are needed here.
 */
public final class Bitboard {

  private Bitboard() {}

  public static final long[] FILE_BB = new long[8];
  public static final long[] RANK_BB = new long[8];
  public static final long LIGHT_SQUARES;
  public static final long DARK_SQUARES;

  static {
    long light = 0L;
    for (Square square : Square.allSquares) {
      if (square == Square.NONE) continue;
      long bit = square.getBitboard();
      FILE_BB[square.file()] |= bit;
      RANK_BB[square.rank()] |= bit;
      if (square.isLightSquare()) light |= bit;
    }
    LIGHT_SQUARES = light;
    DARK_SQUARES = ~light;
  }

  /** Index of the least significant set bit, or 64 if {@code bb} is 0. */
  public static int bitScanForward(long bb) {
    return Long.numberOfTrailingZeros(bb);
  }

  /** Index of the most significant set bit, or -1 if {@code bb} is 0. */
  public static int bitScanReverse(long bb) {
    return 63 - Long.numberOfLeadingZeros(bb);
  }

  /** Clears the least significant set bit. */
  public static long extractLsb(long bb) {
    return bb & (bb - 1);
  }

  public static int popCount(long bb) {
    return Long.bitCount(bb);
  }
}
