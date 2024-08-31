package uk.ac.soton.comp1206.ui;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * ScoreList that extends VBox
 * <p>
 * Set up the UI display of the scores in the ScoreScene wth a transition
 */

public class ScoresList extends VBox {

  /**
   * Array of colours to display scores
   */
  public static final Color[] COLOURS = {
      Color.DEEPPINK,
      Color.RED,
      Color.ORANGE,
      Color.YELLOW,
      Color.YELLOWGREEN,
      Color.LIME,
      Color.GREEN,
      Color.DARKGREEN,
      Color.DARKTURQUOISE,
      Color.DEEPSKYBLUE
  };
  private static final Logger logger = LogManager.getLogger(ScoresList.class);
  /**
   * SimpleListProperty that holds a list of pairs of string and integers
   */
  private final SimpleListProperty<Pair<String, Integer>> highScores = new SimpleListProperty<>(
      FXCollections.observableArrayList());
  /**
   * String title of score
   */
  private final String title;

  /**
   * Constructor for the scorelist class
   *
   * @param highScores list of scores to be set onto vbox
   * @param title      title of the list
   */
  public ScoresList(SimpleListProperty<Pair<String, Integer>> highScores, String title) {
    super();
    //Set alignment of VBox
    setAlignment(Pos.TOP_CENTER);

    //Bind the scores between UI and logic
    this.highScores.bind(highScores);

    //Set title of scores
    this.title = title;

    //Animate score reveal
    reveal();
  }

  /**
   * Clear the old list, make the title and populate the leaderboard
   */
  public void organiseList() {
    logger.info("Organise the scores");

    //Clear the previous scores
    getChildren().clear();

    //Make title
    var titleText = new Text(this.title);
    titleText.getStyleClass().add("scoreitem");
    titleText.setStyle("-fx-fill: white;");
    getChildren().add(titleText);

    //Populate VBox with the scores
    for (int i = 0; i < 10; i++) {
      //Make new text to add to VBox
      var score = new Text(
          this.highScores.get(i).getKey() + ": " + this.highScores.get(i).getValue());

      score.getStyleClass().add("scoreitem");
      score.setFill(COLOURS[i]);
      getChildren().add(score);
    }
  }

  /**
   * Animate the reveal of the leaderboard
   */
  private void reveal() {
    logger.info("Animate the reveal the scores");
    organiseList();

    //Scale Transition to increase over time
    ScaleTransition st = new ScaleTransition(Duration.millis(3000), this);
    st.setFromX(0.0);
    st.setToX(1.0);
    st.setFromY(0.0);
    st.setToY(1.0);

    //Fade Transition to appear over time
    FadeTransition ft = new FadeTransition(new Duration(4000), this);
    ft.setFromValue(0);
    ft.setToValue(1);

    //Combined animations to start together
    ParallelTransition parTransition = new ParallelTransition(this);
    parTransition.getChildren().addAll(st, ft);
    parTransition.play();
  }
}
