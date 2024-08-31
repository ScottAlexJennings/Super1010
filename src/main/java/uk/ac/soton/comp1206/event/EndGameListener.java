package uk.ac.soton.comp1206.event;

/**
 * The End Game listener is used to handle the event when game ends. A boolean of the game state is
 * passed
 */
public interface EndGameListener {

  /**
   * Handle if the game is over
   *
   * @param isOver the state of the game
   */
  void endGame(Boolean isOver);
}
