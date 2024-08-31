package uk.ac.soton.comp1206.component;

import javafx.animation.AnimationTimer;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ObservableValue;
import javafx.scene.canvas.Canvas;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.GamePiece;

/**
 * The Visual User Interface component representing a single block in the grid.
 * <p>
 * Extends Canvas and is responsible for drawing itself.
 * <p>
 * Displays an empty square (when the value is 0) or a coloured square depending on value.
 * <p>
 * The GameBlock value should be bound to a corresponding block in the Grid model.
 */
public class GameBlock extends Canvas {

  /**
   * The set of colours for different pieces
   */
  public static final Color[] COLOURS = {
      Color.TRANSPARENT,
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.YELLOW,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE,
      Color.AQUA,
      Color.AQUAMARINE,
      Color.BLUE,
      Color.MEDIUMPURPLE,
      Color.PURPLE,
      Color.MAGENTA,
      Color.DARKSLATEBLUE,
      Color.LIGHTPINK
  };
  private static final Logger logger = LogManager.getLogger(GameBlock.class);
  /**
   * The game board
   */
  private final GameBoard gameBoard;

  /**
   * The width of the game block
   */
  private final double width;

  /**
   * The height of the game block
   */
  private final double height;
  /**
   * The value of this block (0 = empty, otherwise specifies the colour to render as)
   */
  private final IntegerProperty value = new SimpleIntegerProperty(0);
  /**
   * The column this block exists as in the grid
   */
  private int x;
  /**
   * The row this block exists as in the grid
   */
  private int y;

  /**
   * Create a new single Game Block
   *
   * @param gameBoard the board this block belongs to
   * @param x         the column the block exists in
   * @param y         the row the block exists in
   * @param width     the width of the canvas to render
   * @param height    the height of the canvas to render
   */
  public GameBlock(GameBoard gameBoard, int x, int y, double width, double height) {
    this.gameBoard = gameBoard;
    this.width = width;
    this.height = height;
    this.x = x;
    this.y = y;

    //A canvas needs a fixed width and height
    setWidth(width);
    setHeight(height);

    //Do an initial paint
    paint();

    //When the value property is updated, call the internal updateValue method
    value.addListener(this::updateValue);
  }

  /**
   * Create an animation to fade out blocks that are cleared
   */
  public void fadeOut(GamePiece nextPiece, GameBlock currentBlock) {
    logger.info("fading block");
    var gc = getGraphicsContext2D();

    //Clear blocks animation
    AnimationTimer timer = new AnimationTimer() {
      //Set default opacity
      double opacity = 0.8;

      @Override
      public void handle(long now) {
        //Set the background effect on the blocks
        paintHighlight(nextPiece, currentBlock);

        //Apply the effects to the blocks
        gc.save();
        gc.setGlobalAlpha(opacity);
        gc.setFill(Color.LAWNGREEN);
        gc.fillRect(0, 0, width, height);
        gc.restore();

        //Reduce the opacity of the green blocks until they are gone
        if (opacity <= 0) {
          stop();
        } else {
          opacity -= 0.025;
        }
      }
    };
    //Start the animation
    timer.start();
  }

  /**
   * When the value of this block is updated,
   *
   * @param observable what was updated
   * @param oldValue   the old value
   * @param newValue   the new value
   */
  private void updateValue(ObservableValue<? extends Number> observable, Number oldValue,
      Number newValue) {
    logger.info("Updating value");
    paint();
  }

  /**
   * Handle painting of the block canvas
   */
  public void paint() {
    logger.info("Paint the canvas something");
    //If the block is empty, paint as empty
    if (value.get() == 0) {
      paintEmpty();
    } else {
      //If the block is not empty, paint with the colour represented by the value
      paintColor(COLOURS[value.get()]);
    }
  }

  /**
   * Define the effect that the blocks that are highlighted by the cursor or arrows will be
   */
  private void paintHover(int colourVal) {
    var gc = getGraphicsContext2D();
    var gcB = getGraphicsContext2D();

    //Colour fill
    //Take the next piece block value to display the next piece
    gc.setFill(COLOURS[colourVal]);
    //Greater than paint empty
    gc.setGlobalAlpha(0.4);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.BLACK);
    gc.strokeRect(0, 0, width, height);

    //Effect
    //Make triangle to make effect on blocks
    gcB.setFill(Paint.valueOf("#5C5959"));
    gcB.setGlobalAlpha(0.2);
    gcB.fillPolygon(new double[]{0, 0, width}, new double[]{0, height, height}, 3);
  }

  /**
   * Method that is called in challenge scene to display next piece on the board
   *
   * @param nextPiece    the next game piece
   * @param currentBlock the current location of the cursor or arrow
   */
  public void paintHighlight(GamePiece nextPiece, GameBlock currentBlock) {
    logger.info("Painting block highlight");
    //Clear the previous blocks
    paint();

    int[][] blocks = nextPiece.getBlocks();

    //Check that blocks to highlight from the next piece
    for (int x = 0; x < 3; x++) { // columns
      for (int y = 0; y < 3; y++) { // rows
        if (blocks[x][y] > 0) {
          //Check the X and Y to see that the next piece blocks align with the current block formation
          if (getX() == currentBlock.getX() - 1 + x && getY() == currentBlock.getY() - 1 + y) {
            paintHover(nextPiece.getValue());
          }
        }
      }
    }
  }

  /**
   * Paint this canvas empty
   */
  private void paintEmpty() {
    logger.info("Painting blocks default");
    var gc = getGraphicsContext2D();
    var gcB = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Fill gray colour at low alpha
    gc.setFill(Paint.valueOf("#A3A2A2"));
    gc.setGlobalAlpha(0.1);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.BLACK);
    //Increase alpha back
    gc.setGlobalAlpha(0.6);
    gc.setLineWidth(5);
    gc.strokeRect(0, 0, width, height);

    //Effect
    //Make triangle to make effect on blocks
    gcB.setFill(Paint.valueOf("#292929"));
    gcB.setGlobalAlpha(0.2);
    gcB.fillPolygon(new double[]{0, 0, width}, new double[]{0, height, height}, 3);
  }

  /**
   * Paint this canvas with the given colour
   *
   * @param colour the colour to paint
   */
  private void paintColor(Paint colour) {
    logger.info("Painting block " + colour);
    var gc = getGraphicsContext2D();
    var gcB = getGraphicsContext2D();

    //Clear
    gc.clearRect(0, 0, width, height);

    //Colour fill
    gc.setFill(colour);
    gc.setGlobalAlpha(1);
    gc.fillRect(0, 0, width, height);

    //Border
    gc.setStroke(Color.BLACK);
    gc.strokeRect(0, 0, width, height);

    //Effect
    //Make triangle to make effect on blocks
    gcB.setFill(Paint.valueOf("#46464b"));
    gcB.setGlobalAlpha(0.2);
    gcB.fillPolygon(new double[]{0, 0, width}, new double[]{0, height, height}, 3);
  }

  /**
   * Get the column of this block
   *
   * @return column number
   */
  public int getX() {
    return x;
  }

  /**
   * Get the row of this block
   *
   * @return row number
   */
  public int getY() {
    return y;
  }

  /**
   * Bind the value of this block to another property. Used to link the visual block to a
   * corresponding block in the Grid.
   *
   * @param input property to bind the value to
   */
  public void bind(ObservableValue<? extends Number> input) {
    value.bind(input);
  }

}
