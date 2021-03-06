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
package im.ene.lab.toro.player.internal;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaCodec;
import android.os.Build;
import android.os.Handler;
import com.google.android.exoplayer.DefaultLoadControl;
import com.google.android.exoplayer.LoadControl;
import com.google.android.exoplayer.MediaCodecSelector;
import com.google.android.exoplayer.MediaCodecVideoTrackRenderer;
import com.google.android.exoplayer.SampleSource;
import com.google.android.exoplayer.TrackRenderer;
import com.google.android.exoplayer.audio.AudioCapabilities;
import com.google.android.exoplayer.hls.DefaultHlsTrackSelector;
import com.google.android.exoplayer.hls.HlsChunkSource;
import com.google.android.exoplayer.hls.HlsMasterPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylist;
import com.google.android.exoplayer.hls.HlsPlaylistParser;
import com.google.android.exoplayer.hls.HlsSampleSource;
import com.google.android.exoplayer.hls.PtsTimestampAdjusterProvider;
import com.google.android.exoplayer.metadata.MetadataTrackRenderer;
import com.google.android.exoplayer.metadata.id3.Id3Frame;
import com.google.android.exoplayer.metadata.id3.Id3Parser;
import com.google.android.exoplayer.text.TextTrackRenderer;
import com.google.android.exoplayer.text.eia608.Eia608TrackRenderer;
import com.google.android.exoplayer.upstream.DataSource;
import com.google.android.exoplayer.upstream.DefaultAllocator;
import com.google.android.exoplayer.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer.upstream.DefaultUriDataSource;
import com.google.android.exoplayer.util.ManifestFetcher;
import com.google.android.exoplayer.util.ManifestFetcher.ManifestCallback;
import java.io.IOException;
import java.util.List;

