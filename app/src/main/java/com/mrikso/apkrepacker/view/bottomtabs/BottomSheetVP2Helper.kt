package com.mrikso.apkrepacker.view.bottomtabs

import android.view.View
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.viewpager2.widget.ViewPager2

object BottomSheetVP2Helper {

    @JvmStatic
    fun setupViewPager(vp: ViewPager2) {
        findBottomSheetParent(vp)?.also {
            vp.registerOnPageChangeCallback(
                BottomSheetViewPagerListener(
                    vp,
                    ViewPagerBottomSheetBehavior.from(it)
                )
            )
        }
    }

    private class BottomSheetViewPagerListener(
        private val vp: ViewPager2,
        private val behavior: ViewPagerBottomSheetBehavior<View>
    ) : ViewPager2.OnPageChangeCallback() {

        override fun onPageSelected(position: Int) {
            vp.post(behavior::invalidateScrollingChild)
        }
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

}