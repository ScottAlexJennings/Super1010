package uk.ac.soton.comp1206.scene;

import java.util.Calendar;
import java.util.Formatter;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import uk.ac.soton.comp1206.ui.GamePane;
import uk.ac.soton.comp1206.ui.GameWindow;
import uk.ac.soton.comp1206.ui.Multimedia;

/**
 * Lobby Scene extends BaseScene
 * <p>
 * The Lobby scene. Holds the UI for the Lobby window and multiplayer game logic
 */

public class LobbyScene extends BaseScene {

  private static final Logger logger = LogManager.getLogger(LobbyScene.class);

  /**
   * Initialise the main BorderPane
   */
  private BorderPane mainPane;

  /**
   * Initialise the messages VBox that contains the messages shared
   */
  private VBox messages;

  /**
   * Initialise the timer that requests new channels
   */
  private Timer timer;

  /**
   * Create a new scene, passing in the GameWindow the scene will be displayed in
   *
   * @param gameWindow the game window
   */
  public LobbyScene(GameWindow gameWindow) {
    //Inherit from baseScene
    super(gameWindow);
    logger.info("Creating Lobby Scene");
  }

  /**
   * Initialise the scene
   */
  @Override
  public void initialise() {
    logger.info("Initialise the lobby scene");

    //Stop and start music
    Multimedia.stopAudio();
    Multimedia.playBackgroundMusic("menu.mp3");

    //Handle escape key
    escape();
  }

  /**
   * Build the Lobby window
   */
  @Override
  public void build() {
    logger.info("Building " + this.getClass().getName());

    root = new GamePane(gameWindow.getWidth(), gameWindow.getHeight());

    //StackPane
    var challengePane = new StackPane();
    challengePane.setMaxWidth(gameWindow.getWidth());
    challengePane.setMaxHeight(gameWindow.getHeight());
    challengePane.getStyleClass().add("lobby-background");
    root.getChildren().add(challengePane);

    //Border Pane
    mainPane = new BorderPane();
    challengePane.getChildren().add(mainPane);

    //Top Pane title
    var title = new Text("Multiplayer");
    BorderPane.setAlignment(title, Pos.BOTTOM_CENTER);
    title.getStyleClass().add("title");
    var filler = new HBox(title);
    filler.setAlignment(Pos.BOTTOM_CENTER);
    filler.setPadding(new Insets(30));
    mainPane.setTop(filler);

    //VBox
    VBox vBox = new VBox(20);
    vBox.setPadding(new Insets(0, 0, 0, 20));
    vBox.setAlignment(Pos.TOP_CENTER);
    mainPane.setLeft(vBox);

    //Games Text
    var gamesText = new Text("Current Games");
    gamesText.getStyleClass().add("heading");
    vBox.getChildren().add(gamesText);

    //Make game button
    var button = new Button("Make new game");
    button.getStyleClass().add("buttons");
    button.setStyle("-fx-font-size: 13px;");
    vBox.getChildren().add(button);

    //Input lobby name textField
    var lobbyName = new TextField();
    lobbyName.setVisible(false);
    lobbyName.setPromptText("New lobby name");
    lobbyName.setMaxWidth(135);
    vBox.getChildren().add(lobbyName);

    //Handle button pressed
    button.setOnAction(event -> lobbyName.setVisible(true));
    //Handle create lobby
    lobbyName.setOnAction(event -> {
      //Update communicator to create new lobby
      gameWindow.getCommunicator().send("CREATE " + lobbyName.getText());
      logger.info("Creating channel: " + lobbyName.getText());

      //Make lobby chat room
      mainPane.setRight(lobbyChat(lobbyName.getText()));

      //Clear textField
      lobbyName.clear();

      //Hide textField
      lobbyName.setVisible(false);
    });

    //Make vbox for channels inside existing vbox
    var channels = new VBox(20);
    channels.setPadding(new Insets(5, 0, 0, 0));
    channels.setAlignment(Pos.TOP_CENTER);
    vBox.getChildren().add(channels);

    //Timer on infinite repeat to get channels
    repeatTimer();

    gameWindow.getCommunicator().addListener((message) -> Platform.runLater(() -> {
      logger.info("Load channels into UI");
      //Get channels that have already been made
      loadChannels(message, channels);
    }));
  }

