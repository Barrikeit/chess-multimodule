package dev.barrikeit.chesslib.moves;

import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.pieces.Pawn;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.EnumSet;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class BoardMove {

  private final SimpleMove move;
  private final PreMove preMove;
  private final Consequence consequence;
  private final EnumSet<Ambiguity> ambiguity;

  public BoardMove(SimpleMove move) {
    this(move, null, null, EnumSet.noneOf(Ambiguity.class));
  }

  public BoardMove(SimpleMove move, PreMove preMove, Consequence consequence) {
    this(move, preMove, consequence, EnumSet.noneOf(Ambiguity.class));
  }

  public BoardMove(SimpleMove move, PreMove preMove, Consequence consequence, EnumSet<Ambiguity> ambiguity) {
    this.move = move;
    this.preMove = preMove;
    this.consequence = consequence;
    this.ambiguity = ambiguity;
  }

  public Square getFrom() {
    return move.from();
  }

  public Square getTo() {
    return move.to();
  }

  public Piece getPiece() {
    return move.piece();
  }

  public BoardMove withAmbiguity(EnumSet<Ambiguity> ambiguity) {
    return new BoardMove(move, preMove, consequence, ambiguity);
  }

  public BoardMove withConsequence(Consequence consequence) {
    return new BoardMove(move, preMove, consequence, ambiguity);
  }

  @Override
  public String toString() {
    return toString(true);
  }

  public String toString(boolean useFigurineNotation) {
    if (move instanceof KingSideCastle) return "O-O";
    if (move instanceof QueenSideCastle) return "O-O-O";

    boolean isCapture = preMove instanceof Capture;
    String symbol;
    if (!(getPiece() instanceof Pawn)) {
      symbol = useFigurineNotation ? getPiece().getFanSymbol() : getPiece().getFenSymbol();
    } else if (isCapture) {
      symbol = getFrom().getFile().getNotation();
    } else {
      symbol = "";
    }
    String file = ambiguity.contains(Ambiguity.AMBIGUOUS_FILE) ? getFrom().getFile().getNotation() : "";
    String rank = ambiguity.contains(Ambiguity.AMBIGUOUS_RANK) ? getFrom().getRank().getNotation() : "";
    String capture = isCapture ? "x" : "";
    String promotion = consequence instanceof Promotion promo ? "=" + promo.piece().getFenSymbol() : "";

    return symbol + file + rank + capture + getTo() + promotion;
  }

  public enum Ambiguity {
    AMBIGUOUS_FILE, AMBIGUOUS_RANK
  }
}