/**
 * A {@link ExoMediaPlayer.RendererBuilder} for HLS.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN)  //
public class HlsRendererBuilder implements ExoMediaPlayer.RendererBuilder {

  private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
  private static final int MAIN_BUFFER_SEGMENTS = 254;
  private static final int AUDIO_BUFFER_SEGMENTS = 54;
  private static final int TEXT_BUFFER_SEGMENTS = 2;

  private final Context context;
  private final String userAgent;
  private final String url;

  private AsyncRendererBuilder currentAsyncBuilder;

  public HlsRendererBuilder(Context context, String userAgent, String url) {
    this.context = context;
    this.userAgent = userAgent;
    this.url = url;
  }

  @Override
  public void buildRenderers(ExoMediaPlayer player) {
    currentAsyncBuilder = new AsyncRendererBuilder(context, userAgent, url, player);
    currentAsyncBuilder.init();
  }

  @Override
  public void cancel() {
    if (currentAsyncBuilder != null) {
      currentAsyncBuilder.cancel();
      currentAsyncBuilder = null;
    }
  }

  private static final class AsyncRendererBuilder implements ManifestCallback<HlsPlaylist> {

    private final Context context;
    private final String userAgent;
    private final ExoMediaPlayer player;
    private final ManifestFetcher<HlsPlaylist> playlistFetcher;

    private boolean canceled;

    public AsyncRendererBuilder(Context context, String userAgent, String url, ExoMediaPlayer player) {
      this.context = context;
      this.userAgent = userAgent;
      this.player = player;
      HlsPlaylistParser parser = new HlsPlaylistParser();
      playlistFetcher = new ManifestFetcher<>(url, new DefaultUriDataSource(context, userAgent),
          parser);
    }

    public void init() {
      playlistFetcher.singleLoad(player.getMainHandler().getLooper(), this);
    }

    public void cancel() {
      canceled = true;
    }

    @Override
    public void onSingleManifestError(IOException e) {
      if (canceled) {
        return;
      }

      player.onRenderersError(e);
    }

    @Override
    public void onSingleManifest(HlsPlaylist manifest) {
      if (canceled) {
        return;
      }

      Handler mainHandler = player.getMainHandler();
      LoadControl loadControl = new DefaultLoadControl(new DefaultAllocator(BUFFER_SEGMENT_SIZE));
      DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
      PtsTimestampAdjusterProvider timestampAdjusterProvider = new PtsTimestampAdjusterProvider();

      boolean haveSubtitles = false;
      boolean haveAudios = false;
      if (manifest instanceof HlsMasterPlaylist) {
        HlsMasterPlaylist masterPlaylist = (HlsMasterPlaylist) manifest;
        haveSubtitles = !masterPlaylist.subtitles.isEmpty();
        haveAudios = !masterPlaylist.audios.isEmpty();
      }

      // Build the video/id3 renderers.
      DataSource dataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
      HlsChunkSource chunkSource = new HlsChunkSource(true /* isMaster */, dataSource, manifest,
          DefaultHlsTrackSelector.newDefaultInstance(context), bandwidthMeter,
          timestampAdjusterProvider);
      HlsSampleSource sampleSource = new HlsSampleSource(chunkSource, loadControl,
          MAIN_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, player, ExoMediaPlayer.TYPE_VIDEO);
      MediaCodecVideoTrackRenderer videoRenderer = new MediaCodecVideoTrackRenderer(context,
          sampleSource, MediaCodecSelector.DEFAULT, MediaCodec.VIDEO_SCALING_MODE_SCALE_TO_FIT,
          5000, mainHandler, player, 50);
      MetadataTrackRenderer<List<Id3Frame>> id3Renderer = new MetadataTrackRenderer<>(
          sampleSource, new Id3Parser(), player, mainHandler.getLooper());

      // Build the audio renderer.
      EnhancedMediaCodecAudioTrackRenderer audioRenderer;
      if (haveAudios) {
        DataSource audioDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
        HlsChunkSource audioChunkSource = new HlsChunkSource(false /* isMaster */, audioDataSource,
            manifest, DefaultHlsTrackSelector.newAudioInstance(), bandwidthMeter,
            timestampAdjusterProvider);
        HlsSampleSource audioSampleSource = new HlsSampleSource(audioChunkSource, loadControl,
            AUDIO_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, player,
            ExoMediaPlayer.TYPE_AUDIO);
        audioRenderer = new EnhancedMediaCodecAudioTrackRenderer(
            new SampleSource[] {sampleSource, audioSampleSource}, MediaCodecSelector.DEFAULT, null,
            true, player.getMainHandler(), player, AudioCapabilities.getCapabilities(context),
            AudioManager.STREAM_MUSIC);
      } else {
        audioRenderer = new EnhancedMediaCodecAudioTrackRenderer(sampleSource,
            MediaCodecSelector.DEFAULT, null, true, player.getMainHandler(), player,
            AudioCapabilities.getCapabilities(context), AudioManager.STREAM_MUSIC);
      }

      // Build the text renderer.
      TrackRenderer textRenderer;
      if (haveSubtitles) {
        DataSource textDataSource = new DefaultUriDataSource(context, bandwidthMeter, userAgent);
        HlsChunkSource textChunkSource = new HlsChunkSource(false /* isMaster */, textDataSource,
            manifest, DefaultHlsTrackSelector.newSubtitleInstance(), bandwidthMeter,
            timestampAdjusterProvider);
        HlsSampleSource textSampleSource = new HlsSampleSource(textChunkSource, loadControl,
            TEXT_BUFFER_SEGMENTS * BUFFER_SEGMENT_SIZE, mainHandler, player, ExoMediaPlayer.TYPE_TEXT);
        textRenderer = new TextTrackRenderer(textSampleSource, player, mainHandler.getLooper());
      } else {
        textRenderer = new Eia608TrackRenderer(sampleSource, player, mainHandler.getLooper());
      }

      TrackRenderer[] renderers = new TrackRenderer[ExoMediaPlayer.RENDERER_COUNT];
      renderers[ExoMediaPlayer.TYPE_VIDEO] = videoRenderer;
      renderers[ExoMediaPlayer.TYPE_AUDIO] = audioRenderer;
      renderers[ExoMediaPlayer.TYPE_METADATA] = id3Renderer;
      renderers[ExoMediaPlayer.TYPE_TEXT] = textRenderer;
      player.onRenderers(renderers, bandwidthMeter);
    }

  }

}
