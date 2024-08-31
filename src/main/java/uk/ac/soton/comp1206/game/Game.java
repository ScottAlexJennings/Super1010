package uk.ac.soton.comp1206.game;

import java.util.HashSet;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBlockCoordinate;
import uk.ac.soton.comp1206.event.EndGameListener;
import uk.ac.soton.comp1206.event.GameLoopListener;
import uk.ac.soton.comp1206.event.LineClearedListener;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The Game class handles the main logic, state and properties of the TetrECS game. Methods to
 * manipulate the game state and to handle actions made by the player should take place inside this
 * class.
 */
public class Game {

  private static final Logger logger = LogManager.getLogger(Game.class);
  /**
   * Number of rows
   */
  protected final int rows;
  /**
   * Number of columns
   */
  protected final int cols;
  /**
   * The grid model linked to the game
   */
  protected final Grid grid;
  /**
   * HashSet of the blocks to be cleared
   */
  private final HashSet<IntegerProperty> blocks = new HashSet<>();
  /**
   * IntegerProperty of the score to link to challenge scene
   */
  public IntegerProperty score = new SimpleIntegerProperty(0);
  /**
   * IntegerProperty of the level to link to challenge scene
   */
  public IntegerProperty level = new SimpleIntegerProperty(0);
  /**
   * IntegerProperty of the lives to link to challenge scene
   */
  public IntegerProperty lives = new SimpleIntegerProperty(3);
  /**
   * IntegerProperty of the multipler to link to challenge scene
   */
  public IntegerProperty multiplier = new SimpleIntegerProperty(1);
  /**
   * Initialising the nextPieceListener
   */
  private NextPieceListener nextPieceListener;
  /**
   * Initialising the followingPieceListener
   */
  private NextPieceListener followingPieceListener;
  /**
   * Initialising the lineClearedListener
   */
  private LineClearedListener lineClearedListener;
  /**
   * Initialising the gameLoopListener
   */
  private GameLoopListener gameLoopListener;
  /**
   * Initialising the endGameListener
   */
  private EndGameListener endGameListener;
  /**
   * The GamePiece that holds the current piece that will be placed
   */
  private GamePiece currentPiece;
  /**
   * The GamePiece that holds the second piece to be played
   */
  private GamePiece followingPiece;
  /**
   * The Timer for the game loop
   */
  private Timer countDown;
  /**
   * The x-axis location of the focused block. Initialised to 2 (centre of board)
   */
  private int aimX = 2;

  /**
   * The y-axis location of the focused block. Initialised to 2 (centre of board)
   */
  private int aimY = 2;

  /**
   * Create a new game with the specified rows and columns. Creates a corresponding grid model.
   */
  public Game(int cols, int rows) {
    this.cols = cols;
    this.rows = rows;

    //Create a new grid model to represent the game state
    this.grid = new Grid(cols, rows);
  }

  /**
   * Start the game
   */
  public void start() {
    logger.info("Starting game");
    initialiseGame();
  }

  /**
   * Stop the game
   */
  public void stop() {
    logger.info("Stopping the game");

    //Setting all listeners to null
    nextPieceListener = null;
    followingPieceListener = null;
    lineClearedListener = null;
    gameLoopListener = null;

    //Stopping the timer
    countDown.cancel();
    countDown.purge();
  }

  /**
   * Initialise a new game and set up anything that needs to be done at the start
   */
  public void initialiseGame() {
    logger.info("Initialising game");

    //Populate the GamePieces
    currentPiece = spawnPiece();
    followingPiece = spawnPiece();

    //Update UI with upcoming pieces
    nextPieceListener.nextPiece(currentPiece);
    followingPieceListener.nextPiece(followingPiece);

    logger.info("The current piece is " + currentPiece);
    logger.info("The following piece is " + followingPiece);

    //Starting the timer
    resetTimer(getTimerDelay());
  }

  /**
   * Handle what should happen when a particular block is clicked
   *
   * @param gameBlock the block that was clicked
   */
  public void blockClicked(GameBlock gameBlock) {
    //Get the position of this block
    int x = gameBlock.getX();
    int y = gameBlock.getY();

    //Place block at position of the block
    placeBlock(x, y);
  }

  /**
   * Place the block of the conditions are met
   *
   * @param x x position of the block
   * @param y y position of the block
   */
  public void placeBlock(int x, int y) {
    //Try play the current piece at location of the focused block
    if (grid.playPiece(currentPiece, x, y)) {
      //Replace the current piece with the following
      nextPiece();
      //What happens after piece is played
      afterPiece();
      //Play place sound
      Multimedia.playSound("place.wav");
    } else
    //Play fail sound
    {
      Multimedia.playSound("fail.wav");
    }
  }

