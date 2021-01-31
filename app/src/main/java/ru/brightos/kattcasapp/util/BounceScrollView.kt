package ru.brightos.kattcasapp.util

import android.content.Context
import android.util.AttributeSet
import android.util.DisplayMetrics

import android.widget.ScrollView
import androidx.core.widget.NestedScrollView


class BounceScrollView : NestedScrollView {
    private var mContext: Context
    private var mMaxYOverscrollDistance = 0

    constructor(context: Context) : super(context) {
        mContext = context
        initBounceScrollView()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        mContext = context
        initBounceScrollView()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(
        context,
        attrs,
        defStyle
    ) {
        mContext = context
        initBounceScrollView()
    }

    private fun initBounceScrollView() {
        //get the density of the screen and do some maths with it on the max overscroll distance
        //variable so that you get similar behaviors no matter what the screen size
        val metrics: DisplayMetrics = mContext.getResources().getDisplayMetrics()
        val density = metrics.density
        mMaxYOverscrollDistance = (density * MAX_Y_OVERSCROLL_DISTANCE).toInt()
    }

    override fun overScrollBy(
        deltaX: Int,
        deltaY: Int,
        scrollX: Int,
        scrollY: Int,
        scrollRangeX: Int,
        scrollRangeY: Int,
        maxOverScrollX: Int,
        maxOverScrollY: Int,
        isTouchEvent: Boolean
    ): Boolean {
        //This is where the magic happens, we have replaced the incoming maxOverScrollY with our own custom variable mMaxYOverscrollDistance;
        return super.overScrollBy(
            deltaX,
            deltaY,
            scrollX,
            scrollY,
            scrollRangeX,
            scrollRangeY,
            maxOverScrollX,
            mMaxYOverscrollDistance,
            isTouchEvent
        )
    }



    companion object {
        private const val MAX_Y_OVERSCROLL_DISTANCE = 200
    }
}