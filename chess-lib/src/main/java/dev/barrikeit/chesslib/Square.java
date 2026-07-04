package dev.barrikeit.chesslib;

/**
 * All 64 board squares, plus the special {@link Square#NONE} value used where no square applies
 * (e.g. no en-passant target).
 */
public enum Square {
  A1, B1, C1, D1, E1, F1, G1, H1,
  A2, B2, C2, D2, E2, F2, G2, H2,
  A3, B3, C3, D3, E3, F3, G3, H3,
  A4, B4, C4, D4, E4, F4, G4, H4,
  A5, B5, C5, D5, E5, F5, G5, H5,
  A6, B6, C6, D6, E6, F6, G6, H6,
  A7, B7, C7, D7, E7, F7, G7, H7,
  A8, B8, C8, D8, E8, F8, G8, H8,
  NONE;

  public static final Square[] allSquares = values();

  private static final Rank[] rankValues = Rank.values();
  private static final File[] fileValues = File.values();

  /** Internal (file, rank) pair backing the board-coordinate arithmetic below. */
  private record Position(int file, int rank) {}

  private final Position position = ordinal() < 64 ? new Position(ordinal() % 8, ordinal() / 8) : null;

  public static Square encode(Rank rank, File file) {
    return allSquares[rank.ordinal() * 8 + file.ordinal()];
  }

  public static Square fromValue(String v) {
    return valueOf(v);
  }

  public static Square from(File file, Rank rank) {
    return allSquares[file.ordinal() + rank.ordinal() * 8];
  }

  public static Square from(int file, int rank) {
    if (file < 0 || file > 7) throw new IllegalArgumentException("Invalid file: " + file);
    if (rank < 0 || rank > 7) throw new IllegalArgumentException("Invalid rank: " + rank);
    return allSquares[file + rank * 8];
  }

  /** Same as {@link #from(int, int)}, but returns {@code null} instead of throwing when out of bounds. */
  public static Square fromOrNull(int file, int rank) {
    if (file < 0 || file > 7 || rank < 0 || rank > 7) return null;
    return allSquares[file + rank * 8];
  }

  public File getFile() {
    return fileValues[position.file()];
  }

  public Rank getRank() {
    return rankValues[position.rank()];
  }

  public int file() {
    return position.file();
  }

  public int rank() {
    return position.rank();
  }

  public boolean isLightSquare() {
    return (file() + rank()) % 2 == 1;
  }

  public boolean isDarkSquare() {
    return !isLightSquare();
  }

  /** The single-bit bitboard representation of this square, or 0L for {@link #NONE}. */
  public long getBitboard() {
    return this == NONE ? 0L : 1L << ordinal();
  }

  public String value() {
    return name();
  }
}
