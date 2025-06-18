/**
 * Copyright 2015 RECRUIT LIFESTYLE CO., LTD.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ext.draggablerotationalcubelibrary

import android.content.Context
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager

/**
 * フルスクリーンを監視するViewです。
 * http://stackoverflow.com/questions/18551135/receiving-hidden-status-bar-entering-a-full-screen-activity-event-on-a-service/19201933#19201933
 */
internal class FullscreenObserverView(
    context: Context?,
    /**
     * ScreenListener
     */
    private val mScreenChangedListener: ScreenChangedListener?
) : View(context), ViewTreeObserver.OnGlobalLayoutListener,
    View.OnSystemUiVisibilityChangeListener {
    /**
     * WindowManager.LayoutParams
     *
     * @return WindowManager.LayoutParams
     */
    // リスナーのセット

    // 幅1,高さ最大の透明なViewを用意して、レイアウトの変化を検知する
    /**
     * WindowManager.LayoutParams
     */
    val windowLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    /**
     * 最後の表示状態（onSystemUiVisibilityChangeが来ない場合があるので自分で保持）
     * ※来ない場合：ImmersiveMode→ステータスバーを触る→ステータスバーが消える
     */
    private var mLastUiVisibility: Int

    /**
     * WindowのRect
     */
    private val mWindowRect: Rect

    /**
     * コンストラクタ
     */
    init {
        windowLayoutParams.width = 1
        windowLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        windowLayoutParams.type = OVERLAY_TYPE
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        windowLayoutParams.format = PixelFormat.TRANSLUCENT

        mWindowRect = Rect()
        mLastUiVisibility = NO_LAST_VISIBILITY
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        viewTreeObserver.addOnGlobalLayoutListener(this)
        setOnSystemUiVisibilityChangeListener(this)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDetachedFromWindow() {
        // レイアウトの変化通知を削除
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            viewTreeObserver.removeOnGlobalLayoutListener(this)
        } else {
            viewTreeObserver.removeGlobalOnLayoutListener(this)
        }
        setOnSystemUiVisibilityChangeListener(null)
        super.onDetachedFromWindow()
    }

    /**
     * {@inheritDoc}
     */
    override fun onGlobalLayout() {
        // View（フル画面）のサイズを取得
        if (mScreenChangedListener != null) {
            getWindowVisibleDisplayFrame(mWindowRect)
            mScreenChangedListener.onScreenChanged(mWindowRect, mLastUiVisibility)
        }
    }

    /**
     * ナビゲーションバーに処理を行うアプリ（onGlobalLayoutのイベントが発生しない場合）で利用しています。
     * (Nexus5のカメラアプリなど)
     */
    override fun onSystemUiVisibilityChange(visibility: Int) {
        mLastUiVisibility = visibility
        // ナビゲーションバーの変化を受けて表示・非表示切替
        if (mScreenChangedListener != null) {
            getWindowVisibleDisplayFrame(mWindowRect)
            mScreenChangedListener.onScreenChanged(mWindowRect, visibility)
        }
    }

    companion object {
        /**
         * Constant that mLastUiVisibility does not exist.
         */
        const val NO_LAST_VISIBILITY: Int = -1

        /**
         * Overlay Type
         */
        private var OVERLAY_TYPE = 0

        init {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            } else {
                OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            }
        }
    }
}