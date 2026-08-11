package com.turingheights.wally

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class NavigationTest {

    @get:Rule(order = 0)
    var hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    var activityRule = ActivityScenarioRule(MainActivity::class.java)

    @Test
    fun testNavigationToSettings() {
        // Wait for HomeFragment to load and check if settings icon is there
        onView(withId(R.id.action_settings)).check(matches(isDisplayed()))
        
        // Click on settings
        onView(withId(R.id.action_settings)).perform(click())
        
        // Verify we are on Settings screen
        onView(withId(R.id.settings_title)).check(matches(isDisplayed()))
    }

    @Test
    fun testNavigationToFavourites() {
        onView(withId(R.id.action_favourite)).check(matches(isDisplayed()))
        onView(withId(R.id.action_favourite)).perform(click())
        
        // Verify we are on Favourites screen
        onView(withId(R.id.favourites_title)).check(matches(isDisplayed()))
    }
}
