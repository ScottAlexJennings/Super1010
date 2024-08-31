package uk.ac.soton.comp1206.event;

/**
 * The Game Loop listener is used to handle the event when the timer runs out or restarts to
 * progress the game
 */

public interface GameLoopListener {

  /**
   * Handles the game loop
   *
   * @param countdown the time left before the game loops
   */
  void gameLoop(double countdown);
}