  /**
   * Clear any full vertical/horizontal lines that have been made AND reset the timer
   */
  public void afterPiece() {
    //Reset the number of lines to clear
    int clearLines = 0;
    //Reset the blocks to clear
    blocks.clear();

    //Check the columns to see if a column is full
    for (int x = 0; x < cols; x++) {
      //Reset the counter for each column
      var counter = 0;

      //Check the rows
      for (int y = 0; y < rows; y++) {
        //If the block has a value other than 0 increase counter
        if (grid.get(x, y) != 0) {
          counter++;
        }
      }
      //If the counter has the same number as rows call the column clear
      //and increase the line counter
      if (counter == rows) {
        clearColumns(x);
        clearLines++;
      }
    }

    //Check the row to see if a column is full
    for (int y = 0; y < rows; y++) {
      //Reset the counter for each row
      var counter = 0;

      //Check the columns
      for (int x = 0; x < cols; x++) {
        //If the block has a value other than 0 increase counter
        if (grid.get(x, y) != 0) {
          counter++;
        }
      }
      //If the counter has the same number as columns call the row clear
      //and increase the line counter
      if (counter == rows) {
        clearRows(y);
        clearLines++;
      }
    }

    //Calculate the score based on the number of blocks and lines cleared
    calcScore(blocks.size(), clearLines);

    //Update each block to be empty
    for (IntegerProperty block : blocks) {
      block.set(0);
    }

    logger.info("Number of lines cleared: " + clearLines);
    logger.info("Number of blocks cleared: " + blocks.size());

    //Reset the timer
    resetTimer(getTimerDelay());
    logger.info("Timer reset");
  }

  /**
   * Clear the column on the row of the parameter
   * @param x clear column on row x
   */
  private void clearColumns(int x) {
    //Get a set of the cords of the blocks to clear in the UI
    HashSet<GameBlockCoordinate> blockCords = new HashSet<>();

    //Iterate though the column
    for (int y = 0; y < rows; y++) {
      //Add blocks to be cleared in game
      blocks.add(grid.getGridProperty(x, y));
      //Add block cords to be cleared in UI
      blockCords.add(new GameBlockCoordinate(x, y));
    }
    //Update the UI to clear blocks in message
    lineClearedListener.lineCleared(blockCords);

    //Play line cleared music
    Multimedia.playSound("clear.wav");
  }

  /**
   * Clear the row on the column of the parameter
   * @param y clear column on row y
   */
  private void clearRows(int y) {
    //Get a set of the cords of the blocks to clear in the UI
    HashSet<GameBlockCoordinate> blockCords = new HashSet<>();

    //Iterate though the column
    for (int x = 0; x < cols; x++) {
      //Add blocks to be cleared in game
      blocks.add(grid.getGridProperty(x, y));
      //Add block cords to be cleared in UI
      blockCords.add(new GameBlockCoordinate(x, y));
    }
    //Update the UI to clear blocks in message
    lineClearedListener.lineCleared(blockCords);

    //Play line cleared music
    Multimedia.playSound("clear.wav");
  }

  /**
   * Calculate the total score of the game. Updates when a line is cleared
   */
  private void calcScore(int blockNumber, int lineNumber) {
    //Calculate the score
    //Number of lines cleared * number of blocks cleared * 10 * multiplier + score
    score.set((lineNumber * blockNumber * 10 * multiplier.get()) + score.get());
    logger.info("The score is: " + score.get());

    //Implement the level
    level.set(score.get() / 1000);
    logger.info("The level is: " + level.get());

    //Implement the multiplier
    multiplier.set(multiplier.get() + 1);
    if (lineNumber == 0) {
      multiplier.set(1);
    }
    logger.info("The multiplier is" + multiplier.get());
  }

  /**
   * Figure out how long the timer has before the block is changed and a life is lost
   */
  private int getTimerDelay() {
    //Time the user has to place a block
    //12000 - level * 500
    int delay = 12000 - (level.get() * 500);

    //Set minimum value for the delay
    //At level 20 time does not get any faster
    if (delay < 2500)
      delay = 2500;

    return delay;
  }

  /**
   * Handle duration of the game. Handle the time running out and losing lives
   */
  private void gameLoop() {
    //When you still have lives and the game is not over
    if (lives.get() > 0) {
      //Reduce the number of lives
      lives.set(lives.get() - 1);
      logger.info("Lives left are " + lives.get());

      //Replace the current piece and get a new following piece
      nextPiece();

      //Reset the timer
      resetTimer(getTimerDelay());
      logger.info("Timer started with " + getTimerDelay() + " milliseconds");

      //Set the multipler back to 1 because no block placed
      multiplier.set(1);
    } else {
      logger.info("Game has ended, no more lives");
      //Stop the game because all lives are lost
      stop();

      //Update the listener to tell the UI the game is over
      endGameListener.endGame(true);
    }
  }

