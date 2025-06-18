package com.ext.draggablerotationalcubelibrary

import android.os.Parcelable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentPagerAdapter
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.PagerAdapter


class EndlessPagerAdapter(pagerAdapter: PagerAdapter?) : PagerAdapter() {
    private val pagerAdapter: PagerAdapter

    init {
        requireNotNull(pagerAdapter) { "Did you forget initialize PagerAdapter?" }
        require(!((pagerAdapter is FragmentPagerAdapter || pagerAdapter is FragmentStatePagerAdapter) && pagerAdapter.count < 3)) { "When you use FragmentPagerAdapter or FragmentStatePagerAdapter, it only supports >= 3 pages." }
        this.pagerAdapter = pagerAdapter
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        if (DEBUG) Log.d(TAG, "Destroy: " + getVirtualPosition(position))
        pagerAdapter.destroyItem(container, getVirtualPosition(position), `object`)

        if (pagerAdapter.count < 4) {
            pagerAdapter.instantiateItem(container, getVirtualPosition(position))
        }
    }

    override fun finishUpdate(container: ViewGroup) {
        pagerAdapter.finishUpdate(container)
    }

    override fun getCount(): Int {
        return Int.MAX_VALUE // this is the magic that we can scroll infinitely.
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return pagerAdapter.getPageTitle(getVirtualPosition(position))
    }

    override fun getPageWidth(position: Int): Float {
        return pagerAdapter.getPageWidth(getVirtualPosition(position))
    }

    override fun isViewFromObject(view: View, o: Any): Boolean {
        return pagerAdapter.isViewFromObject(view, o)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        if (DEBUG) Log.d(TAG, "Instantiate: " + getVirtualPosition(position))
        return pagerAdapter.instantiateItem(container, getVirtualPosition(position))
    }

    override fun saveState(): Parcelable? {
        return pagerAdapter.saveState()
    }

    override fun restoreState(state: Parcelable?, loader: ClassLoader?) {
        pagerAdapter.restoreState(state, loader)
    }

    override fun startUpdate(container: ViewGroup) {
        pagerAdapter.startUpdate(container)
    }

    private fun getVirtualPosition(realPosition: Int): Int {
        return realPosition % pagerAdapter.count
    }

    companion object {
        private const val TAG = "EndlessPagerAdapter"
        private const val DEBUG = false
    }
}