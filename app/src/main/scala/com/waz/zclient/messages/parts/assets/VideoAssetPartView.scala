/**
 * Wire
 * Copyright (C) 2016 Wire Swiss GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.waz.zclient.messages.parts.assets

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.widget.FrameLayout
import com.waz.threading.Threading
import com.waz.utils.events.Signal
import com.waz.zclient.R
import com.waz.zclient.messages.MsgPart
import com.waz.zclient.messages.parts.assets.DeliveryState.OtherUploading
import com.waz.zclient.utils.ContextUtils._
import com.waz.zclient.views.ImageAssetDrawable
import com.waz.zclient.views.ImageAssetDrawable.State.Loaded

class VideoAssetPartView(context: Context, attrs: AttributeSet, style: Int) extends FrameLayout(context, attrs, style) with PlayableAsset with ImageLayoutAssetPart {
  def this(context: Context, attrs: AttributeSet) = this(context, attrs, 0)
  def this(context: Context) = this(context, null, 0)

  override val tpe: MsgPart = MsgPart.VideoAsset

  override def inflate() = inflate(R.layout.message_video_asset_content)

  imageDrawable.state.map {
    case Loaded(_, _, _) => getColor(R.color.white)
    case _ => getColor(R.color.black)
  }.on(Threading.Ui)(durationView.setTextColor)

  val bg = deliveryState flatMap {
    case OtherUploading => Signal.const[Drawable](assetBackground)
    case _ =>
      imageDrawable.state map {
        case ImageAssetDrawable.State.Failed(_) => assetBackground
        case _ => imageDrawable
      } orElse Signal.const[Drawable](imageDrawable)
  }

  bg.on(Threading.Ui) { setBackground }

  asset.disableAutowiring()

  assetActionButton.onClicked.filter(_ == DeliveryState.Complete) { _ =>
    asset.currentValue foreach { case (a, _) =>
      controller.openFile(a, showDialog = false)
    }
  }
}