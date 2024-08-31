package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Next Piece listener is used to handle the event when next piece is constructed
 */

public interface NextPieceListener {

  /**
   * Handles the next piece event
   *
   * @param piece the next GamePiece
   */
  void nextPiece(GamePiece piece);

}
