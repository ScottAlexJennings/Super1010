package uk.ac.soton.comp1206.scene;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.game.Game;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;
import uk.ac.soton.comp1206.ui.ScoresList;

/**
 * The Score scene extends baseScene
 * <p>
 * The score scene that holds the UI for the local and online scores
 */
public class ScoresScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(ScoresScene.class);

  /**
   * SimpleListProperty that holds a list of pairs of string and integers
   */
  private final SimpleListProperty<Pair<String, Integer>> localScores = new SimpleListProperty<>(
      FXCollections.observableArrayList());

  /**
   * SimpleListProperty that holds a list of pairs of string and integers
   */
  private final SimpleListProperty<Pair<String, Integer>> remoteScores = new SimpleListProperty<>(
      FXCollections.observableArrayList());

  /**
   * Global variable to store the final game state of the game.
   */
  private final Game game;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public ScoresScene(GameWindow gameWindow, Game game) {
    super(gameWindow);
    this.game = game;
    logger.info("Creating Score Scene");
  }

  /**
   * Make a default score list .txt file if file does not already exist
   *
   * @param filename name of the file
   */
  public static void makeDefaultScore(String filename) {
    //Make new file
    File file = new File(filename);
    logger.info("Make new file");

    //If it does not exist create one
    if (!file.exists()) {
      try {
        //Make file writer
        FileWriter fw = new FileWriter(file);

        //Add scores to file
        for (int i = 0; i <= 9; i++) {
          //Increase scores by 1000
          fw.write("Scott:" + 1000 * (10 - i) + "\n");
        }
        //Close file writer
        fw.close();

      } catch (IOException e) {
        logger.info("File does not exist");
        e.printStackTrace();
      }
    }
  }

  /**
   * Start score window
   */
  @Override
  public void initialise() {
    //Start and stop music
    Multimedia.stopAudio();
    Multimedia.playBackgroundMusic("menu.mp3");

    //Handle key pressed
    scene.setOnKeyPressed(this::handle);
  }

  /**
   * Build scene
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //Build stackPane to put mainPane over background
    var menuPane = new StackPane();
    menuPane.setMaxWidth(gameWindow.getWidth());
    menuPane.setMaxHeight(gameWindow.getHeight());
    menuPane.getStyleClass().add("score-background");
    root.getChildren().add(menuPane);

    //Communicator listener to load online scores
    gameWindow.getCommunicator().addListener(this::loadOnlineScores);

    //Communicator listener to fetch online high scores
    gameWindow.getCommunicator().send("HISCORES");

    //Vbox to make mainpane to add most nodes too
    var mainPane = new VBox(20);
    mainPane.setAlignment(Pos.CENTER);
    menuPane.getChildren().add(mainPane);

    //Title (TetrECS)
    var logoImage = new Image(
        Objects.requireNonNull(this.getClass().getResource("/images/TetrECS.png"))
            .toExternalForm());
    var logo = new ImageView(logoImage);
    logo.setFitWidth(550);
    logo.setPreserveRatio(true);
    logo.setSmooth(true);
    logo.setCache(true);
    mainPane.getChildren().add(logo);

    //Game Over text
    var gameOverText = new Text("Game Over");
    gameOverText.getStyleClass().add("bigtitle");
    mainPane.getChildren().add(gameOverText);

    //High scores text
    var highScoresText = new Text("Highscores");
    highScoresText.getStyleClass().add("title");
    mainPane.getChildren().add(highScoresText);

    //Load the local scores
    loadScores("scores.txt");

    //Check there is a new high scores
    checkNewHighscore();

    //If true make a textField for the username to display on the leaderboards
    if (checkNewHighscore()) {
      logger.info("New high score ");

      //Make TextField
      var userField = new TextField();
      userField.setPromptText("Enter username");
      userField.setMaxWidth(200);
      mainPane.getChildren().add(userField);

      //On enter of username
      userField.setOnAction(event -> {
        //Get text from textField
        var username = userField.getText();
        logger.info("Username entered");

        //If new score beats online highScores add to leaderboard
        if (remoteScores.size() < 10 || game.getScoreProperty() > remoteScores.get(9).getValue()) {
          logger.info("Writing new score to server");

          //Add score to online scores
          this.remoteScores.add(new Pair<>(username, game.getScoreProperty()));

          //Add score to UI
          writeOnlineScores(username, game.getScoreProperty().toString());
        }

        //Add high score to local scores
        this.localScores.add(new Pair<>(username, game.getScoreProperty()));

        //Sort scores both local and remote
        sortScore(this.localScores);
        sortScore(this.remoteScores);

        //Update score file
        writeScores();

        //Remove textField
        mainPane.getChildren().remove(userField);

        //Scores display side by side
        var hbox = new HBox(100);
        hbox.setAlignment(Pos.CENTER);
        mainPane.getChildren().add(hbox);

        //Display local and remote scores in UI
        var localScores = new ScoresList(this.localScores, "Local");
        hbox.getChildren().add(localScores);
        var remoteScores = new ScoresList(this.remoteScores, "Multiplayer");
        hbox.getChildren().add(remoteScores);
      });

    } else {
      //Scores not new high score
      logger.info("No new high score, displaying old");

      //Scores display side by side
      var hbox = new HBox(100);
      hbox.setAlignment(Pos.CENTER);
      mainPane.getChildren().add(hbox);

      //Display local and remote scores in UI
      var localScores = new ScoresList(this.localScores, "Local");
      hbox.getChildren().add(localScores);
      var remoteScores = new ScoresList(this.remoteScores, "Multiplayer");
      hbox.getChildren().add(remoteScores);
    }
  }

  /**
   * Write the local scores to the score file
   */
  private void writeScores() {
    //Make a new file
    File file = new File("scores.txt");

    try {
      //Make new fileWriter
      FileWriter fw = new FileWriter(file);

      //Write the local scores to file
      for (Pair<String, Integer> pair : this.localScores) {
        fw.write(pair.getKey() + ":" + pair.getValue() + "\n");
      }
      //Close fileWriter
      fw.close();

    } catch (IOException e) {
      e.printStackTrace();
      logger.info("No file found");
    }
  }

  /**
   * Sort the scores of the Simple list by value
   *
   * @param scores simple list of pair of scores
   */
  private void sortScore(SimpleListProperty<Pair<String, Integer>> scores) {
    logger.info("Score Sorted");

    //Sort the scores based on value
    scores.sort((p1, p2) -> p2.getValue() - p1.getValue());
  }

  private boolean checkNewHighscore() {
    //Reset the is new high score boolean
    boolean isNew = false;

    //Compare the scores in local scores to current score
    for (Pair<String, Integer> pair : this.localScores) {

      //Return true if it is a new high score
      if (game.getScoreProperty() > pair.getValue()) {
        logger.info("New score: " + game.getScoreProperty());

        isNew = true;
        break;
      }
      logger.info("Not a new highscore");
    }
    //Sort the local scores
    sortScore(this.localScores);

    logger.info("New high score state: " + isNew);
    return isNew;
  }

  /**
   * Load local scores from file
   *
   * @param filename name of file to load from
   */
  public void loadScores(String filename) {
    logger.info("Loading scores");

    //Make a file if it doesn't already exist
    makeDefaultScore(filename);

    //Set the buffer reader to null
    BufferedReader br = null;

    try {
      //Make a line
      String line;

      //Make a new buffer reader
      br = new BufferedReader(new FileReader(filename));

      //Iterate though the file while there is another line
      while ((line = br.readLine()) != null) {
        //Split teh line by :
        String[] split = line.split(":");

        //Add the scores to the local scores
        this.localScores.add(new Pair<>(split[0], Integer.valueOf(split[1].trim())));
      }
      logger.info("Size of array is " + localScores.size());

    } catch (IOException e) {
      e.printStackTrace();
      logger.info("File does not exist");

    } finally {
      try {
        if (br != null) {
          //Close Buffer
          br.close();
        }
      } catch (Exception e) {
        logger.info("No buffer to close");
        e.printStackTrace();
      }
    }
    //Sort the local scores
    sortScore(this.localScores);
  }

  /**
   * Load the online scores
   *
   * @param message Communicator message
   */
  private void loadOnlineScores(String message) {
    logger.info("Loading online scores");

    //Remove the start of the message
    message = message.replace("HISCORES ", "");

    logger.info("Separating scores sting into scores");
    //Separate online high scores to individual scores
    String[] highScores = message.split("\n");

    //Format and add the scores to remote scores
    for (String score : highScores) {
      //Split to get the String and Integer
      String[] pair = score.split(":");

      logger.info("Adding scores to remote score sheet");
      //Add scores to remote
      remoteScores.add(new Pair<>(pair[0], Integer.valueOf(pair[1].trim())));
    }
  }

  /**
   * write the new high score to the communicator
   *
   * @param username name of the user
   * @param value    value of the new high score
   */
  private void writeOnlineScores(String username, String value) {
    //Update the communicator to add the high score
    logger.info("Update online scores");
    gameWindow.getCommunicator().send("HISCORE " + username + ":" + value);
  }

  /**
   * Handle escape key pressed
   *
   * @param event key pressed
   */
  private void handle(KeyEvent event) {
    if (event.getCode() == KeyCode.ESCAPE) {
      logger.info("Game stopped");

      //Load start menu
      gameWindow.startMenu();
    }
  }
}

