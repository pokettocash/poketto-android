package com.poketto.poketto.controllers

import android.content.Context
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.support.v4.view.ViewPager
import android.widget.Button
import android.view.View
import android.content.Intent
import android.support.design.widget.TabLayout
import android.view.ViewGroup
import android.view.LayoutInflater
import android.support.v4.view.PagerAdapter
import com.poketto.poketto.R
import com.poketto.poketto.data.PokettoPreferenceManager
import com.poketto.poketto.services.Wallet

class OnboardingActivity: FragmentActivity() {

    private lateinit var mPager: ViewPager

    var preferenceManager: PokettoPreferenceManager? = null
    var screens: IntArray = intArrayOf(
        R.layout.intro_screen1,
        R.layout.intro_screen2,
        R.layout.intro_screen3,
        R.layout.intro_screen4,
        R.layout.intro_screen5
    )
    var nextButton: Button? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = findViewById(R.id.pager)

        val tabLayout = findViewById<View>(R.id.tabDots) as TabLayout
        tabLayout.setupWithViewPager(mPager)

        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = OnboardingPagerAdapter()
        mPager.adapter = pagerAdapter

        nextButton = findViewById(R.id.next)
        nextButton!!.setOnClickListener { this.next() }

        mPager.addOnPageChangeListener(viewPagerPageChangeListener)

        preferenceManager = PokettoPreferenceManager(this)
        if (!preferenceManager!!.firstLaunch()) {
            launchMain()
            finish()
        }
    }

    override fun onBackPressed() {
        if (mPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            mPager.currentItem = mPager.currentItem - 1
        }
    }

    fun next() {
        Wallet(this).generate() // TODO: check generate wallet error
        launchMain()
    }

    private fun launchMain() {
        preferenceManager!!.setFirstTimeLaunch(false)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    var viewPagerPageChangeListener: ViewPager.OnPageChangeListener = object : ViewPager.OnPageChangeListener {

        override fun onPageSelected(position: Int) {

        }

        override fun onPageScrolled(arg0: Int, arg1: Float, arg2: Int) {

        }

        override fun onPageScrollStateChanged(arg0: Int) {

        }
    }

    inner class OnboardingPagerAdapter : PagerAdapter() {
        private var inflater: LayoutInflater? = null

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = inflater!!.inflate(screens.get(position), container, false)
            container.addView(view)
            return view
        }

        override fun getCount(): Int {
            return screens.count()
        }

        override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
            val v = `object` as View
            container.removeView(v)
        }

        override fun isViewFromObject(v: View, `object`: Any): Boolean {
            return v === `object`
        }
    }

}