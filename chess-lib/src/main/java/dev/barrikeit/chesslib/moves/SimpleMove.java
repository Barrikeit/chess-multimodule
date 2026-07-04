package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.BoardEvent;
import dev.barrikeit.chesslib.Square;

public interface SimpleMove extends BoardEvent {
  Square from();

  Square to();
}
