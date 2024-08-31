package uk.ac.soton.comp1206.scene;

import java.util.Objects;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.App;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * The main menu of the game. Provides a gateway to the rest of the game.
 */
public class MenuScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(MenuScene.class);

  /**
   * Create a new menu scene
   *
   * @param gameWindow the Game Window this will be displayed in
   */
  public MenuScene(GameWindow gameWindow) {
    super(gameWindow);
    logger.info("Creating Menu Scene");
  }

  /**
   * Build the menu layout
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Make stack pane to put main pane over the background
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("menu-background");
    root.getChildren().add(menuPane);

    //Make main border pane
    var mainPane = new BorderPane();
    menuPane.getChildren().add(mainPane);

    //Top pane (Logo)
    var logoImage = new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/ECSGames.png"))
            .toExternalForm());
    var logo = new ImageView(logoImage);
    BorderPane.setAlignment(logo, Pos.CENTER);
    logo.setFitWidth(150);
    logo.setPreserveRatio(true);
    logo.setSmooth(true);
    logo.setCache(true);
    mainPane.setTop(logo);
    animateDefault(logo);

    //Centre Pane (Title)
    var titleImage = new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/TetrECS.png"))
            .toExternalForm());
    var title = new ImageView(titleImage);
    BorderPane.setAlignment(title, Pos.CENTER);
    title.setFitWidth(600);
    title.setPreserveRatio(true);
    title.setSmooth(true);
    title.setCache(true);
    mainPane.setCenter(title);
    animateTitle(title);

    //Bottom Pane (HBox and Buttons)
    var hBox = new HBox(25);
    hBox.setAlignment(Pos.CENTER);
    hBox.setPrefHeight(gameWindow.getHeight() / 4.0);
    mainPane.setBottom(hBox);

    //Make buttons
    makeButton(hBox, "SINGLE PLAYER", this::startGame);
    makeButton(hBox, "MULTIPLAYER", this::startLobby);
    makeButton(hBox, "INSTRUCTIONS", this::startInstructions);
  }

  /**
   * Make start menu buttons
   *
   * @param hBox  node to put buttons on
   * @param title name of the button
   * @param event handle button press
   */
  private void makeButton(HBox hBox, String title, EventHandler<ActionEvent> event) {
    //Make button
    var button = new Button(title);
    button.getStyleClass().add("buttons");
    hBox.getChildren().add(button);

    //Handle event
    button.setOnAction(event);

    //Animate button
    animateDefault(button);
  }

  /**
   * Initialise the menu
   */
  @Override
  public void initialise() {
    //Stop and start music
    Multimedia.stopAudio();
    Multimedia.playBackgroundMusic("menu.mp3");

    //Handle escape key pressed
    escape();
  }

  /**
   * Handle when the Start single player button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    logger.info("Start game");
    gameWindow.startChallenge();
  }

  /**
   * Handle when the instruction button is pressed
   *
   * @param event event
   */
  private void startInstructions(ActionEvent event) {
    logger.info("Start Instructions");
    gameWindow.startInstructions();
  }

  /**
   * Handle when the Start multi player button is pressed
   *
   * @param event event
   */
  private void startLobby(ActionEvent event) {
    logger.info("Start Multiplayer");
    gameWindow.startLobby();
  }

  /**
   * Basic animation for nodes
   *
   * @param node node yo animate
   */
  private void animateDefault(Node node) {
    logger.info("Animate node: " + node);

    //Scale Transition to increase over time
    ScaleTransition st = new ScaleTransition(Duration.millis(2000), node);
    st.setFromX(0.0);
    st.setToX(1.0);
    st.setFromY(0.0);
    st.setToY(1.0);

    //Fade Transition to appear over time
    FadeTransition ft = new FadeTransition(new Duration(4000), node);
    ft.setFromValue(0);
    ft.setToValue(1);

    //Combined animations to start together
    ParallelTransition parTransition = new ParallelTransition(node);
    parTransition.getChildren().addAll(st, ft);
    parTransition.play();
  }

  /**
   * Animate the title specifically
   *
   * @param node node to animate
   */
  private void animateTitle(Node node) {
    logger.info("Animate Title");

    //Scale Transition to increase over time
    ScaleTransition st = new ScaleTransition(Duration.millis(4000), node);
    st.setFromX(0.0);
    st.setToX(1.0);
    st.setFromY(0.0);
    st.setToY(1.0);

    //Rotate Transition to spin over time
    RotateTransition rt = new RotateTransition(Duration.millis(4000), node);
    rt.setFromAngle(0.0);
    rt.setToAngle(360.0);

    //Fade Transition to appear over time
    FadeTransition ft = new FadeTransition(new Duration(6000), node);
    ft.setFromValue(0);
    ft.setToValue(1);

    //Rotate Transition to indefinably sway
    RotateTransition infiniteSway = new RotateTransition(Duration.millis(5000), node);
    infiniteSway.setFromAngle(0.0);
    infiniteSway.setToAngle(10.0);
    infiniteSway.setCycleCount(RotateTransition.INDEFINITE);
    infiniteSway.setAutoReverse(true);
    //Delay the swaying
    infiniteSway.setDelay(new Duration(4000));

    //Combined animations to start together
    ParallelTransition parTransition = new ParallelTransition(node);
    parTransition.getChildren().addAll(rt, ft, st, infiniteSway);
    parTransition.play();
  }

  /**
   * Handle escape key pressed
   */
  public void escape() {
    scene.setOnKeyPressed(event -> {
      logger.info("Key pressed: " + event.getCode());
      if (event.getCode() == KeyCode.ESCAPE) {
        //Close the app
        App.getInstance().shutdown();
      }
    });
  }
}