  /**
   * Make the lobby chat box that is shown when a channel is joined
   *
   * @param name channel name
   * @return the vbox that is the chat room
   */
  private VBox lobbyChat(String name) {
    Multimedia.playSound("transition.wav");

    //Initialise the lobby chat room
    VBox chatWin = new VBox(5);
    chatWin.setAlignment(Pos.TOP_LEFT);
    chatWin.setPrefWidth(gameWindow.getWidth() / 2.0);
    chatWin.setPrefHeight(gameWindow.getHeight() / 1.5);
    chatWin.getStyleClass().add("chat-window");

    //Give the lobby chat a name
    var channel = new Text(name);
    channel.getStyleClass().add("level");

    //Display the names of the players in the lobby
    var players = new Text("Players: ");
    players.getStyleClass().add("chat-text");

    //Introduce them to the lobby
    var welcome = new Text("Welcome to TetrECS online \nTo change name use /nickChange in chat");
    welcome.getStyleClass().add("chat-text");

    //Make a window to display all messages
    var scroller = new ScrollPane();
    scroller.setPrefWidth(gameWindow.getWidth() / 2.0);
    scroller.setPrefHeight(gameWindow.getHeight() / 2.0);
    scroller.getStyleClass().add("scroller");

    //Make a list of all the messages
    messages = new VBox(5);
    messages.getStyleClass().add("messages");
    scroller.setContent(messages);

    //Have a place to write messages
    var text = new TextField();
    text.setPromptText("Send a text");
    text.setOnAction(event -> {
      //Update communicator to receive new message
      gameWindow.getCommunicator().send("MSG " + text.getText());

      //Clear textField
      text.clear();
    });

    //Button to start the game
    var startGame = new Button("Start Game");
    startGame.getStyleClass().add("buttons");
    startGame.setStyle("-fx-font-size: 13px;");
    startGame.setOnAction(this::startGame);

    //Button to leave the channel lobby
    var leaveChannel = new Button("Leave Channel");
    leaveChannel.getStyleClass().add("buttons");
    leaveChannel.setStyle("-fx-font-size: 13px;");
    leaveChannel.setOnAction(event -> {
      //Leave the lobby
      gameWindow.getCommunicator().send("PART");

      //Remove the lobby chat room
      mainPane.setRight(null);
    });

    //Add all the nodes to the initial VBOX
    chatWin.getChildren()
        .addAll(channel, players, welcome, scroller, text, startGame, leaveChannel);

    //Update the communicator to display the messages
    gameWindow.getCommunicator()
        .addListener((message) -> Platform.runLater(() -> receiveMessages(message)));

    //return the lobby chat box
    return chatWin;
  }

  /**
   * Format the received messages to display in the UI
   *
   * @param text communicator message to format
   */
  private void receiveMessages(String text) {
    logger.info("Get messages to add too lobby chat");

    //Make sure message is a MSG message to display
    if (text.contains(":")) {

      //Play received message sound
      Multimedia.playSound("pling.wav");

      //Remove message start
      text = text.replace("MSG ", "");

      //Split the communicator message into its separate messages to display
      String[] sepMessages = text.split("\n");

      //Add messages to Vbox messages
      for (String message : sepMessages) {
        //Split the message into parts
        String[] msg = message.split(":");

        //Make a new text
        var display = new Text();

        //Add time with calendar
        Calendar cal = Calendar.getInstance();
        Formatter fmt = new Formatter();
        //Format time to be hour and minutes
        fmt.format("%tl:%tM", cal, cal);

        //Set the text
        display.setText("[" + fmt + "] " + msg[0] + " > " + msg[1]);

        //Add message to VBox
        messages.getChildren().add(display);
      }
    }
  }

  /**
   * Load the already made channels
   *
   * @param message message from communicator
   * @param vBox    VBox that channels are added too
   */
  private void loadChannels(String message, VBox vBox) {
    logger.info("Loading channels to UI");

    //Get right message from communicator
    if (message.contains("CHANNELS")) {
      //Remove start message
      message = message.replace("CHANNELS ", "");

      //Remove previous channels
      vBox.getChildren().clear();

      //Make sure the message is a message
      if (message.length() != 0) {
        //Split the communicator message into channels
        String[] channels = message.split("\n");

        //Add the channel buttons to the UI
        for (String channel : channels) {
          //Make new button
          Button channelButton = new Button(channel);
          channelButton.getStyleClass().add("channel-buttons");
          vBox.getChildren().add(channelButton);

          //Handle button press
          channelButton.setOnAction(event -> {
            //Update communicator to join selected channel
            gameWindow.getCommunicator().send("JOIN " + channel);

            //Make lobby chat box
            mainPane.setRight(lobbyChat(channel));
          });
        }
      }
    }
  }

  /**
   * repeating timer to request current channels
   */
  private void repeatTimer() {
    logger.info("Calling repeat");

    //Declare the timer
    timer = new Timer();

    //Add and schedule communicator update
    timer.scheduleAtFixedRate(new TimerTask() {
      @Override
      public void run() {
        //Get list of channels
        gameWindow.getCommunicator().send("LIST");
        logger.info("Get channels");
      }
      //Repeat every 3 seconds with no delay
    }, 0, 3000);
  }

  /**
   * Handle when the Start single player button is pressed
   *
   * @param event event
   */
  private void startGame(ActionEvent event) {
    logger.info("Starting lobby scene");
    gameWindow.startChallenge();
  }

  /**
   * Handle escape key press
   */
  public void escape() {
    scene.setOnKeyPressed(event -> {
      logger.info("Key pressed: " + event.getCode());

      //Handle key pressed
      if (event.getCode() == KeyCode.ESCAPE) {
        //Stop audio
        Multimedia.stopAudio();

        //Stop timer
        timer.cancel();

        //Clear listeners
        gameWindow.getCommunicator().clearListeners();

        //Start menu
        gameWindow.startMenu();
        gameWindow.getCommunicator().send("PART");
      }
    });
  }
}
