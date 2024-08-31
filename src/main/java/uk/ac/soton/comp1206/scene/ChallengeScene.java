package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.GameBlock;
import uk.ac.soton.comp1206.component.GameBoard;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.event.NextPieceListener;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The Single Player challenge scene. Holds the UI for the single player challenge mode in the
 * game.
 */
public class ChallengeScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Game that handles main logic
   */
  protected Game game;

  /**
   * GameBoard that holds the blocks
   */
  protected GameBoard board;

  /**
   * Initialising the pieceListener
   */
  private NextPieceListener pieceListener;

  /**
   * Initialising the followingPieceListener
   */
  private NextPieceListener followingPieceListener;

  /**
   * Rectangle that is the UI timer
   */
  private Rectangle timer;

  /**
   * Timeline that changes the colour of the UI timer
   */
  private Timeline timeline;

  /**
   * Text that holds the highScore of the local scores
   */
  private Text highScore;

  /**
   * Create a new Single Player challenge scene
   *
   * @param gameWindow the Game Window
   */
  public ChallengeScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Challenge Scene");
    ScoresScene.makeDefaultScore("scores.txt");
  }

  /**
   * Build the Challenge window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Setting of the game
    setupGame();

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane (Place the borderpane atop the background)
    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("challenge-background");
    root.getChildren().add(challengePane);

    //Border Pane (Add the majority of nodes to this)
    var mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    //Top Pane title
    var title = new Text("Challenge Mode");
    BorderPane.setAlignment(title, Pos.BOTTOM_CENTER);
    title.getStyleClass().add("title");

    var filler = new HBox(title);
    filler.setAlignment(Pos.BOTTOM_CENTER);
    filler.setPadding(new Insets(30));
    mainPane.setTop(filler);

    //Right Pane (Board and HBox)
    var hBox = new HBox(5);
    mainPane.setRight(hBox);

    //VBox
    VBox vBox = new VBox(5);
    vBox.setAlignment(Pos.TOP_CENTER);
    hBox.getChildren().add(vBox);

    //High Score
    var highScoreText = new Text("High Score");
    highScoreText.getStyleClass().add("heading");
    vBox.getChildren().add(highScoreText);

    //HighScore Display
    highScore = new Text(String.valueOf(getHighScore()));
    highScore.getStyleClass().add("score");
    vBox.getChildren().add(highScore);

    //Multiplier
    var multiplier = new Text("Multiplier");
    multiplier.getStyleClass().add("heading");
    vBox.getChildren().add(multiplier);

    //Multiplier Display
    var multiplierDisplay = new Text(String.valueOf(game.multiplier.get()));
    multiplierDisplay.textProperty().bind(game.multiplier.asString());
    multiplierDisplay.getStyleClass().add("level");
    vBox.getChildren().add(multiplierDisplay);

    //Board
    board = new GameBoard(game.getGrid(), gameWindow.getWidth() / 2.0, gameWindow.getWidth() / 2.0);
    BorderPane.setAlignment(board, Pos.TOP_CENTER);
    board.setPadding(new Insets(0, 50, 0, 0));
    hBox.getChildren().add(board);

    //Handle block on game board grid being clicked
    board.setOnBlockClick(this::blockClicked);
    board.setOnRightClicked(this::rightClicked);

    //When the mouse is hovering over a block
    board.setOnBlockInHover(this::blockHovered);

    //Clear the row or column with a fade
    game.setLineClearedListener((coordinates) -> board.fadeOut(coordinates, game.getCurrentPiece(), board.getBlock(game.getAimX(), game.getAimY())));

    //Bottom Pane (Progress bar)
    timer = new Rectangle(gameWindow.getWidth(), 25, Color.GREEN);
    mainPane.setBottom(timer);
    game.setGameLoopListener((countdown) -> {
      logger.info("Making UI timer");

      if (timeline != null) {
        timeline.stop();
      }
      //Initialise the UI timer
      timeline = new Timeline();

      //Make the key values that define the changes
      KeyValue keyValueFinal = new KeyValue(timer.widthProperty(), 0);
      KeyValue green = new KeyValue(timer.fillProperty(), Color.GREEN);
      KeyValue red = new KeyValue(timer.fillProperty(), Color.RED);
      KeyValue yellow = new KeyValue(timer.fillProperty(), Color.YELLOW);
      KeyValue keyValueInitial = new KeyValue(timer.widthProperty(), gameWindow.getWidth());

      //Make the frames that the key values are added too
      KeyFrame keyFrameInitial = new KeyFrame(Duration.ZERO, keyValueInitial);
      KeyFrame greenBar = new KeyFrame(Duration.ZERO, green);
      KeyFrame yellowBar = new KeyFrame(Duration.millis(countdown / 2), yellow);
      KeyFrame redBar = new KeyFrame(Duration.millis(countdown), red);
      KeyFrame keyFrameFinal = new KeyFrame(Duration.millis(countdown), keyValueFinal);

      //Add the frames to the UI timer
      timeline.getKeyFrames().add(keyFrameFinal);
      timeline.getKeyFrames().add(redBar);
      timeline.getKeyFrames().add(greenBar);
      timeline.getKeyFrames().add(yellowBar);
      timeline.getKeyFrames().add(keyFrameInitial);

      //Set the timer to play
      timeline.play();
    });

    //Inform that the game is over
    game.setEndGameListener(isOver -> {
      //Clean up the loose ends
      cleanUp();
      Platform.runLater(() -> {
        gameWindow.startScore(game);
        logger.info("Score: " + game.getScoreProperty());
      });
    });

    //Left Pane (VBox and stats)
    makeVBox(mainPane);
  }


  /**
   * Class to make the vBox that holds all the stats outside build class
   *
   * @param mainPane Border Pane in build class
   */
  private void makeVBox(BorderPane mainPane) {
    logger.info("Making stats box");
    VBox vBox = new VBox(5);
    mainPane.setLeft(vBox);
    vBox.setPadding(new Insets(0, 0, 0, 50));
    vBox.setAlignment(Pos.TOP_CENTER);

    //Score
    var score = new Text("Score");
    score.getStyleClass().add("heading");
    vBox.getChildren().add(score);

    //Score Display
    var scoreDisplay = new Text(String.valueOf(game.score.get()));
    scoreDisplay.textProperty().bind(game.score.asString());
    scoreDisplay.getStyleClass().add("score");
    vBox.getChildren().add(scoreDisplay);

    //Level
    var level = new Text("Level");
    level.getStyleClass().add("heading");
    vBox.getChildren().add(level);

    //Level Display
    var levelDisplay = new Text(String.valueOf(game.level.get()));
    levelDisplay.textProperty().bind(game.level.asString());
    levelDisplay.getStyleClass().add("level");
    vBox.getChildren().add(levelDisplay);

    //Lives
    var lives = new Text("Lives Left");
    lives.getStyleClass().add("heading");
    vBox.getChildren().add(lives);

    //Lives Display
    var livesDisplay = new Text(String.valueOf(game.lives.get()));
    livesDisplay.textProperty().bind(game.lives.asString());
    livesDisplay.getStyleClass().add("lives3");
    vBox.getChildren().add(livesDisplay);

    //Upcoming Shape
    var currentShape = new Text("Current Shape");
    currentShape.getStyleClass().add("heading");
    vBox.getChildren().add(currentShape);

    var stackPane = new StackPane();
    stackPane.setAlignment(Pos.CENTER);
    vBox.getChildren().add(stackPane);

    //Upcoming shape Display
    var upcomingPiece = new PieceBoard(3, 3, 100, 100);
    upcomingPiece.setOnBlockClick(this::rightClicked);
    pieceListener = upcomingPiece::displayPiece;
    game.setNextListener(pieceListener);
    stackPane.getChildren().add(upcomingPiece);
    upcomingPiece.displayCircle(50, 50, stackPane);

    //Next Shape
    var nextShape = new Text("Next Shape");
    nextShape.getStyleClass().add("heading");
    vBox.getChildren().add(nextShape);

    //Next Shape Display
    var followingPiece = new PieceBoard(3, 3, 70, 70);

    followingPieceListener = followingPiece::displayPiece;
    game.setFollowingPieceListener(followingPieceListener);
    vBox.getChildren().add(followingPiece);
  }

  /**
   * Get the highScore from the score file
   *
   * @return value of the highScore
   */
  private int getHighScore() {
    //Set the highScore to 0 to get the new one
    int highScore = 0;

    //Set the buffer to null
    BufferedReader br = null;
    try {
      //Read file
      br = new BufferedReader(new FileReader("scores.txt"));

      //Get first line
      String line = br.readLine();

      //Split the line by :
      String[] split = line.split(":");

      //Get the second part that is value
      highScore = Integer.parseInt(split[1].trim());

      logger.info("High Score is: " + highScore);

    } catch (IOException e) {
      //Catch if the file does not exist
      e.printStackTrace();
      logger.info("File does not exist");
    } finally {
      //Close the buffer reader
      try {
        if (br != null) {
          br.close();
        }
      } catch (Exception e) {
        e.printStackTrace();
        logger.info("Could not close Buffer");
      }
    }

    //Return highScore
    return highScore;
  }

  /**
   * Handle when a block is clicked
   *
   * @param gameBlock the Game Block that was clocked
   */
  private void blockClicked(GameBlock gameBlock) {
    logger.info("Block clicked");
    //Run block blocked on the gameBlock
    game.blockClicked(gameBlock);

    //Change the highScore to the game score if it is greater than the current highScore
    if (game.score.get() > Integer.parseInt(highScore.getText())) {
      highScore.setText(String.valueOf(game.score.get()));
    }

    //Update the block to look focused
    pieceHighlight();
    logger.info("Piece highlighted");
  }

  /**
   * Rotate the current piece 90 degrees in the UI
   *
   * @param gameBlock piece to rotate
   */
  private void rightClicked(GameBlock gameBlock) {
    //Rotate the piece
    game.rotateCurrentPiece(1);
    logger.info("Piece rotated");

    //Update the block to look focused
    pieceHighlight();
    logger.info("Piece highlighted");
  }

  /**
   * Hover over by mouse or keys to focus
   *
   * @param gameBlock block that is hovered
   */
  private void blockHovered(GameBlock gameBlock) {
    logger.info("Touching block: {}, {}", gameBlock.getX(), gameBlock.getY());
    game.setAimX(gameBlock.getX());
    game.setAimY(gameBlock.getY());

    //Update the block to look focused
    pieceHighlight();
    logger.info("Piece highlighted");
  }

  /**
   * Show a highlight of the current piece on the gameBoard UI
   */
  private void pieceHighlight() {
    logger.info("Showing current piece highlight");

    //Get the currentBlock
    GameBlock currentBlock = board.getBlock(game.getAimX(), game.getAimY());

    //Update the gameBoard to show a highlight of the gamePiece
    for (GameBlock[] rowBlock : board.getBlocks()) {
      for (GameBlock block : rowBlock) {
        //Make a highlight of the current piece at the current block
        block.paintHighlight(game.getCurrentPiece(), currentBlock);
      }
    }
  }

  /**
   * Setup the game object and model
   */
  public void setupGame() {
    logger.info("Starting a new challenge");

    //Start new game
    game = new Game(5, 5);
  }

  /**
   * Clean up the loose ends
   */
  private void cleanUp() {
    //Make listeners the null
    pieceListener = null;
    followingPieceListener = null;

    //Stop the timer UI
    timeline.stop();
  }

  /**
   * Initialise the scene and start the game
   */
  @Override
  public void initialise() {
    logger.info("Initialising Challenge");

    //Stop and start the music
    Multimedia.stopAudio();
    Multimedia.playBackgroundMusic("game.wav");

    //Start the game
    game.start();

    //Handle on key presses
    scene.setOnKeyPressed(this::handle);
  }

  /**
   * Handle the input of keyboard presses
   *
   * @param event keyboard input
   */
  private void handle(KeyEvent event) {
    logger.info("Handle key press");

    switch (event.getCode()) {
      //Leave Scene
      case ESCAPE -> {
        logger.info("Game stopped");
        game.stop();
        gameWindow.startMenu();
      }
      //Move piece up
      case UP, W -> {
        game.currentAim(0, -1);
        //Update highlight of piece
        pieceHighlight();
      }
      //Move piece down
      case DOWN, S -> {
        game.currentAim(0, 1);
        //Update highlight of piece

        pieceHighlight();
      }
      //Move piece right
      case RIGHT, D -> {
        game.currentAim(1, 0);
        //Update highlight of piece

        pieceHighlight();
      }
      //Move piece left
      case LEFT, A -> {
        game.currentAim(-1, 0);
        //Update highlight of piece
        pieceHighlight();
      }
      //Place piece
      case ENTER, X -> {
        game.placeBlock(game.getAimX(), game.getAimY());
        //Update highlight of piece
        pieceHighlight();
      }
      //Rotate piece 90 degrees
      case Q, Z, OPEN_BRACKET -> {
        game.rotateCurrentPiece(3);
        //Update highlight of piece
        pieceHighlight();
      }
      //Rotate piece 270 degrees
      case E, C, CLOSE_BRACKET -> {
        game.rotateCurrentPiece(1);
        //Update highlight of piece
        pieceHighlight();
      }
      //Swap piece
      case SPACE, R -> {
        game.swapCurrentPiece();
        //Update highlight of piece
        pieceHighlight();
      }
    }
  }
}
