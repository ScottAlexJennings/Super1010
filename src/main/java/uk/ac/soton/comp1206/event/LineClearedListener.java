package uk.ac.soton.comp1206.event;

import java.util.HashSet;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;

/**
 * The Line Cleared listener is used to handle the event a vertical or horizontal line is cleared
 * from the game
 */

public interface LineClearedListener {

  /**
   * Handles what blocks to clear when connect 5
   *
   * @param coordinates HashSet of the coordinates of the blocks to clear
   */
  void lineCleared(HashSet<GameBlockCoordinate> coordinates);

}
