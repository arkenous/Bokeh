package link.k3n.bokeh

import android.animation.Animator
import android.animation.TimeInterpolator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import android.widget.RelativeLayout
import java.util.*

/**
 * Created by k3nsuk3 on 2017/07/29.
 */

class BokehLayout : RelativeLayout {
    companion object {
        val INTERP_LINEAR = 0
        val INTERP_ACCELERATE = 1
        val INTERP_DECELERATE = 2
        val INTERP_ACCELERATE_DECELERATE = 3
        val DEFAULT_COUNT = 20
        val DEFAULT_COLOR = Color.argb(150, 100, 100, 100)
        val DEFAULT_DURATION: Long = 100000
        val DEFAULT_INTERPOLATOR = INTERP_LINEAR

        private fun createInterpolator(type: Int): TimeInterpolator {
            when(type) {
                INTERP_ACCELERATE -> return AccelerateInterpolator()
                INTERP_DECELERATE -> return DecelerateInterpolator()
                INTERP_ACCELERATE_DECELERATE -> return AccelerateDecelerateInterpolator()
                else -> return LinearInterpolator()
            }
        }
    }

    private var mCount = DEFAULT_COUNT
    private var mDuration = DEFAULT_DURATION
    private var mColor = DEFAULT_COLOR
    private var mInterpolator = DEFAULT_INTERPOLATOR
    private val mBokehList: MutableList<BokehView> = mutableListOf()
    private var mIsStarted = false

    private var mCenterX = 0f
    private var mCenterY = 0f
    private var mRadius = 0f
    private val mPaint = Paint()
    private val mRandom = Random()

    constructor(context: Context) : this(context, null, 0)

    constructor(context: Context, attrs: AttributeSet) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        // Create Paint
        mPaint.isAntiAlias = true
        mPaint.style = Paint.Style.FILL
        mPaint.color = mColor

        build()
    }

    @Synchronized fun start() {
        if (mIsStarted) return
        if (mBokehList.size <= 0) build()

        for (i in mBokehList.indices) {
            val delay = i * mDuration / mCount
            mBokehList[i].animate().setStartDelay(delay).start()
        }
    }

    @Synchronized fun stop() {
        if (mBokehList.size <= 0 || !mIsStarted) return

        mBokehList.forEach { it.animate().cancel() }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec) - paddingLeft - paddingRight
        val height = MeasureSpec.getSize(heightMeasureSpec) - paddingTop - paddingBottom

        mCenterX = width * 0.5f
        mCenterY = height * 0.5f
        mRadius = Math.min(width, height) * 0.5f

        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
    }

    private fun clear() {
        stop()

        mBokehList.forEach { removeView(it) }
        mBokehList.clear()
    }

    private fun build() {
        val layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        )

        for (i in 1..mCount) {
            // Setup View
            val bokehView = BokehView(context)

            bokehView.scaleX = bokehView.mScale
            bokehView.scaleY = bokehView.mScale
            bokehView.x = mRandom.nextFloat() * 500f * (if(mRandom.nextBoolean()) 1 else -1)
            bokehView.y = mRandom.nextFloat() * 500f * (if(mRandom.nextBoolean()) 1 else -1)
            bokehView.alpha = 0f

            addView(bokehView, 0, layoutParams)

            val delay = i * mDuration / mCount
            val animator = bokehView.animate()

            animator.startDelay = delay

            animator.x(mRandom.nextFloat() * 500f * (if(mRandom.nextBoolean()) 1 else -1))
            animator.y(mRandom.nextFloat() * 500f * (if(mRandom.nextBoolean()) 1 else -1))
            animator.alpha(mRandom.nextFloat())
            animator.duration = mDuration
            animator.interpolator = createInterpolator(mInterpolator)
            animator.setListener(mAnimatorListener)

            mBokehList.add(bokehView)
        }
    }

    private fun reset() {
        clear()
        build()

        if (mIsStarted) start()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        mBokehList.forEach { it.animate().cancel() }
    }

    inner class BokehView(context: Context) : View(context) {
        var mScale: Float = 0.5f

        init {
            mScale = mRandom.nextFloat() * 0.5f
        }

        override fun onDraw(canvas: Canvas?) {
            canvas?.drawCircle(mCenterX, mCenterY, mRadius, mPaint)
        }
    }

    private val mAnimatorListener = object: Animator.AnimatorListener {
        override fun onAnimationStart(animation: Animator?) {
            Log.e("Bokeh", "onAnimationStart")
            mIsStarted = true
        }

        override fun onAnimationEnd(animation: Animator?) {
            Log.e("Bokeh", "onAnimationEnd")
            if (mBokehList.size <= 0) return

            val bokehView = mBokehList.removeAt(0)

            //TODO: Bokeh が画面外に移動した際の処理を記述
            val topEdge = top + paddingTop
            val leftEdge = left + paddingLeft
            val rightEdge = right - paddingRight
            val bottomEdge = bottom - paddingBottom


            val animator = bokehView.animate()

            animator.x(mRandom.nextFloat() * 500f * (if(mRandom.nextBoolean()) 1 else -1))
            animator.y(mRandom.nextFloat() * 500f * (if(mRandom.nextBoolean()) 1 else -1))
            animator.alpha(mRandom.nextFloat())
            animator.duration = mDuration
            animator.interpolator = createInterpolator(mInterpolator)
            animator.setListener(this)

            mBokehList.add(bokehView)
            animator.start()
        }

        override fun onAnimationCancel(animation: Animator?) {
            mIsStarted = false
        }

        override fun onAnimationRepeat(animation: Animator?) {
            mIsStarted = false
        }
    }
}
