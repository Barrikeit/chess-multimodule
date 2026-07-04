package dev.barrikeit.chesslib;

public enum File {
  A("A"),
  B("B"),
  C("C"),
  D("D"),
  E("E"),
  F("F"),
  G("G"),
  H("H"),
  NONE("");

  public static final File[] allFiles = values();

  private final String notation;

  File(String notation) {
    this.notation = notation;
  }

  public static File from(String notation) {
    return valueOf(notation);
  }

  public static File from(int notation) {
    return switch (notation) {
      case 1 -> A;
      case 2 -> B;
      case 3 -> C;
      case 4 -> D;
      case 5 -> E;
      case 6 -> F;
      case 7 -> G;
      case 8 -> H;
      default -> throw new IllegalArgumentException("Invalid file number: " + notation);
    };
  }

  public String getNotation() {
    return notation;
  }

  public String value() {
    return name();
  }
}
