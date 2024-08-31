package uk.ac.soton.comp1206.component;

import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * Make a board that holds the next piece and the following to be displayed
 * <p>
 * PieceBoard extends the GameBoard
 * <p>
 * Gets the GamePiece and where the value is 0 displays an empty block and otherwise the colour
 * corresponding to the pieces value
 * <p>
 * The PieceBoard should be bound to the corresponding GamePiece in the game directory
 */
public class PieceBoard extends GameBoard {

  /**
   * Create the visual representation of the GamePiece
   * <p>
   * Inherits the parameters of the GameBoard
   *
   * @param cols   number of columns for internal grid
   * @param rows   number of rows for internal grid
   * @param width  the visual width
   * @param height the visual height
   */
  public PieceBoard(int cols, int rows, double width, double height) {
    super(cols, rows, width, height);

  }

  /**
   * Create a visual display of the GamePiece
   *
   * @param piece The Game Piece that will be displayed
   */
  public void displayPiece(GamePiece piece) {
    int[][] blocks = piece.getBlocks();

    //Loop through every part of the game piece
    for (int x = 0; x < blocks.length; x++) { // columns
      for (int y = 0; y < blocks[x].length; y++) { // rows
        //If block has the same value as the piece should
        if (blocks[x][y] == piece.getValue()) {
          //Set the block to that value/colour
          grid.set(x, y, piece.getValue());
        } else {
          //Else set the block to empty
          grid.set(x, y, 0);
        }
      }
    }
  }

  /**
   * Make a circle that will be displayed in the centre of the PieceBoard
   *
   * @param x         Centre of the circle on the x-axis
   * @param y         Centre of the circle on the y-axis
   * @param stackPane The node that the circle is placed on
   */
  public void displayCircle(double x, double y, StackPane stackPane) {
    //Make circle
    Circle circle = new Circle(x, y, 10);

    //Design circle
    circle.setFill(Color.FLORALWHITE);
    circle.setOpacity(0.6);

    //Add circle
    stackPane.getChildren().add(circle);
  }
}
