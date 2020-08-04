package com.mrikso.apkrepacker.view.bottomtabs

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.SimpleOnPageChangeListener

object BottomSheetVPHelper {

    @JvmStatic
    fun setupViewPager(vp: ViewPager) {
        findBottomSheetParent(vp)?.also {
            vp.addOnPageChangeListener(
                BottomSheetViewPagerListener(
                    vp, ViewPagerBottomSheetBehavior.from(it)
                )
            )
        }
    }

    @JvmStatic
    fun getCurrentViewWithVP(vp: ViewPager): View? {
        val currentItem: Int = vp.currentItem
        (0 until vp.childCount).forEach { index ->
            val child = vp.getChildAt(index)
            val layoutParams = child.layoutParams as ViewPager.LayoutParams
            val position = layoutParams.javaClass.getDeclaredField("position").runCatching {
                isAccessible = true
                get(layoutParams) as Int
            }.getOrElse { -1 }
            if (layoutParams.isDecor.not() && currentItem == position) {
                return child
            }
        }
        return null
    }

    private fun findBottomSheetParent(view: View): View? {
        var current: View? = view
        while (current != null) {
            val params = current.layoutParams
            if (params is CoordinatorLayout.LayoutParams && params.behavior is ViewPagerBottomSheetBehavior<*>) {
                return current
            }
            val parent = current.parent
            current = if (parent !is View) null else parent
        }
        return null
    }

    private class BottomSheetViewPagerListener(
        private val vp: ViewPager,
        private val behavior: ViewPagerBottomSheetBehavior<View>
    ) : SimpleOnPageChangeListener() {

        override fun onPageSelected(position: Int) {
            vp.post(behavior::invalidateScrollingChild)
        }

    }
}