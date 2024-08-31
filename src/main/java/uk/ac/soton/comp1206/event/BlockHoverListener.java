package uk.ac.soton.comp1206.event;

import uk.ac.soton.comp1206.component.GameBlock;

/**
 * The Block Hovered listener is used to handle the event when the mouse is hovering over a block in
 * the GameBoard. It passes the GameBlock that was entered in the message
 */
public interface BlockHoverListener {

  /**
   * Handle the hovered block
   *
   * @param block the hovered block
   */
  void mouseInBlock(GameBlock block);

}
