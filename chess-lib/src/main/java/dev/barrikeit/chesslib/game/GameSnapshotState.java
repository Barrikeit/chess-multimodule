package dev.barrikeit.chesslib.game;

import dev.barrikeit.chesslib.Board;
import dev.barrikeit.chesslib.Side;
import dev.barrikeit.chesslib.Square;
import dev.barrikeit.chesslib.moves.AppliedMove;
import dev.barrikeit.chesslib.moves.BoardMove;
import dev.barrikeit.chesslib.moves.Capture;
import dev.barrikeit.chesslib.moves.MoveEffect;
import dev.barrikeit.chesslib.moves.Moves;
import dev.barrikeit.chesslib.pieces.King;
import dev.barrikeit.chesslib.pieces.Pawn;
import dev.barrikeit.chesslib.pieces.Piece;
import java.util.ArrayList;
import java.util.List;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * The state of the game at a given moment: the board, whose turn it is, the game status, the
 * captured pieces, the castling rights of each side, the last move played, and the move currently
 * being resolved.
 *
 * <p>Equality is structural over everything except {@code halfMoveClock}/{@code fullMoveNumber} -
 * those two monotonically change every ply, so including them would mean no two states could ever
 * compare equal, breaking {@link GameRules#hasThreefoldRepetition} (which relies on repeated
 * positions in a game's history comparing equal).
 */
@Getter
@EqualsAndHashCode(exclude = {"halfMoveClock", "fullMoveNumber"})
@Builder(toBuilder = true)
public class GameSnapshotState {

  @Builder.Default private final Board board = new Board();
  @Builder.Default private final Side turn = Side.WHITE;
  @Builder.Default private final GameStatus gameStatus = GameStatus.IN_PROGRESS;
  @Builder.Default private final List<Piece> capturedPieces = List.of();
  @Builder.Default private final int halfMoveClock = 0;
  @Builder.Default private final int fullMoveNumber = 1;
  private final CastlingState castlingState;
  private final AppliedMove lastMove;
  private final AppliedMove move;

  public static GameSnapshotState newGame() {
    Board board = new Board();
    return GameSnapshotState.builder().board(board).castlingState(CastlingState.from(board)).build();
  }

  public int getScore() {
    int score = 0;
    for (Piece piece : board.getPieces().values()) {
      score += piece.getValue() * (piece.getSide() == Side.WHITE ? 1 : -1);
    }
    return score;
  }

  public boolean hasCheck() {
    return hasCheckFor(turn);
  }

  public boolean hasCheckFor(Side side) {
    List<Square> kingSquares = board.find(King.class, side);
    if (kingSquares.isEmpty()) return false;
    Square square = kingSquares.get(0);
    return hasCheckFor(square);
  }

  public boolean hasCheckFor(Square square) {
    for (Piece piece : board.getPieces().values()) {
      List<BoardMove> captures = piece.pseudoLegalMoves(this, true).stream()
          .filter(move -> move.getPreMove() instanceof Capture)
          .toList();
      if (Moves.targetPositions(captures).contains(square)) return true;
    }
    return false;
  }

  public List<BoardMove> legalMovesFrom(Square square) {
    Piece piece = board.getPiece(square);
    if (piece == null) return List.of();
    return applyCheckConstraints(piece.pseudoLegalMoves(this, false));
  }

  private List<BoardMove> applyCheckConstraints(List<BoardMove> moves) {
    return moves.stream()
        .filter(move -> !tempGameState(move).hasCheckFor(move.getPiece().getSide()))
        .toList();
  }

  public GameStateTransition calculateAppliedMove(BoardMove boardMove, List<GameSnapshotState> statesSoFar) {
    GameSnapshotState tempNewGameState = tempGameState(boardMove);

    boolean hasLegalMoves = tempNewGameState.getBoard().pieces(tempNewGameState.getTurn()).keySet().stream()
        .anyMatch(square -> !tempNewGameState.legalMovesFrom(square).isEmpty());
    boolean isCheck = tempNewGameState.hasCheck();
    boolean isCheckNoMate = hasLegalMoves && isCheck;
    boolean isCheckMate = !hasLegalMoves && isCheck;
    boolean isStaleMate = !hasLegalMoves && !isCheck;
    boolean insufficientMaterial = GameRules.hasInsufficientMaterial(tempNewGameState.getBoard().getPieces());
    List<GameSnapshotState> allStates = new ArrayList<>(statesSoFar);
    allStates.add(tempNewGameState);
    boolean threefoldRepetition = GameRules.hasThreefoldRepetition(allStates);

    MoveEffect effect;
    if (isCheckNoMate) effect = MoveEffect.CHECK;
    else if (isCheckMate) effect = MoveEffect.CHECKMATE;
    else if (isStaleMate || insufficientMaterial || threefoldRepetition) effect = MoveEffect.DRAW;
    else effect = null;

    AppliedMove appliedMove = new AppliedMove(GameRules.applyAmbiguity(boardMove, this), effect);

    GameStatus newStatus;
    if (isCheckMate) newStatus = GameStatus.CHECKMATE;
    else if (isStaleMate) newStatus = GameStatus.STALEMATE;
    else if (threefoldRepetition) newStatus = GameStatus.DRAW_BY_REPETITION;
    else if (insufficientMaterial) newStatus = GameStatus.INSUFFICIENT_MATERIAL;
    else newStatus = GameStatus.IN_PROGRESS;

    List<Piece> newCapturedPieces = capturedPieces;
    if (boardMove.getPreMove() instanceof Capture capture) {
      newCapturedPieces = new ArrayList<>(capturedPieces);
      newCapturedPieces.add(capture.piece());
    }

    boolean isPawnMove = boardMove.getPiece() instanceof Pawn;
    boolean isCaptureMove = boardMove.getPreMove() instanceof Capture;
    int newHalfMoveClock = (isPawnMove || isCaptureMove) ? 0 : halfMoveClock + 1;
    int newFullMoveNumber = turn == Side.BLACK ? fullMoveNumber + 1 : fullMoveNumber;

    GameSnapshotState fromGameState = this.toBuilder().move(appliedMove).build();
    GameSnapshotState toGameState = tempNewGameState.toBuilder()
        .gameStatus(newStatus)
        .move(null)
        .lastMove(appliedMove)
        .capturedPieces(newCapturedPieces)
        .castlingState(castlingState.apply(boardMove))
        .halfMoveClock(newHalfMoveClock)
        .fullMoveNumber(newFullMoveNumber)
        .build();

    return new GameStateTransition(fromGameState, toGameState, appliedMove);
  }

  private GameSnapshotState tempGameState(BoardMove boardMove) {
    Board updatedBoard = board.apply(boardMove.getPreMove()).apply(boardMove.getMove()).apply(boardMove.getConsequence());

    return this.toBuilder()
        .board(updatedBoard)
        .turn(turn.flip())
        .move(null)
        .lastMove(new AppliedMove(boardMove, null))
        .build();
  }
}
