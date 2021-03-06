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

package im.ene.lab.toro.sample.presentation.basic2;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by eneim on 6/29/16.
 *
 * A sample {@link RecyclerView.ViewHolder} skeleton.
 */
public abstract class Basic2ViewHolder extends RecyclerView.ViewHolder {

  static int TYPE_VIDEO = 1;

  static int TYPE_NORMAL = 2;

  public Basic2ViewHolder(View itemView) {
    super(itemView);
  }

  public abstract void bind(RecyclerView.Adapter adapter, Object item);
}
