package dev.barrikeit.chesslib;

public enum BoardEventType {
  ON_MOVE,
  ON_UNDO_MOVE,
  ON_LOAD;

  /**
   * Returns a board event type given its name.
   *
   * <p>Same as invoking {@link BoardEventType#valueOf(String)}.
   *
   * @param v name of the board event type
   * @return the board event type with the specified name
   * @throws IllegalArgumentException if the name does not correspond to any board event type
   */
  public static BoardEventType fromValue(String v) {
    return valueOf(v);
  }

  /**
   * Returns the name of the board event type.
   *
   * @return the name of the board event type
   */
  public String value() {
    return name();
  }
}
