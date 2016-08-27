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

package im.ene.lab.toro.ext;

import android.support.annotation.CallSuper;
import android.view.View;
import im.ene.lab.toro.PlayerViewHelper;
import im.ene.lab.toro.ToroPlayer;
import im.ene.lab.toro.ToroPlayerViewHelper;
import im.ene.lab.toro.ToroUtil;
import im.ene.lab.toro.ToroViewHolder;
import im.ene.lab.toro.media.Cineer;
import im.ene.lab.toro.media.PlaybackException;

/**
 * Created by eneim on 1/31/16.
 *
 * Abstract implementation of {@link ToroPlayer} and {@link ToroViewHolder}.
 */
public abstract class BasePlayerViewHolder extends ToroAdapter.ViewHolder
    implements ToroPlayer, ToroViewHolder {

  protected final PlayerViewHelper mHelper;

  private View.OnLongClickListener mLongClickListener;

  public BasePlayerViewHolder(View itemView) {
    super(itemView);
    mHelper = new ToroPlayerViewHelper(this, itemView);
    if (allowLongPressSupport()) {
      if (mLongClickListener == null) {
        mLongClickListener = new View.OnLongClickListener() {
          @Override public boolean onLongClick(View v) {
            return mHelper.onItemLongClick(BasePlayerViewHolder.this,
                BasePlayerViewHolder.this.itemView, BasePlayerViewHolder.this.itemView.getParent());
          }
        };
      }

      super.setOnItemLongClickListener(mLongClickListener);
    } else {
      mLongClickListener = null;
    }
  }

  @CallSuper @Override public void onActivityActive() {

  }

  @CallSuper @Override
  public void setOnItemLongClickListener(final View.OnLongClickListener listener) {
    if (allowLongPressSupport()) {
      // Client set different long click listener, but this View holder tends to support Long
      // press, so we must support it
      if (mLongClickListener == null) {
        mLongClickListener = new View.OnLongClickListener() {
          @Override public boolean onLongClick(View v) {
            return mHelper.onItemLongClick(BasePlayerViewHolder.this, itemView,
                itemView.getParent());
          }
        };
      }
    } else {
      mLongClickListener = null;
    }

    super.setOnItemLongClickListener(new View.OnLongClickListener() {
      @Override public boolean onLongClick(View v) {
        boolean longClickHandled = false;

        if (mLongClickListener != null) {
          longClickHandled = mLongClickListener.onLongClick(v); // we can ignore this boolean result
        }

        return listener.onLongClick(v) && longClickHandled;
      }
    });
  }

  @CallSuper @Override public void onActivityInactive() {
    // Release listener to prevent memory leak
    mLongClickListener = null;
  }

  @CallSuper @Override public void onAttachedToParent() {
    mHelper.onAttachedToParent();
  }

  @CallSuper @Override public void onDetachedFromParent() {
    mHelper.onDetachedFromParent();
  }

  @Override public int getPlayOrder() {
    return getAdapterPosition();
  }

  /**
   * Allow long press to play support or not. {@code false} by default
   */
  protected boolean allowLongPressSupport() {
    return false;
  }

  @Override public void onBuffering() {

  }

  @Override public void onVideoPreparing() {

  }

  @Override public void onVideoPrepared(Cineer mp) {

  }

  @Override public void onPlaybackStarted() {

  }

  @Override public void onPlaybackPaused() {

  }

  @Override public void onPlaybackCompleted() {

  }

  @Override public boolean onPlaybackError(Cineer mp, PlaybackException error) {
    return true;  // don't want to see the annoying dialog
  }

  private static final String TAG = "ToroViewHolder";

  @Override public float visibleAreaOffset() {
    return ToroUtil.visibleAreaOffset(this, itemView.getParent());
  }

  /**
   * Indicate that this Player is able to replay right after it stops (loop-able) or not.
   *
   * @return true if this Player is loop-able, false otherwise
   */
  @Override public boolean isLoopAble() {
    return false;
  }
}
