package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.component.PieceBoard;
import uk.ac.soton.comp1206.game.GamePiece;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * Instruction Scene extends BaseSecene
 * <p>
 * The Instructions scene. Holds the UI for the Instructions and dynamic display of the game
 * pieces.
 */

public class InstructionsScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(InstructionsScene.class);

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public InstructionsScene(GameWindow gameWindow) {
    //Inherit from super
    super(gameWindow);
    logger.info("Creating Instructions Scene");
  }

  /**
   * Build the instructions scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    //Make root
    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Make organization pane
    var mainPane = new VBox(20);
    mainPane.setAlignment(Pos.CENTER);
    mainPane.setMaxWidth(gameWindow.getWidth());
    mainPane.setMaxHeight(gameWindow.getHeight());
    mainPane.getStyleClass().add("menu-background");
    root.getChildren().add(mainPane);

    //Add instructions image
    var logoImage = new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/Instructions.png"))
            .toExternalForm());
    var logo = new ImageView(logoImage);
    logo.setFitWidth(gameWindow.getWidth() / 1.35);
    logo.setPreserveRatio(true);
    logo.setSmooth(true);
    logo.setCache(true);
    mainPane.getChildren().add(logo);

    //Add pieces in grid pane
    var gridPane = new GridPane();
    gridPane.setAlignment(Pos.CENTER);
    gridPane.setHgap(15);
    gridPane.setVgap(15);

    //Initiate the counter that changes the piece
    int count = 0;

    //Put the pieceBoards into the grid
    for (var y = 0; y < 3; y++) {
      for (var x = 0; x < 6; x++) {
        //Make new pieceBoard
        var pieceBoard = new PieceBoard(3, 3, 50, 50);
        //Display piece in UI
        pieceBoard.displayPiece(GamePiece.createPiece(count));
        //Add the piece to the grid
        gridPane.add(pieceBoard, x, y);
        //Increase count
        count++;
        logger.info("Adding piece: " + count);
      }
    }
    //Add the grid to the organization pane
    mainPane.getChildren().add(gridPane);
  }

  /**
   * Initialise the scene
   */
  @Override
  public void initialise() {
    logger.info("Initialising the instruction scene");

    //Stop and stat music
    Multimedia.stopAudio();
    Multimedia.playBackgroundMusic("menu.mp3");

    //Handle the escape key
    escape();
  }

  /**
   * Handle the escape key
   */
  public void escape() {
    scene.setOnKeyPressed(event -> {
      logger.info("Key pressed: " + event.getCode());

      //Return to start menu on escape key press
      if (event.getCode() == KeyCode.ESCAPE) {
        gameWindow.startMenu();
      }
    });
  }
}
