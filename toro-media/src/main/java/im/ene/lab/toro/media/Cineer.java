/*
 * Copyright 2016 eneim@Eneim Labs, nam@ene.im
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package im.ene.lab.toro.media;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.FloatRange;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.view.Surface;
import android.view.View;
import android.widget.MediaController;
import android.widget.VideoView;
import java.io.IOException;
import java.util.Map;

/**
 * Created by eneim on 6/2/16.
 */
public interface Cineer {

  /**
   * See {@link State}
   */
  int PLAYER_IDLE = 0;
  int PLAYER_PREPARING = 1;
  int PLAYER_PREPARED = 2;
  int PLAYER_BUFFERING = 3;
  int PLAYER_READY = 4;
  int PLAYER_ENDED = 5;

  /**
   * Shared API for any Med
   */
  interface Player {

    /**
     * See {@link VideoView#start()}
     */
    void start();

    /**
     * See {@link VideoView#pause()}
     */
    void pause();

    /**
     * See {@link VideoView#stopPlayback()}
     */
    void stop();

    /**
     * See {@link VideoView#getDuration()}
     *
     * @return media's duration.
     */
    long getDuration();

    /**
     * See {@link VideoView#getCurrentPosition()}
     *
     * @return current playback position.
     */
    long getCurrentPosition();

    /**
     * See {@link VideoView#seekTo(int)}
     *
     * @param pos seek to specific position.
     */
    void seekTo(long pos);

    /**
     * See {@link VideoView#isPlaying()}
     *
     * @return {@code true} if the media is being played, {@code false} otherwise.
     */
    boolean isPlaying();

    /**
     * See {@link VideoView#getBufferPercentage()}
     *
     * @return current buffered percentage.
     */
    @IntRange(from = 0, to = 100) int getBufferPercentage();

    /**
     * Get the audio session id for the player used by this VideoView. This can be used to
     * apply audio effects to the audio track of a video.
     *
     * See {@link VideoView#getAudioSessionId()}
     *
     * @return The audio session, or 0 if there was an error.
     */
    int getAudioSessionId();

    void setBackgroundAudioEnabled(boolean enabled);

    void setMedia(@NonNull Media source);

    void setMedia(Uri uri);

    /**
     * See {@link MediaPlayer#setVolume(float, float)}
     *
     * @param volume volume level.
     */
    void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);

    void setOnPlayerStateChangeListener(OnPlayerStateChangeListener listener);
  }

  /**
   * Extension for Video player widgets.
   */
  interface VideoPlayer extends Player {

    int getVideoHeight();

    int getVideoWidth();
  }

  interface Controller {

    /** see {@link MediaController#hide()} */
    void hide();

    /** see {@link MediaController#show()} */
    void show();

    /** see {@link MediaController#show(int)} */
    void show(int timeout);

    /** see {@link MediaController#setMediaPlayer(MediaController.MediaPlayerControl)} */
    void setMediaPlayer(Player player);

    /** see {@link MediaController#setAnchorView(View)} */
    void setAnchorView(View anchorView);

    /** see {@link MediaController#setEnabled(boolean)} */
    void setEnabled(boolean enabled);

    /** see {@link MediaController#isShowing()} */
    boolean isShowing();
  }

  /** see {@link MediaPlayer#start()} */
  void start() throws IllegalStateException;

  /** see {@link MediaPlayer#pause()} */
  void pause();

  /** see {@link MediaPlayer#stop()} */
  void stop();

  /** see {@link MediaPlayer#release()} */
  void release();

  /** see {@link MediaPlayer#reset()} */
  void reset();

  /** see {@link MediaPlayer#getDuration()} */
  long getDuration();

  /** see {@link MediaPlayer#getCurrentPosition()} */
  long getCurrentPosition();

  /** see {@link MediaPlayer#seekTo(int)} */
  void seekTo(long milliSec);

  /** see {@link MediaPlayer#isPlaying()} */
  boolean isPlaying();

  /** see {@link MediaPlayer#setAudioSessionId(int)} */
  void setAudioSessionId(int audioSessionId);

  /** see {@link MediaPlayer#getAudioSessionId()} */
  int getAudioSessionId();

  /** see {@link MediaPlayer#getVideoWidth()} */
  int getVideoWidth();

  /** see {@link MediaPlayer#getVideoHeight()} ()} */
  int getVideoHeight();

  /** see ExoPlayer#getBufferedPercentage() */
  int getBufferedPercentage();

  @Deprecated
  /** see {@link MediaPlayer#setOnVideoSizeChangedListener(MediaPlayer.OnVideoSizeChangedListener)} */
  void setOnVideoSizeChangedListener(OnVideoSizeChangedListener listener);

  /** see {@link MediaPlayer#setOnInfoListener(MediaPlayer.OnInfoListener)} */
  void setOnInfoListener(OnInfoListener listener);

  void setPlayerStateChangeListener(OnPlayerStateChangeListener listener);

  /** see {@link MediaPlayer#setDataSource(Context, Uri, Map)} */
  void setDataSource(Context context, Uri uri, Map<String, String> headers)
      throws IOException, IllegalArgumentException, SecurityException, IllegalStateException;

  /** see {@link MediaPlayer#setSurface(Surface)} */
  void setSurface(Surface surface);

  /** see {@link MediaPlayer#setAudioStreamType(int)} */
  void setAudioStreamType(int audioStreamType);

  /** see {@link MediaPlayer#setScreenOnWhilePlaying(boolean)} */
  void setScreenOnWhilePlaying(boolean screenOnWhilePlaying);

  /** see {@link MediaPlayer#prepareAsync()} */
  void prepareAsync() throws IllegalStateException;

  /** see {@link MediaPlayer#setVolume(float, float)} */
  void setVolume(@FloatRange(from = 0.f, to = 1.f) float volume);
}