  /**
   * reset the timer of the game
   *
   * @param delay amount of time the timer lasts
   */
  private void resetTimer(int delay) {
    //Initiate the timer task
    TimerTask task = new TimerTask() {
      @Override
      public void run() {
        gameLoop();
      }
    };

    //Prime timer
    if (countDown != null) {
      countDown.cancel();
    }

    //Start timer
    countDown = new Timer();
    countDown.schedule(task, delay);

    //Update the UI to tell the visual timer to start again with the message
    gameLoopListener.gameLoop(getTimerDelay());
    logger.info("Timer started");
  }

  /**
   * Set the listener to handle an event when nextPiece is set
   *
   * @param listener listener to nextPiece object
   */
  public void setNextListener(NextPieceListener listener) {
    this.nextPieceListener = listener;
  }

  /**
   * Set the listener to handle an event when followingPiece is set
   *
   * @param listener listener to followingPiece object
   */
  public void setFollowingPieceListener(NextPieceListener listener) {
    this.followingPieceListener = listener;
  }

  /**
   * Set the listener to handle an event when line is cleared
   *
   * @param listener listener to lineCleared object
   */
  public void setLineClearedListener(LineClearedListener listener) {
    this.lineClearedListener = listener;
  }

  /**
   * Set the listener to handle an event when game loops
   *
   * @param listener listener to gameLoop object
   */
  public void setGameLoopListener(GameLoopListener listener) {
    this.gameLoopListener = listener;
  }

  /**
   * Set the listener to handle an event when the game ends
   *
   * @param listener listener to gameEnd object
   */
  public void setEndGameListener(EndGameListener listener) {
    this.endGameListener = listener;
  }

  /**
   * Change the focused block with the keys so long as they are within range
   * @param x change x-axis of focused block
   * @param y change y-axis of focused block
   */
  public void currentAim(int x, int y) {
    aimX += x;
    aimY += y;

    //Focused block cannot surpass GameBoard size to the right
    if (aimX > 4) {
      aimX = 4;
    }

    //Focused block cannot surpass GameBoard size to the left
    if (aimX < 0) {
      aimX = 0;
    }

    //Focused block cannot surpass GameBoard size to the top
    if (aimY > 4) {
      aimY = 4;
    }

    //Focused block cannot surpass GameBoard size to the bottom
    if (aimY < 0) {
      aimY = 0;
    }
  }

  /**
   * Get the aim on the x-axis
   * @return aim on the x-axis
   */
  public int getAimX() {
    return aimX;
  }

  /**
   * Set the aim on the x-axis
   * @param x1 input to change by
   */
  public void setAimX(int x1) {
    aimX = x1;
  }

  /**
   * Get the aim on the y-axis
   * @return aim on the y-axis
   */
  public int getAimY() {
    return aimY;
  }

  /**
   * Set the aim on the y-axis
   * @param y1 input to change by
   */
  public void setAimY(int y1) {
    aimY = y1;
  }

  /**
   * Spawn a random piece from the models of pieces
   */
  public GamePiece spawnPiece() {
    Random random = new Random();
    //18 possible options with the added 3 blocks
    return GamePiece.createPiece(random.nextInt(18), random.nextInt(3));
  }

  /**
   * Replace the current piece with a new piece
   */
  public void nextPiece() {
    //Move the following to the current
    currentPiece = followingPiece;

    //Make new following piece
    followingPiece = spawnPiece();

    //Update UI with upcoming pieces
    nextPieceListener.nextPiece(currentPiece);
    followingPieceListener.nextPiece(followingPiece);

    logger.info("The next piece is " + currentPiece);
  }

  /**
   * Rotate the current piece a certain number of time
   *
   * @param num the number of rotations
   */
  public void rotateCurrentPiece(int num) {
    logger.info("Piece rotated");
    currentPiece.rotate(num);
    nextPieceListener.nextPiece(currentPiece);
    Multimedia.playSound("rotate.wav");
  }

  /**
   * Swap the current and following piece
   */
  public void swapCurrentPiece() {
    var temp = followingPiece;
    followingPiece = currentPiece;
    currentPiece = temp;

    nextPieceListener.nextPiece(currentPiece);
    followingPieceListener.nextPiece(followingPiece);
    Multimedia.playSound("transition.wav");
  }

  /**
   * Getter method for Score Property
   *
   * @return Integer
   */
  public Integer getScoreProperty() {
    return score.get();
  }

  public GamePiece getCurrentPiece() {
    return currentPiece;
  }

  /**
   * Get the grid model inside this game representing the game state of the board
   *
   * @return game grid model
   */
  public Grid getGrid() {
    return grid;
  }
}
