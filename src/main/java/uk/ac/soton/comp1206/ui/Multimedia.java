package uk.ac.soton.comp1206.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Multimedia class implements sound across the app
 * <p>
 * Methods to make sound effects and background music
 */

public class Multimedia {

  private static final Logger logger = LogManager.getLogger(Multimedia.class);

  /**
   * BooleanProperty to change audio state
   */
  private static final BooleanProperty audioEnabled = new SimpleBooleanProperty(true);

  /**
   * Media player to play sounds
   */
  private static MediaPlayer mediaPlayer;

  /**
   * Media player to play music
   */
  private static MediaPlayer backgroundPlayer;

  /**
   * play background music
   *
   * @param music name of music
   */
  public static void playBackgroundMusic(String music) {
    //Don't play is audio disabled
    if (getAudioEnabled()) {
      return;
    }

    //File to play
    String toPlay = Multimedia.class.getResource("/music/" + music).toString();
    logger.info("Playing audio: " + toPlay);

    try {
      //Make new media
      Media play = new Media(toPlay);

      //Initialise media player
      backgroundPlayer = new MediaPlayer(play);
      backgroundPlayer.setCycleCount(MediaPlayer.INDEFINITE);
      backgroundPlayer.setVolume(0.05);

      //Play media
      backgroundPlayer.play();
    } catch (Exception e) {
      //Disable audio
      setAudioEnabled(false);

      e.printStackTrace();
      logger.error(e.toString());
      logger.info("Could not play audio: " + music);
    }
  }

  /**
   * play sound effect
   *
   * @param sound name of sound
   */
  public static void playSound(String sound) {
    //Don't play is audio disabled
    if (getAudioEnabled()) {
      return;
    }

    //File to play
    String toPlay = Multimedia.class.getResource("/sounds/" + sound).toString();
    logger.info("Playing audio: " + toPlay);

    try {
      //Make new media
      Media play = new Media(toPlay);

      //Initialise media player
      mediaPlayer = new MediaPlayer(play);
      mediaPlayer.setVolume(0.1);

      //Play media
      mediaPlayer.play();
    } catch (Exception e) {
      //Disable audio
      setAudioEnabled(false);

      e.printStackTrace();
      logger.error(e.toString());
      logger.info("Could not play audio: " + sound);
    }
  }

  /**
   * State of audio
   *
   * @return audio state
   */
  public static BooleanProperty audioEnabledProperty() {
    return audioEnabled;
  }

  /**
   * Get audio state
   *
   * @return audio state
   */
  public static boolean getAudioEnabled() {
    return !audioEnabledProperty().get();
  }

  /**
   * set state off audio
   *
   * @param enabled state of audio to set
   */
  public static void setAudioEnabled(boolean enabled) {
    logger.info("Audio enabled set to: " + enabled);
    audioEnabledProperty().set(enabled);
  }

  /**
   * Stop audio
   */
  public static void stopAudio() {
    try {
      //Stop audio
      backgroundPlayer.stop();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
