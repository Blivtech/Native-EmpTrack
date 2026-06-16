package com.blivtech.emptrack.utils

import android.view.View
import com.facebook.shimmer.ShimmerFrameLayout

object ShimmerHelper {

    // ✅ Start shimmer + hide real view
    fun show(
        shimmer: ShimmerFrameLayout,
        vararg hideViews: View
    ) {
        shimmer.visibility = View.VISIBLE
        shimmer.startShimmer()
        hideViews.forEach { it.visibility = View.GONE }
    }

    // ✅ Stop shimmer + show real view
    fun hide(
        shimmer: ShimmerFrameLayout,
        vararg showViews: View
    ) {
        shimmer.stopShimmer()
        shimmer.visibility = View.GONE
        showViews.forEach { it.visibility = View.VISIBLE }
    }
}