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

import android.animation.TimeInterpolator
import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.PixelFormat
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.SystemClock
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.KeyCharacterMap
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.VelocityTracker
import android.view.ViewConfiguration
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.view.animation.OvershootInterpolator
import android.widget.FrameLayout
import androidx.annotation.IntDef
import androidx.core.view.ViewCompat
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.DynamicAnimation.OnAnimationUpdateListener
import androidx.dynamicanimation.animation.FlingAnimation
import androidx.dynamicanimation.animation.FloatValueHolder
import androidx.dynamicanimation.animation.SpringAnimation
import androidx.dynamicanimation.animation.SpringForce
import java.lang.ref.WeakReference
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

internal class FloatingView(context: Context) : FrameLayout(context),
    ViewTreeObserver.OnPreDrawListener {
    /**
     * AnimationState
     */
    @IntDef(*[STATE_NORMAL, STATE_INTERSECTING, STATE_FINISHING])
    @Retention(AnnotationRetention.SOURCE)
    internal annotation class AnimationState

    /**
     * WindowManager
     */
    private val mWindowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager

    /**
     * WindowManager.LayoutParamsを取得します。
     */
    /**
     * LayoutParams
     */
    val windowLayoutParams: WindowManager.LayoutParams = WindowManager.LayoutParams()

    /**
     * VelocityTracker
     */
    private var mVelocityTracker: VelocityTracker? = null

    /**
     * [ViewConfiguration]
     */
    private var mViewConfiguration: ViewConfiguration? = null

    /**
     * Minimum threshold required for movement(px)
     */
    private var mMoveThreshold = 0f

    /**
     * Maximum fling velocity
     */
    private var mMaximumFlingVelocity = 0f

    /**
     * Maximum x coordinate velocity
     */
    private var mMaximumXVelocity = 0f

    /**
     * Maximum x coordinate velocity
     */
    private var mMaximumYVelocity = 0f

    /**
     * Threshold to move when throwing
     */
    private var mThrowMoveThreshold = 0f

    /**
     * DisplayMetrics
     */
    private val mMetrics = DisplayMetrics()

    /**
     * 押下処理を通過しているかチェックするための時間
     */
    private var mTouchDownTime: Long = 0

    /**
     * スクリーン押下X座標(移動量判定用)
     */
    private var mScreenTouchDownX = 0f

    /**
     * スクリーン押下Y座標(移動量判定用)
     */
    private var mScreenTouchDownY = 0f

    /**
     * 一度移動を始めたフラグ
     */
    private var mIsMoveAccept = false

    /**
     * スクリーンのタッチX座標
     */
    private var mScreenTouchX = 0f

    /**
     * スクリーンのタッチY座標
     */
    private var mScreenTouchY = 0f

    /**
     * ローカルのタッチX座標
     */
    private var mLocalTouchX = 0f

    /**
     * ローカルのタッチY座標
     */
    private var mLocalTouchY = 0f

    /**
     * 初期表示のX座標
     */
    private var mInitX = 0

    /**
     * 初期表示のY座標
     */
    private var mInitY = 0

    /**
     * Initial animation running flag
     */
    private var mIsInitialAnimationRunning = false

    /**
     * 初期表示時にアニメーションするフラグ
     */
    private var mAnimateInitialMove = false

    /**
     * status bar's height
     */
    private val mBaseStatusBarHeight: Int

    /**
     * status bar's height(landscape)
     */
    private var mBaseStatusBarRotatedHeight = 0

    /**
     * Current status bar's height
     */
    private var mStatusBarHeight = 0

    /**
     * Navigation bar's height(portrait)
     */
    private var mBaseNavigationBarHeight = 0

    /**
     * Navigation bar's height
     * Placed bottom on the screen(tablet)
     * Or placed vertically on the screen(phone)
     */
    private var mBaseNavigationBarRotatedHeight = 0

    /**
     * Current Navigation bar's vertical size
     */
    private var mNavigationBarVerticalOffset = 0

    /**
     * Current Navigation bar's horizontal size
     */
    private var mNavigationBarHorizontalOffset = 0

    /**
     * Offset of touch X coordinate
     */
    private var mTouchXOffset = 0

    /**
     * Offset of touch Y coordinate
     */
    private var mTouchYOffset = 0

    /**
     * 左・右端に寄せるアニメーション
     */
    private var mMoveEdgeAnimator: ValueAnimator? = null

    /**
     * Interpolator
     */
    private val mMoveEdgeInterpolator: TimeInterpolator

    /**
     * 移動限界を表すRect
     */
    private val mMoveLimitRect: Rect

    /**
     * 表示位置（画面端）の限界を表すRect
     */
    private val mPositionLimitRect: Rect

    /**
     * ドラッグ可能フラグ
     */
    private var mIsDraggable = false

    /**
     * Viewの形を取得します。
     *
     * @return SHAPE_CIRCLE or SHAPE_RECTANGLE
     */
    /**
     * Viewの形を表す定数
     *
     * @param shape SHAPE_CIRCLE or SHAPE_RECTANGLE
     */
    /**
     * 形を表す係数
     */
    var shape: Float = 0f

    /**
     * FloatingViewのアニメーションを行うハンドラ
     */
    private val mAnimationHandler: FloatingAnimationHandler

    /**
     * 長押しを判定するためのハンドラ
     */
    private val mLongPressHandler: LongPressHandler

    /**
     * 画面端をオーバーするマージン
     */
    private var mOverMargin = 0

    /**
     * OnTouchListener
     */
    private var mOnTouchListener: OnTouchListener? = null

    /**
     * 長押し状態の場合
     */
    private var mIsLongPressed = false

    /**
     * 移動方向
     */
    private var mMoveDirection: Int

    /**
     * Use dynamic physics-based animations or not
     */
    private var mUsePhysics: Boolean

    /**
     * If true, it's a tablet. If false, it's a phone
     */
    private val mIsTablet: Boolean

    /**
     * Surface.ROTATION_XXX
     */
    private var mRotation: Int

    /**
     * Cutout safe inset rect(Same as FloatingViewManager's mSafeInsetRect)
     */
    private val mSafeInsetRect: Rect

    /**
     * コンストラクタ
     *
     * @param context [Context]
     */
    init {
        mWindowManager.defaultDisplay.getMetrics(mMetrics)
        windowLayoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
        windowLayoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        windowLayoutParams.type = OVERLAY_TYPE
        windowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        windowLayoutParams.format = PixelFormat.TRANSLUCENT
        // 左下の座標を0とする
        windowLayoutParams.gravity = Gravity.LEFT or Gravity.BOTTOM
        mAnimationHandler = FloatingAnimationHandler(this)
        mLongPressHandler = LongPressHandler(this)
        mMoveEdgeInterpolator = OvershootInterpolator(MOVE_TO_EDGE_OVERSHOOT_TENSION)
        mMoveDirection = FloatingViewManager.MOVE_DIRECTION_DEFAULT
        mUsePhysics = false
        val resources = context.resources
        mIsTablet =
            (resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
        mRotation = mWindowManager.defaultDisplay.rotation

        mMoveLimitRect = Rect()
        mPositionLimitRect = Rect()
        mSafeInsetRect = Rect()

        // ステータスバーの高さを取得
        mBaseStatusBarHeight = getSystemUiDimensionPixelSize(resources, "status_bar_height")
        // Check landscape resource id
        val statusBarLandscapeResId =
            resources.getIdentifier("status_bar_height_landscape", "dimen", "android")
        mBaseStatusBarRotatedHeight = if (statusBarLandscapeResId > 0) {
            getSystemUiDimensionPixelSize(
                resources,
                "status_bar_height_landscape"
            )
        } else {
            mBaseStatusBarHeight
        }

        // Init physics-based animation properties
        updateViewConfiguration()

        // Detect NavigationBar
        if (hasSoftNavigationBar()) {
            mBaseNavigationBarHeight =
                getSystemUiDimensionPixelSize(resources, "navigation_bar_height")
            val resName =
                if (mIsTablet) "navigation_bar_height_landscape" else "navigation_bar_width"
            mBaseNavigationBarRotatedHeight = getSystemUiDimensionPixelSize(resources, resName)
        } else {
            mBaseNavigationBarHeight = 0
            mBaseNavigationBarRotatedHeight = 0
        }

        // 初回描画処理用
        viewTreeObserver.addOnPreDrawListener(this)
    }

    /**
     * Check if there is a software navigation bar(including the navigation bar in the screen).
     *
     * @return True if there is a software navigation bar
     */
    private fun hasSoftNavigationBar(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val realDisplayMetrics = DisplayMetrics()
            mWindowManager.defaultDisplay.getRealMetrics(realDisplayMetrics)
            return realDisplayMetrics.heightPixels > mMetrics.heightPixels || realDisplayMetrics.widthPixels > mMetrics.widthPixels
        }

        // old device check flow
        // Navigation bar exists (config_showNavigationBar is true, or both the menu key and the back key are not exists)
        val context = context
        val resources = context.resources
        val hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey()
        val hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK)
        val showNavigationBarResId =
            resources.getIdentifier("config_showNavigationBar", "bool", "android")
        val hasNavigationBarConfig =
            showNavigationBarResId != 0 && resources.getBoolean(showNavigationBarResId)
        return hasNavigationBarConfig || (!hasMenuKey && !hasBackKey)
    }

    /**
     * 表示位置を決定します。
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        refreshLimitRect()
    }

    /**
     * 画面回転時にレイアウトの調整をします。
     */
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        updateViewConfiguration()
        refreshLimitRect()
    }

    /**
     * 初回描画時の座標設定を行います。
     */
    override fun onPreDraw(): Boolean {
        viewTreeObserver.removeOnPreDrawListener(this)
        // X座標に初期値が設定されていればデフォルト値を入れる(マージンは考慮しない)
        if (mInitX == DEFAULT_X) {
            mInitX = 0
        }
        // Y座標に初期値が設定されていればデフォルト値を入れる
        if (mInitY == DEFAULT_Y) {
            mInitY = mMetrics.heightPixels - mStatusBarHeight - measuredHeight
        }

        // 初期位置を設定
        windowLayoutParams.x = mInitX
        windowLayoutParams.y = mInitY

        // 画面端に移動しない場合は指定座標に移動
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NONE) {
            moveTo(mInitX, mInitY, mInitX, mInitY, false)
        } else {
            mIsInitialAnimationRunning = true
            // 初期位置から画面端に移動
            moveToEdge(mInitX, mInitY, mAnimateInitialMove)
        }
        mIsDraggable = true
        updateViewLayout()
        return true
    }

    /**
     * Called when the layout of the system has changed.
     *
     * @param isHideStatusBar     If true, the status bar is hidden
     * @param isHideNavigationBar If true, the navigation bar is hidden
     * @param isPortrait          If true, the device orientation is portrait
     * @param windowRect          [Rect] of system window
     */
    fun onUpdateSystemLayout(
        isHideStatusBar: Boolean,
        isHideNavigationBar: Boolean,
        isPortrait: Boolean,
        windowRect: Rect
    ) {
        // status bar
        updateStatusBarHeight(isHideStatusBar, isPortrait)
        // touch X offset(support Cutout)
        updateTouchXOffset(isHideNavigationBar, windowRect.left)
        // touch Y offset(support Cutout)
        mTouchYOffset = if (isPortrait) mSafeInsetRect.top else 0
        // navigation bar
        updateNavigationBarOffset(isHideNavigationBar, isPortrait, windowRect)
        refreshLimitRect()
    }

    /**
     * Update height of StatusBar.
     *
     * @param isHideStatusBar If true, the status bar is hidden
     * @param isPortrait      If true, the device orientation is portrait
     */
    private fun updateStatusBarHeight(isHideStatusBar: Boolean, isPortrait: Boolean) {
        if (isHideStatusBar) {
            // 1.(No Cutout)No StatusBar(=0)
            // 2.(Has Cutout)StatusBar is not included in mMetrics.heightPixels (=0)
            mStatusBarHeight = 0
            return
        }

        // Has Cutout
        val hasTopCutout = mSafeInsetRect.top != 0
        if (hasTopCutout) {
            mStatusBarHeight = if (isPortrait) {
                0
            } else {
                mBaseStatusBarRotatedHeight
            }
            return
        }

        // No cutout
        mStatusBarHeight = if (isPortrait) {
            mBaseStatusBarHeight
        } else {
            mBaseStatusBarRotatedHeight
        }
    }

    /**
     * Update of touch X coordinate
     *
     * @param isHideNavigationBar If true, the navigation bar is hidden
     * @param windowLeftOffset    Left side offset of device display
     */
    private fun updateTouchXOffset(isHideNavigationBar: Boolean, windowLeftOffset: Int) {
        val hasBottomCutout = mSafeInsetRect.bottom != 0
        if (hasBottomCutout) {
            mTouchXOffset = windowLeftOffset
            return
        }

        // No cutout
        // touch X offset(navigation bar is displayed and it is on the left side of the device)
        mTouchXOffset =
            if (!isHideNavigationBar && windowLeftOffset > 0) mBaseNavigationBarRotatedHeight else 0
    }

    /**
     * Update offset of NavigationBar.
     *
     * @param isHideNavigationBar If true, the navigation bar is hidden
     * @param isPortrait          If true, the device orientation is portrait
     * @param windowRect          [Rect] of system window
     */
    private fun updateNavigationBarOffset(
        isHideNavigationBar: Boolean,
        isPortrait: Boolean,
        windowRect: Rect
    ) {
        var currentNavigationBarHeight = 0
        var currentNavigationBarWidth = 0
        var navigationBarVerticalDiff = 0
        val hasSoftNavigationBar = hasSoftNavigationBar()
        // auto hide navigation bar(Galaxy S8, S9 and so on.)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            val realDisplayMetrics = DisplayMetrics()
            mWindowManager.defaultDisplay.getRealMetrics(realDisplayMetrics)
            currentNavigationBarHeight = realDisplayMetrics.heightPixels - windowRect.bottom
            currentNavigationBarWidth = realDisplayMetrics.widthPixels - mMetrics.widthPixels
            navigationBarVerticalDiff = mBaseNavigationBarHeight - currentNavigationBarHeight
        }

        if (!isHideNavigationBar) {
            // auto hide navigation bar
            // 他デバイスとの矛盾をもとに推測する
            // 1.デバイスに組み込まれたナビゲーションバー（mBaseNavigationBarHeight == 0）はシステムの状態によって高さに差が発生しない
            // 2.デバイスに組み込まれたナビゲーションバー(!hasSoftNavigationBar)は意図的にBaseを0にしているので、矛盾している
            mNavigationBarVerticalOffset =
                if (navigationBarVerticalDiff != 0 && mBaseNavigationBarHeight == 0 ||
                    !hasSoftNavigationBar && mBaseNavigationBarHeight != 0
                ) {
                    if (hasSoftNavigationBar) {
                        // 1.auto hide mode -> show mode
                        // 2.show mode -> auto hide mode -> home
                        0
                    } else {
                        // show mode -> home
                        -currentNavigationBarHeight
                    }
                } else {
                    // normal device
                    0
                }

            mNavigationBarHorizontalOffset = 0
            return
        }

        // If the portrait, is displayed at the bottom of the screen
        if (isPortrait) {
            // auto hide navigation bar
            mNavigationBarVerticalOffset =
                if (!hasSoftNavigationBar && mBaseNavigationBarHeight != 0) {
                    0
                } else {
                    mBaseNavigationBarHeight
                }
            mNavigationBarHorizontalOffset = 0
            return
        }

        // If it is a Tablet, it will appear at the bottom of the screen.
        // If it is Phone, it will appear on the side of the screen
        if (mIsTablet) {
            mNavigationBarVerticalOffset = mBaseNavigationBarRotatedHeight
            mNavigationBarHorizontalOffset = 0
        } else {
            mNavigationBarVerticalOffset = 0
            // auto hide navigation bar
            // 他デバイスとの矛盾をもとに推測する
            // 1.デバイスに組み込まれたナビゲーションバー(!hasSoftNavigationBar)は、意図的にBaseを0にしているので、矛盾している
            mNavigationBarHorizontalOffset =
                if (!hasSoftNavigationBar && mBaseNavigationBarRotatedHeight != 0) {
                    0
                } else if (hasSoftNavigationBar && mBaseNavigationBarRotatedHeight == 0) {
                    // 2.ソフトナビゲーションバーの場合、Baseが設定されるため矛盾している
                    currentNavigationBarWidth
                } else {
                    mBaseNavigationBarRotatedHeight
                }
        }
    }

    /**
     * Update [ViewConfiguration]
     */
    private fun updateViewConfiguration() {
        mViewConfiguration = ViewConfiguration.get(context)
        mMoveThreshold = mViewConfiguration?.scaledTouchSlop?.toFloat()!!
        mMaximumFlingVelocity = mViewConfiguration?.scaledMaximumFlingVelocity?.toFloat()!!
        mMaximumXVelocity = mMaximumFlingVelocity / MAX_X_VELOCITY_SCALE_DOWN_VALUE
        mMaximumYVelocity = mMaximumFlingVelocity / MAX_Y_VELOCITY_SCALE_DOWN_VALUE
        mThrowMoveThreshold = mMaximumFlingVelocity / THROW_THRESHOLD_SCALE_DOWN_VALUE
    }

    /**
     * Update the PositionLimitRect and MoveLimitRect according to the screen size change.
     */
    private fun refreshLimitRect() {
        cancelAnimation()

        // 前の画面座標を保存
        val oldPositionLimitWidth = mPositionLimitRect.width()
        val oldPositionLimitHeight = mPositionLimitRect.height()

        // 新しい座標情報に切替
        mWindowManager.defaultDisplay.getMetrics(mMetrics)
        val width = measuredWidth
        val height = measuredHeight
        val newScreenWidth = mMetrics.widthPixels
        val newScreenHeight = mMetrics.heightPixels

        // 移動範囲の設定
        mMoveLimitRect[-width, -height * 2, newScreenWidth + width + mNavigationBarHorizontalOffset] =
            newScreenHeight + height + mNavigationBarVerticalOffset
        mPositionLimitRect[-mOverMargin, 0, newScreenWidth - width + mOverMargin + mNavigationBarHorizontalOffset] =
            newScreenHeight - mStatusBarHeight - height + mNavigationBarVerticalOffset

        // Initial animation stop when the device rotates
        val newRotation = mWindowManager.defaultDisplay.rotation
        if (mAnimateInitialMove && mRotation != newRotation) {
            mIsInitialAnimationRunning = false
        }

        // When animation is running and the device is not rotating
        if (mIsInitialAnimationRunning && mRotation == newRotation) {
            moveToEdge(windowLayoutParams.x, windowLayoutParams.y, true)
        } else {
            // If there is a screen change during the operation, move to the appropriate position
            if (mIsMoveAccept) {
                moveToEdge(windowLayoutParams.x, windowLayoutParams.y, false)
            } else {
                val newX =
                    (windowLayoutParams.x * mPositionLimitRect.width() / oldPositionLimitWidth.toFloat() + 0.5f).toInt()
                val goalPositionX = min(
                    max(mPositionLimitRect.left.toDouble(), newX.toDouble()),
                    mPositionLimitRect.right.toDouble()
                ).toInt()
                val newY =
                    (windowLayoutParams.y * mPositionLimitRect.height() / oldPositionLimitHeight.toFloat() + 0.5f).toInt()
                val goalPositionY = min(
                    max(mPositionLimitRect.top.toDouble(), newY.toDouble()),
                    mPositionLimitRect.bottom.toDouble()
                ).toInt()
                moveTo(
                    windowLayoutParams.x,
                    windowLayoutParams.y,
                    goalPositionX,
                    goalPositionY,
                    false
                )
            }
        }
        mRotation = newRotation
    }

    /**
     * {@inheritDoc}
     */
    override fun onDetachedFromWindow() {
        if (mMoveEdgeAnimator != null) {
            mMoveEdgeAnimator!!.removeAllUpdateListeners()
        }
        super.onDetachedFromWindow()
    }

    /**
     * {@inheritDoc}
     */
    override fun onInterceptTouchEvent(event: MotionEvent): Boolean {
        return dispatchTouchEvent(event, true)
    }

    /**
     * {@inheritDoc}
     */
    override fun onTouchEvent(event: MotionEvent): Boolean {
        return dispatchTouchEvent(event, false)
    }

    /**
     * Use onInterceptTouchEvent to detect FloatView move then onTouchEvent consume event
     */
    private fun dispatchTouchEvent(event: MotionEvent, isOnInterceptTouchEvent: Boolean): Boolean {
        // Viewが表示されていなければ何もしない
        if (visibility != VISIBLE) {
            return false
        }

        // タッチ不能な場合は何もしない
        if (!mIsDraggable) {
            return false
        }

        // Block while initial display animation is running
        if (mIsInitialAnimationRunning) {
            return false
        }

        // 現在位置のキャッシュ
        mScreenTouchX = event.rawX
        mScreenTouchY = event.rawY
        val action = event.action
        var isWaitForMoveToEdge = false
        // 押下
        if (action == MotionEvent.ACTION_DOWN) {
            // アニメーションのキャンセル
            cancelAnimation()
            mScreenTouchDownX = mScreenTouchX
            mScreenTouchDownY = mScreenTouchY
            mLocalTouchX = event.x
            mLocalTouchY = event.y
            mIsMoveAccept = false
            setScale(SCALE_PRESSED)

            if (mVelocityTracker == null) {
                // Retrieve a new VelocityTracker object to watch the velocity of a motion.
                mVelocityTracker = VelocityTracker.obtain()
            } else {
                // Reset the velocity tracker back to its initial state.
                mVelocityTracker!!.clear()
            }

            // タッチトラッキングアニメーションの開始
            mAnimationHandler.updateTouchPosition(xByTouch.toFloat(), yByTouch.toFloat())
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH)
            mAnimationHandler.sendAnimationMessage(FloatingAnimationHandler.ANIMATION_IN_TOUCH)
            // 長押し判定の開始
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED)
            mLongPressHandler.sendEmptyMessageDelayed(
                LongPressHandler.LONG_PRESSED,
                LONG_PRESS_TIMEOUT.toLong()
            )
            // 押下処理の通過判定のための時間保持
            // mIsDraggableやgetVisibility()のフラグが押下後に変更された場合にMOVE等を処理させないようにするため
            mTouchDownTime = event.downTime
            // compute offset and restore
            addMovement(event)
            mIsInitialAnimationRunning = false
        } else if (action == MotionEvent.ACTION_MOVE) {
            // 移動判定の場合は長押しの解除
            if (mIsMoveAccept) {
                mIsLongPressed = false
                mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED)
            }
            // 押下処理が行われていない場合は処理しない
            if (mTouchDownTime != event.downTime) {
                return !isOnInterceptTouchEvent
            }
            // 移動受付状態でない、かつX,Y軸ともにしきい値よりも小さい場合
            if (!mIsMoveAccept && abs((mScreenTouchX - mScreenTouchDownX).toDouble()) < mMoveThreshold && abs(
                    (mScreenTouchY - mScreenTouchDownY).toDouble()
                ) < mMoveThreshold
            ) {
                return !isOnInterceptTouchEvent
            }
            mIsMoveAccept = true
            mAnimationHandler.updateTouchPosition(xByTouch.toFloat(), yByTouch.toFloat())
            // compute offset and restore
            addMovement(event)
        } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            // compute velocity tracker
            if (mVelocityTracker != null) {
                mVelocityTracker!!.computeCurrentVelocity(CURRENT_VELOCITY_UNITS)
            }

            // 判定のため長押しの状態を一時的に保持
            val tmpIsLongPressed = mIsLongPressed
            // 長押しの解除
            mIsLongPressed = false
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED)
            // 押下処理が行われていない場合は処理しない
            if (mTouchDownTime != event.downTime) {
                return true
            }
            // アニメーションの削除
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH)
            // 拡大率をもとに戻す
            setScale(SCALE_NORMAL)

            // destroy VelocityTracker (#103)
            if (!mIsMoveAccept && mVelocityTracker != null) {
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }

            // When ACTION_UP is done (when not pressed or moved)
            if (action == MotionEvent.ACTION_UP && !tmpIsLongPressed && !mIsMoveAccept) {
            } else {
                // Make a move after checking whether it is finished or not
                isWaitForMoveToEdge = true
            }
        }

        // タッチリスナを通知
        if (mOnTouchListener != null) {
            mOnTouchListener!!.onTouch(this, event)
        }

        // Lazy execution of moveToEdge
        if (isWaitForMoveToEdge && mAnimationHandler.state != STATE_FINISHING) {
            // include device rotation
            moveToEdge(true)
            if (mVelocityTracker != null) {
                mVelocityTracker!!.recycle()
                mVelocityTracker = null
            }
        }

        return !isOnInterceptTouchEvent || mIsMoveAccept
    }

    /**
     * Call addMovement and restore MotionEvent coordinate
     *
     * @param event [MotionEvent]
     */
    private fun addMovement(event: MotionEvent) {
        val deltaX = event.rawX - event.x
        val deltaY = event.rawY - event.y
        event.offsetLocation(deltaX, deltaY)
        mVelocityTracker!!.addMovement(event)
        event.offsetLocation(-deltaX, -deltaY)
    }

    /**
     * 長押しされた場合の処理です。
     */
    private fun onLongClick() {
        mIsLongPressed = true
        // 長押し処理
        val size = childCount
        for (i in 0 until size) {
            getChildAt(i).performLongClick()
        }
    }

    /**
     * 画面から消す際の処理を表します。
     */
    override fun setVisibility(visibility: Int) {
        // 画面表示時
        if (visibility != VISIBLE) {
            // 画面から消す時は長押しをキャンセルし、画面端に強制的に移動します。
            cancelLongPress()
            setScale(SCALE_NORMAL)
            if (mIsMoveAccept) {
                moveToEdge(false)
            }
            mAnimationHandler.removeMessages(FloatingAnimationHandler.ANIMATION_IN_TOUCH)
            mLongPressHandler.removeMessages(LongPressHandler.LONG_PRESSED)
        }
        super.setVisibility(visibility)
    }

    /**
     * {@inheritDoc}
     */
    override fun setOnTouchListener(listener: OnTouchListener) {
        mOnTouchListener = listener
    }

    /**
     * 左右の端に移動します。
     *
     * @param withAnimation アニメーションを行う場合はtrue.行わない場合はfalse
     */
    private fun moveToEdge(withAnimation: Boolean) {
        val currentX = xByTouch
        val currentY = yByTouch
        moveToEdge(currentX, currentY, withAnimation)
    }

    /**
     * 始点を指定して左右の端に移動します。
     *
     * @param startX        X座標の初期値
     * @param startY        Y座標の初期値
     * @param withAnimation アニメーションを行う場合はtrue.行わない場合はfalse
     */
    private fun moveToEdge(startX: Int, startY: Int, withAnimation: Boolean) {
        // 指定座標に移動
        val goalPositionX = getGoalPositionX(startX, startY)
        val goalPositionY = getGoalPositionY(startX, startY)
        moveTo(startX, startY, goalPositionX, goalPositionY, withAnimation)
    }

    /**
     * 指定座標に移動します。<br></br>
     * 画面端の座標を超える場合は、自動的に画面端に移動します。
     *
     * @param currentX      現在のX座標（アニメーションの始点用に使用）
     * @param currentY      現在のY座標（アニメーションの始点用に使用）
     * @param goalPositionX 移動先のX座標
     * @param goalPositionY 移動先のY座標
     * @param withAnimation アニメーションを行う場合はtrue.行わない場合はfalse
     */
    private fun moveTo(
        currentX: Int,
        currentY: Int,
        goalPositionX: Int,
        goalPositionY: Int,
        withAnimation: Boolean
    ) {
        // 画面端からはみ出さないように調整
        var goalPositionX = goalPositionX
        var goalPositionY = goalPositionY
        goalPositionX = min(
            max(mPositionLimitRect.left.toDouble(), goalPositionX.toDouble()),
            mPositionLimitRect.right.toDouble()
        ).toInt()
        goalPositionY = min(
            max(mPositionLimitRect.top.toDouble(), goalPositionY.toDouble()),
            mPositionLimitRect.bottom.toDouble()
        ).toInt()
        // アニメーションを行う場合
        if (withAnimation) {
            // Use physics animation
            val usePhysicsAnimation =
                mUsePhysics && mVelocityTracker != null && mMoveDirection != FloatingViewManager.MOVE_DIRECTION_NEAREST
            if (usePhysicsAnimation) {
                startPhysicsAnimation(goalPositionX, currentY)
            } else {
                startObjectAnimation(currentX, currentY, goalPositionX, goalPositionY)
            }
        } else {
            // 位置が変化した時のみ更新
            if (windowLayoutParams.x != goalPositionX || windowLayoutParams.y != goalPositionY) {
                windowLayoutParams.x = goalPositionX
                windowLayoutParams.y = goalPositionY
                updateViewLayout()
            }
        }
        // タッチ座標を初期化
        mLocalTouchX = 0f
        mLocalTouchY = 0f
        mScreenTouchDownX = 0f
        mScreenTouchDownY = 0f
        mIsMoveAccept = false
    }

    /**
     * Start Physics-based animation
     *
     * @param goalPositionX goal position X coordinate
     * @param currentY      current Y coordinate
     */
    private fun startPhysicsAnimation(goalPositionX: Int, currentY: Int) {
        // start X coordinate animation
        val containsLimitRectWidth =
            windowLayoutParams.x < mPositionLimitRect.right && windowLayoutParams.x > mPositionLimitRect.left
        // If MOVE_DIRECTION_NONE, play fling animation
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NONE && containsLimitRectWidth) {
            val velocityX = min(
                max(
                    mVelocityTracker!!.xVelocity.toDouble(),
                    -mMaximumXVelocity.toDouble()
                ), mMaximumXVelocity.toDouble()
            ).toFloat()
            startFlingAnimationX(velocityX)
        } else {
            startSpringAnimationX(goalPositionX)
        }

        // start Y coordinate animation
        val containsLimitRectHeight =
            windowLayoutParams.y < mPositionLimitRect.bottom && windowLayoutParams.y > mPositionLimitRect.top
        val velocityY = (-min(
            max(
                mVelocityTracker!!.yVelocity.toDouble(),
                -mMaximumYVelocity.toDouble()
            ), mMaximumYVelocity.toDouble()
        )).toFloat()
        if (containsLimitRectHeight) {
            startFlingAnimationY(velocityY)
        } else {
            startSpringAnimationY(currentY, velocityY)
        }
    }

    /**
     * Start object animation
     *
     * @param currentX      current X coordinate
     * @param currentY      current Y coordinate
     * @param goalPositionX goal position X coordinate
     * @param goalPositionY goal position Y coordinate
     */
    private fun startObjectAnimation(
        currentX: Int,
        currentY: Int,
        goalPositionX: Int,
        goalPositionY: Int
    ) {
        if (goalPositionX == currentX) {
            //to move only y coord
            mMoveEdgeAnimator = ValueAnimator.ofInt(currentY, goalPositionY)
            mMoveEdgeAnimator?.addUpdateListener { animation ->
                windowLayoutParams.y = (animation.animatedValue as Int)
                updateViewLayout()
                updateInitAnimation(animation)
            }
        } else {
            // To move only x coord (to left or right)
            windowLayoutParams.y = goalPositionY
            mMoveEdgeAnimator = ValueAnimator.ofInt(currentX, goalPositionX)
            mMoveEdgeAnimator?.addUpdateListener { animation ->
                windowLayoutParams.x = (animation.animatedValue as Int)
                updateViewLayout()
                updateInitAnimation(animation)
            }
        }
        // X軸のアニメーション設定
        mMoveEdgeAnimator?.setDuration(MOVE_TO_EDGE_DURATION)
        mMoveEdgeAnimator?.setInterpolator(mMoveEdgeInterpolator)
        mMoveEdgeAnimator?.start()
    }

    /**
     * Start spring animation(X coordinate)
     *
     * @param goalPositionX goal position X coordinate
     */
    private fun startSpringAnimationX(goalPositionX: Int) {
        // springX
        val springX = SpringForce(goalPositionX.toFloat())
        springX.setDampingRatio(ANIMATION_SPRING_X_DAMPING_RATIO)
        springX.setStiffness(ANIMATION_SPRING_X_STIFFNESS)
        // springAnimation
        val springAnimationX = SpringAnimation(FloatValueHolder())
        springAnimationX.setStartVelocity(mVelocityTracker!!.xVelocity)
        springAnimationX.setStartValue(windowLayoutParams.x.toFloat())
        springAnimationX.setSpring(springX)
        springAnimationX.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
        springAnimationX.addUpdateListener(OnAnimationUpdateListener { animation, value, velocity ->
            val x = Math.round(value)
            // Not moving, or the touch operation is continuing
            if (windowLayoutParams.x == x || mVelocityTracker != null) {
                return@OnAnimationUpdateListener
            }
            // update x coordinate
            windowLayoutParams.x = x
            updateViewLayout()
        })
        springAnimationX.start()
    }

    /**
     * Start spring animation(Y coordinate)
     *
     * @param currentY  current Y coordinate
     * @param velocityY velocity Y coordinate
     */
    private fun startSpringAnimationY(currentY: Int, velocityY: Float) {
        // Create SpringForce
        val springY =
            SpringForce((if (currentY < mMetrics.heightPixels / 2) mPositionLimitRect.top else mPositionLimitRect.bottom).toFloat())
        springY.setDampingRatio(SpringForce.DAMPING_RATIO_LOW_BOUNCY)
        springY.setStiffness(SpringForce.STIFFNESS_LOW)

        // Create SpringAnimation
        val springAnimationY = SpringAnimation(FloatValueHolder())
        springAnimationY.setStartVelocity(velocityY)
        springAnimationY.setStartValue(windowLayoutParams.y.toFloat())
        springAnimationY.setSpring(springY)
        springAnimationY.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
        springAnimationY.addUpdateListener(OnAnimationUpdateListener { animation, value, velocity ->
            val y = Math.round(value)
            // Not moving, or the touch operation is continuing
            if (windowLayoutParams.y == y || mVelocityTracker != null) {
                return@OnAnimationUpdateListener
            }
            // update y coordinate
            windowLayoutParams.y = y
            updateViewLayout()
        })
        springAnimationY.start()
    }

    /**
     * Start fling animation(X coordinate)
     *
     * @param velocityX velocity X coordinate
     */
    private fun startFlingAnimationX(velocityX: Float) {
        val flingAnimationX = FlingAnimation(FloatValueHolder())
        flingAnimationX.setStartVelocity(velocityX)
        flingAnimationX.setMaxValue(mPositionLimitRect.right.toFloat())
        flingAnimationX.setMinValue(mPositionLimitRect.left.toFloat())
        flingAnimationX.setStartValue(windowLayoutParams.x.toFloat())
        flingAnimationX.setFriction(ANIMATION_FLING_X_FRICTION)
        flingAnimationX.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
        flingAnimationX.addUpdateListener(OnAnimationUpdateListener { animation, value, velocity ->
            val x = Math.round(value)
            // Not moving, or the touch operation is continuing
            if (windowLayoutParams.x == x || mVelocityTracker != null) {
                return@OnAnimationUpdateListener
            }
            // update y coordinate
            windowLayoutParams.x = x
            updateViewLayout()
        })
        flingAnimationX.start()
    }

    /**
     * Start fling animation(Y coordinate)
     *
     * @param velocityY velocity Y coordinate
     */
    private fun startFlingAnimationY(velocityY: Float) {
        val flingAnimationY = FlingAnimation(FloatValueHolder())
        flingAnimationY.setStartVelocity(velocityY)
        flingAnimationY.setMaxValue(mPositionLimitRect.bottom.toFloat())
        flingAnimationY.setMinValue(mPositionLimitRect.top.toFloat())
        flingAnimationY.setStartValue(windowLayoutParams.y.toFloat())
        flingAnimationY.setFriction(ANIMATION_FLING_Y_FRICTION)
        flingAnimationY.setMinimumVisibleChange(DynamicAnimation.MIN_VISIBLE_CHANGE_PIXELS)
        flingAnimationY.addUpdateListener(OnAnimationUpdateListener { animation, value, velocity ->
            val y = Math.round(value)
            // Not moving, or the touch operation is continuing
            if (windowLayoutParams.y == y || mVelocityTracker != null) {
                return@OnAnimationUpdateListener
            }
            // update y coordinate
            windowLayoutParams.y = y
            updateViewLayout()
        })
        flingAnimationY.start()
    }

    /**
     * Check if it is attached to the Window and call WindowManager.updateLayout()
     */
    private fun updateViewLayout() {
        if (!ViewCompat.isAttachedToWindow(this)) {
            return
        }
        mWindowManager.updateViewLayout(this, windowLayoutParams)
    }

    /**
     * Update animation initialization flag
     *
     * @param animation [ValueAnimator]
     */
    private fun updateInitAnimation(animation: ValueAnimator) {
        if (mAnimateInitialMove && animation.duration <= animation.currentPlayTime) {
            mIsInitialAnimationRunning = false
        }
    }

    /**
     * Get the final point of movement (X coordinate)
     *
     * @param startX Initial value of X coordinate
     * @param startY Initial value of Y coordinate
     * @return End point of X coordinate
     */
    private fun getGoalPositionX(startX: Int, startY: Int): Int {
        var goalPositionX = startX

        // Move to left or right edges
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_DEFAULT) {
            val isMoveRightEdge = startX > (mMetrics.widthPixels - width) / 2
            goalPositionX =
                if (isMoveRightEdge) mPositionLimitRect.right else mPositionLimitRect.left
        } else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_LEFT) {
            goalPositionX = mPositionLimitRect.left
        } else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_RIGHT) {
            goalPositionX = mPositionLimitRect.right
        } else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NEAREST) {
            val distLeftRight =
                min(startX.toDouble(), (mPositionLimitRect.width() - startX).toDouble())
                    .toInt()
            val distTopBottom =
                min(startY.toDouble(), (mPositionLimitRect.height() - startY).toDouble())
                    .toInt()
            if (distLeftRight < distTopBottom) {
                val isMoveRightEdge = startX > (mMetrics.widthPixels - width) / 2
                goalPositionX =
                    if (isMoveRightEdge) mPositionLimitRect.right else mPositionLimitRect.left
            }
        } else if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_THROWN) {
            if (mVelocityTracker != null && mVelocityTracker!!.xVelocity > mThrowMoveThreshold) {
                goalPositionX = mPositionLimitRect.right
            } else if (mVelocityTracker != null && mVelocityTracker!!.xVelocity < -mThrowMoveThreshold) {
                goalPositionX = mPositionLimitRect.left
            } else {
                val isMoveRightEdge = startX > (mMetrics.widthPixels - width) / 2
                goalPositionX =
                    if (isMoveRightEdge) mPositionLimitRect.right else mPositionLimitRect.left
            }
        }

        return goalPositionX
    }

    /**
     * Get the final point of movement (Y coordinate)
     *
     * @param startX Initial value of X coordinate
     * @param startY Initial value of Y coordinate
     * @return End point of Y coordinate
     */
    private fun getGoalPositionY(startX: Int, startY: Int): Int {
        var goalPositionY = startY

        // Move to top/bottom/left/right edges
        if (mMoveDirection == FloatingViewManager.MOVE_DIRECTION_NEAREST) {
            val distLeftRight =
                min(startX.toDouble(), (mPositionLimitRect.width() - startX).toDouble())
                    .toInt()
            val distTopBottom =
                min(startY.toDouble(), (mPositionLimitRect.height() - startY).toDouble())
                    .toInt()
            if (distLeftRight >= distTopBottom) {
                val isMoveTopEdge = startY < (mMetrics.heightPixels - height) / 2
                goalPositionY =
                    if (isMoveTopEdge) mPositionLimitRect.top else mPositionLimitRect.bottom
            }
        }

        return goalPositionY
    }

    /**
     * アニメーションをキャンセルします。
     */
    private fun cancelAnimation() {
        if (mMoveEdgeAnimator != null && mMoveEdgeAnimator!!.isStarted) {
            mMoveEdgeAnimator!!.cancel()
            mMoveEdgeAnimator = null
        }
    }

    /**
     * 拡大・縮小を行います。
     *
     * @param newScale 設定する拡大率
     */
    private fun setScale(newScale: Float) {
        // INFO:childにscaleを設定しないと拡大率が変わらない現象に対処するための修正
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            val childCount = childCount
            for (i in 0 until childCount) {
                val targetView = getChildAt(i)
                targetView.scaleX = newScale
                targetView.scaleY = newScale
            }
        } else {
            scaleX = newScale
            scaleY = newScale
        }
    }

    /**
     * ドラッグ可能フラグ
     *
     * @param isDraggable ドラッグ可能にする場合はtrue
     */
    fun setDraggable(isDraggable: Boolean) {
        mIsDraggable = isDraggable
    }

    /**
     * 画面端をオーバーするマージンです。
     *
     * @param margin マージン
     */
    fun setOverMargin(margin: Int) {
        mOverMargin = margin
    }

    /**
     * 移動方向を設定します。
     *
     * @param moveDirection 移動方向
     */
    fun setMoveDirection(moveDirection: Int) {
        mMoveDirection = moveDirection
    }

    /**
     * Use dynamic physics-based animations or not
     * Warning: Can not be used before API 16
     *
     * @param usePhysics Setting this to false will revert to using a ValueAnimator (default is true)
     */
    fun usePhysics(usePhysics: Boolean) {
        mUsePhysics = usePhysics && Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
    }

    /**
     * 初期座標を設定します。
     *
     * @param x FloatingViewの初期X座標
     * @param y FloatingViewの初期Y座標
     */
    fun setInitCoords(x: Int, y: Int) {
        mInitX = x
        mInitY = y
    }

    /**
     * 初期表示時にアニメーションするフラグを設定します。
     *
     * @param animateInitialMove 初期表示時にアニメーションする場合はtrue
     */
    fun setAnimateInitialMove(animateInitialMove: Boolean) {
        mAnimateInitialMove = animateInitialMove
    }

    /**
     * Window上での描画領域を取得します。
     *
     * @param outRect 変更を加えるRect
     */
    fun getWindowDrawingRect(outRect: Rect) {
        val currentX = xByTouch
        val currentY = yByTouch
        outRect[currentX, currentY, currentX + width] = currentY + height
    }

    private val xByTouch: Int
        /**
         * タッチ座標から算出されたFloatingViewのX座標
         *
         * @return FloatingViewのX座標
         */
        get() = (mScreenTouchX - mLocalTouchX - mTouchXOffset).toInt()

    private val yByTouch: Int
        /**
         * タッチ座標から算出されたFloatingViewのY座標
         *
         * @return FloatingViewのY座標
         */
        get() = (mMetrics.heightPixels + mNavigationBarVerticalOffset - (mScreenTouchY - mLocalTouchY + height - mTouchYOffset)).toInt()

    /**
     * 通常状態に変更します。
     */
    fun setNormal() {
        mAnimationHandler.state = STATE_NORMAL
        mAnimationHandler.updateTouchPosition(xByTouch.toFloat(), yByTouch.toFloat())
    }

    /**
     * 重なった状態に変更します。
     *
     * @param centerX 対象の中心座標X
     * @param centerY 対象の中心座標Y
     */
    fun setIntersecting(centerX: Int, centerY: Int) {
        mAnimationHandler.state = STATE_INTERSECTING
        mAnimationHandler.updateTargetPosition(centerX.toFloat(), centerY.toFloat())
    }

    /**
     * 終了状態に変更します。
     */
    fun setFinishing() {
        mAnimationHandler.state = STATE_FINISHING
        mIsMoveAccept = false
        visibility = GONE
    }

    val state: Int
        get() = mAnimationHandler.state

    /**
     * Set the cutout's safe inset area
     *
     * @param safeInsetRect [FloatingViewManager.setSafeInsetRect]
     */
    fun setSafeInsetRect(safeInsetRect: Rect?) {
        mSafeInsetRect.set(safeInsetRect!!)
    }

    /**
     * アニメーションの制御を行うハンドラです。
     */
    internal class FloatingAnimationHandler(floatingView: FloatingView) : Handler() {
        /**
         * アニメーションを開始した時間
         */
        private var mStartTime: Long = 0

        /**
         * アニメーションを始めた時点のTransitionX
         */
        private var mStartX = 0f

        /**
         * アニメーションを始めた時点のTransitionY
         */
        private var mStartY = 0f

        /**
         * 実行中のアニメーションのコード
         */
        private var mStartedCode: Int

        /**
         * アニメーション状態フラグ
         */
        private var mState: Int

        /**
         * 現在の状態
         */
        private var mIsChangeState = false

        /**
         * 追従対象のX座標
         */
        private var mTouchPositionX = 0f

        /**
         * 追従対象のY座標
         */
        private var mTouchPositionY = 0f

        /**
         * 追従対象のX座標
         */
        private var mTargetPositionX = 0f

        /**
         * 追従対象のY座標
         */
        private var mTargetPositionY = 0f

        /**
         * FloatingView
         */
        private val mFloatingView = WeakReference(floatingView)

        /**
         * コンストラクタ
         */
        init {
            mStartedCode = ANIMATION_NONE
            mState = STATE_NORMAL
        }

        /**
         * アニメーションの処理を行います。
         */
        override fun handleMessage(msg: Message) {
            val floatingView = mFloatingView.get()
            if (floatingView == null) {
                removeMessages(ANIMATION_IN_TOUCH)
                return
            }

            val animationCode = msg.what
            val animationType = msg.arg1
            val params = floatingView.windowLayoutParams

            // 状態変更またはアニメーションを開始した場合の初期化
            if (mIsChangeState || animationType == TYPE_FIRST) {
                // 状態変更時のみアニメーション時間を使う
                mStartTime = if (mIsChangeState) SystemClock.uptimeMillis() else 0
                mStartX = params.x.toFloat()
                mStartY = params.y.toFloat()
                mStartedCode = animationCode
                mIsChangeState = false
            }
            // 経過時間
            val elapsedTime = (SystemClock.uptimeMillis() - mStartTime).toFloat()
            val trackingTargetTimeRate = min(
                (elapsedTime / CAPTURE_DURATION_MILLIS).toDouble(),
                1.0
            ).toFloat()

            // 重なっていない場合のアニメーション
            if (mState == STATE_NORMAL) {
                val basePosition = calcAnimationPosition(trackingTargetTimeRate)
                // 画面外へのオーバーを認める
                val moveLimitRect = floatingView.mMoveLimitRect
                // 最終的な到達点
                val targetPositionX = min(
                    max(
                        moveLimitRect.left.toDouble(),
                        mTouchPositionX.toInt().toDouble()
                    ), moveLimitRect.right.toDouble()
                ).toFloat()
                val targetPositionY = min(
                    max(
                        moveLimitRect.top.toDouble(),
                        mTouchPositionY.toInt().toDouble()
                    ), moveLimitRect.bottom.toDouble()
                ).toFloat()
                params.x = (mStartX + (targetPositionX - mStartX) * basePosition).toInt()
                params.y = (mStartY + (targetPositionY - mStartY) * basePosition).toInt()
                floatingView.updateViewLayout()
                sendMessageAtTime(
                    newMessage(animationCode, TYPE_UPDATE),
                    SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS
                )
            } else if (mState == STATE_INTERSECTING) {
                val basePosition = calcAnimationPosition(trackingTargetTimeRate)
                // 最終的な到達点
                val targetPositionX = mTargetPositionX - floatingView.width / 2
                val targetPositionY = mTargetPositionY - floatingView.height / 2
                // 現在地からの移動
                params.x = (mStartX + (targetPositionX - mStartX) * basePosition).toInt()
                params.y = (mStartY + (targetPositionY - mStartY) * basePosition).toInt()
                floatingView.updateViewLayout()
                sendMessageAtTime(
                    newMessage(animationCode, TYPE_UPDATE),
                    SystemClock.uptimeMillis() + ANIMATION_REFRESH_TIME_MILLIS
                )
            }
        }

        /**
         * アニメーションのメッセージを送信します。
         *
         * @param animation   ANIMATION_IN_TOUCH
         * @param delayMillis メッセージの送信時間
         */
        fun sendAnimationMessageDelayed(animation: Int, delayMillis: Long) {
            sendMessageAtTime(
                newMessage(animation, TYPE_FIRST),
                SystemClock.uptimeMillis() + delayMillis
            )
        }

        /**
         * アニメーションのメッセージを送信します。
         *
         * @param animation ANIMATION_IN_TOUCH
         */
        fun sendAnimationMessage(animation: Int) {
            sendMessage(newMessage(animation, TYPE_FIRST))
        }

        /**
         * タッチ座標の位置を更新します。
         *
         * @param positionX タッチX座標
         * @param positionY タッチY座標
         */
        fun updateTouchPosition(positionX: Float, positionY: Float) {
            mTouchPositionX = positionX
            mTouchPositionY = positionY
        }

        /**
         * 追従対象の位置を更新します。
         *
         * @param centerX 追従対象のX座標
         * @param centerY 追従対象のY座標
         */
        fun updateTargetPosition(centerX: Float, centerY: Float) {
            mTargetPositionX = centerX
            mTargetPositionY = centerY
        }

        var state: Int
            /**
             * 現在の状態を返します。
             *
             * @return STATE_NORMAL or STATE_INTERSECTING or STATE_FINISHING
             */
            get() = mState
            /**
             * アニメーション状態を設定します。
             *
             * @param newState STATE_NORMAL or STATE_INTERSECTING or STATE_FINISHING
             */
            set(newState) {
                // 状態が異なった場合のみ状態を変更フラグを変える
                if (mState != newState) {
                    mIsChangeState = true
                }
                mState = newState
            }

        companion object {
            /**
             * アニメーションをリフレッシュするミリ秒
             */
            private const val ANIMATION_REFRESH_TIME_MILLIS = 10L

            /**
             * FloatingViewの吸着の着脱時間
             */
            private const val CAPTURE_DURATION_MILLIS = 300L

            /**
             * アニメーションなしの状態を表す定数
             */
            private const val ANIMATION_NONE = 0

            /**
             * タッチ時に発生するアニメーションの定数
             */
            const val ANIMATION_IN_TOUCH: Int = 1

            /**
             * アニメーション開始を表す定数
             */
            private const val TYPE_FIRST = 1

            /**
             * アニメーション更新を表す定数
             */
            private const val TYPE_UPDATE = 2

            /**
             * アニメーション時間から求められる位置を計算します。
             *
             * @param timeRate 時間比率
             * @return ベースとなる係数(0.0から1.0 ＋ α)
             */
            private fun calcAnimationPosition(timeRate: Float): Float {
                // y=0.55sin(8.0564x-π/2)+0.55
                val position = if (timeRate <= 0.4) {
                    (0.55 * sin(8.0564 * timeRate - Math.PI / 2) + 0.55).toFloat()
                } else {
                    (4 * (0.417 * timeRate - 0.341).pow(2.0) - 4 * (0.417 - 0.341).pow(2.0) + 1).toFloat()
                }
                return position
            }

            /**
             * 送信するメッセージを生成します。
             *
             * @param animation ANIMATION_IN_TOUCH
             * @param type      TYPE_FIRST,TYPE_UPDATE
             * @return Message
             */
            private fun newMessage(animation: Int, type: Int): Message {
                val message = Message.obtain()
                message.what = animation
                message.arg1 = type
                return message
            }
        }
    }

    /**
     * 長押し処理を制御するハンドラです。<br></br>
     * dispatchTouchEventで全てのタッチ処理を実装しているので、長押しも独自実装しています。
     */
    internal class LongPressHandler(view: FloatingView) : Handler() {
        /**
         * TrashView
         */
        private val mFloatingView = WeakReference(view)

        override fun handleMessage(msg: Message) {
            val view = mFloatingView.get()
            if (view == null) {
                removeMessages(LONG_PRESSED)
                return
            }

            view.onLongClick()
        }

        companion object {
            /**
             * アニメーションなしの状態を表す定数
             */
            const val LONG_PRESSED: Int = 0
        }
    }

    companion object {
        /**
         * 押下時の拡大率
         */
        private const val SCALE_PRESSED = 0.9f

        /**
         * 通常時の拡大率
         */
        private const val SCALE_NORMAL = 1.0f

        /**
         * 画面端移動アニメーションの時間
         */
        private const val MOVE_TO_EDGE_DURATION = 450L

        /**
         * 画面端移動アニメーションの係数
         */
        private const val MOVE_TO_EDGE_OVERSHOOT_TENSION = 1.25f

        /**
         * Damping ratio constant for spring animation (X coordinate)
         */
        private const val ANIMATION_SPRING_X_DAMPING_RATIO = 0.7f

        /**
         * Stiffness constant for spring animation (X coordinate)
         */
        private const val ANIMATION_SPRING_X_STIFFNESS = 350f

        /**
         * Friction constant for fling animation (X coordinate)
         */
        private const val ANIMATION_FLING_X_FRICTION = 1.7f

        /**
         * Friction constant for fling animation (Y coordinate)
         */
        private const val ANIMATION_FLING_Y_FRICTION = 1.7f

        /**
         * Current velocity units
         */
        private const val CURRENT_VELOCITY_UNITS = 1000

        /**
         * 通常状態
         */
        const val STATE_NORMAL: Int = 0

        /**
         * 重なり状態
         */
        const val STATE_INTERSECTING: Int = 1

        /**
         * 終了状態
         */
        const val STATE_FINISHING: Int = 2

        /**
         * 長押し判定とする時間(移動操作も考慮して通常の1.5倍)
         */
        private val LONG_PRESS_TIMEOUT = (1.5f * ViewConfiguration.getLongPressTimeout()).toInt()

        /**
         * Constant for scaling down X coordinate velocity
         */
        private const val MAX_X_VELOCITY_SCALE_DOWN_VALUE = 9f

        /**
         * Constant for scaling down Y coordinate velocity
         */
        private const val MAX_Y_VELOCITY_SCALE_DOWN_VALUE = 8f

        /**
         * Constant for calculating the threshold to move when throwing
         */
        private const val THROW_THRESHOLD_SCALE_DOWN_VALUE = 9f

        /**
         * デフォルトのX座標を表す値
         */
        const val DEFAULT_X: Int = Int.MIN_VALUE

        /**
         * デフォルトのY座標を表す値
         */
        const val DEFAULT_Y: Int = Int.MIN_VALUE

        /**
         * Default width size
         */
        const val DEFAULT_WIDTH: Int = ViewGroup.LayoutParams.WRAP_CONTENT

        /**
         * Default height size
         */
        const val DEFAULT_HEIGHT: Int = ViewGroup.LayoutParams.WRAP_CONTENT

        /**
         * Overlay Type
         */
        private var OVERLAY_TYPE = 0

        init {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
                OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_PRIORITY_PHONE
            } else {
                OVERLAY_TYPE = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            }
        }

        /**
         * Get the System ui dimension(pixel)
         *
         * @param resources [Resources]
         * @param resName   dimension resource name
         * @return pixel size
         */
        private fun getSystemUiDimensionPixelSize(resources: Resources, resName: String): Int {
            var pixelSize = 0
            val resId = resources.getIdentifier(resName, "dimen", "android")
            if (resId > 0) {
                pixelSize = resources.getDimensionPixelSize(resId)
            }
            return pixelSize
        }
    }
}
