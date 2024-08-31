package uk.ac.soton.comp1206.game;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The Grid is a model which holds the state of a game board. It is made up of a set of Integer
 * values arranged in a 2D arrow, with rows and columns.
 * <p>
 * Each value inside the Grid is an IntegerProperty can be bound to enable modification and display
 * of the contents of the grid.
 * <p>
 * The Grid contains functions related to modifying the model, for example, placing a piece inside
 * the grid.
 * <p>
 * The Grid should be linked to a GameBoard for it's display.
 */
public class Grid {

  private static final Logger logger = LogManager.getLogger(Game.class);

  /**
   * The number of columns in this grid
   */
  private final int cols;

  /**
   * The number of rows in this grid
   */
  private final int rows;

  /**
   * The grid is a 2D arrow with rows and columns of SimpleIntegerProperties.
   */
  private final SimpleIntegerProperty[][] grid;

  /**
   * Create a new Grid with the specified number of columns and rows and initialise them
   *
   * @param cols number of columns
   * @param rows number of rows
   */
  public Grid(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create the grid itself
    grid = new SimpleIntegerProperty[cols][rows];
    logger.info("Making grid");

    //Add a SimpleIntegerProperty to every block in the grid
    for (var y = 0; y < rows; y++) {
      for (var x = 0; x < cols; x++) {
        grid[x][y] = new SimpleIntegerProperty(0);
      }
    }
  }

  /**
   * Determine if the GamePiece can be played on the grid
   *
   * @param piece  piece to play
   * @param placeX x location of piece
   * @param placeY y location of piece
   * @return state where block and be played
   */
  public boolean canPlayPiece(GamePiece piece, int placeX, int placeY) {
    //Move the start of check to top left
    placeX = placeX - 1;
    placeY = placeY - 1;

    //Get the blocks from the piece
    int[][] blocks = piece.getBlocks();

    //Loop through every part of the game piece
    for (int x = 0; x < blocks.length; x++) { // columns
      for (int y = 0; y < blocks[x].length; y++) { // rows
        if (blocks[x][y] == piece.getValue()) {
          //Keep inside the grid
          if ((placeX + x) > 4 || (placeX + x) < 0 || (placeY + y) > 4 || (placeY + y) < 0) {
            logger.info("Cannot play piece");
            return false;
          }
          //Set the block value
          int gridValue = get(placeX + x, placeY + y);

          //Check if there is something in the way of block
          if (gridValue > 0) {
            logger.info("Cannot play piece");
            return false;
          }
        }
      }
    }
    //There is nothing in the way
    logger.info("Can play piece");
    return true;
  }

  /**
   * Play the GamePiece in the Grid
   *
   * @param piece  piece to play
   * @param placeX x location of piece
   * @param placeY y location of piece
   * @return state where block is placed
   */
  public boolean playPiece(GamePiece piece, int placeX, int placeY) {
    //Determine if the block can be placed
    if (!canPlayPiece(piece, placeX, placeY)) {
      logger.info("Cannot play piece");
      return false;
    }

    //Move the start of placement to top left
    placeX = placeX - 1;
    placeY = placeY - 1;

    //Get the blocks from the piece
    int[][] blocks = piece.getBlocks();

    //Loop through every part of the game piece
    for (int x = 0; x < blocks.length; x++) { // columns
      for (int y = 0; y < blocks[x].length; y++) { // rows
        //Change value and colour of block if equal to value of piece
        if (blocks[x][y] == piece.getValue()) {
          //Updates the grid of the game board with that piece
          set(placeX + x, placeY + y, piece.getValue());
        }
      }
    }
    logger.info("Can play piece");
    return true;
  }

  /**
   * Get the Integer property contained inside the grid at a given row and column index. Can be used
   * for binding.
   *
   * @param x column
   * @param y row
   * @return the IntegerProperty at the given x and y in this grid
   */
  public IntegerProperty getGridProperty(int x, int y) {
    return grid[x][y];
  }

  /**
   * Update the value at the given x and y index within the grid
   *
   * @param x     column
   * @param y     row
   * @param value the new value
   */
  public void set(int x, int y, int value) {
    grid[x][y].set(value);
  }

  /**
   * Get the value represented at the given x and y index within the grid
   *
   * @param x column
   * @param y row
   * @return the value
   */
  public int get(int x, int y) {
    try {
      //Get the value held in the property at the x and y index provided
      return grid[x][y].get();
    } catch (ArrayIndexOutOfBoundsException e) {
      //No such index
      return -1;
    }
  }

  /**
   * Get the number of columns in this game
   *
   * @return number of columns
   */
  public int getCols() {
    return cols;
  }


  /**
   * Get the number of rows in this game
   *
   * @return number of rows
   */
  public int getRows() {
    return rows;
  }

}
