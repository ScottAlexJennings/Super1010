package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Right Click listener is used to handle the event when a block in a GameBoard is clicked with
 * the right mouse button. It passes the GameBlock that was clicked in the message
 */

public interface RightClickedListener {

  /**
   * Handles when the block was right-clicked event
   *
   * @param block The GameBlock that is right-clicked
   */
  void rightClicked(GameBlock block);

}
