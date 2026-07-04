package dev.barrikeit.chesslib;

public enum Rank {
  ONE("1"),
  TWO("2"),
  THREE("3"),
  FOUR("4"),
  FIVE("5"),
  SIX("6"),
  SEVEN("7"),
  EIGHT("8"),
  NONE("");

  public static final Rank[] allRanks = values();

  private final String notation;

  Rank(String notation) {
    this.notation = notation;
  }

  public static Rank from(String notation) {
    return valueOf(notation);
  }

  public static Rank from(int notation) {
    return switch (notation) {
      case 1 -> ONE;
      case 2 -> TWO;
      case 3 -> THREE;
      case 4 -> FOUR;
      case 5 -> FIVE;
      case 6 -> SIX;
      case 7 -> SEVEN;
      case 8 -> EIGHT;
      default -> throw new IllegalArgumentException("Invalid rank number: " + notation);
    };
  }

  public String getNotation() {
    return notation;
  }

  public String value() {
    return name();
  }
}
