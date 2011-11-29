/*
 * Copyright © 2011 Michael Castleman.
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package com.ridewithbikes

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import TypedResource._
import java.util.{TimeZone, Locale, Calendar}
import android.text.format.DateFormat
import android.util.Log

object BikeActivity {
  val SET_DATE_REQUEST = 1
  val SET_TIME_REQUEST = 2
}

class BikeActivity extends Activity with TypedActivity {
  lazy val newYork = TimeZone.getTimeZone("America/New_York")
  lazy val junction = Typeface.createFromAsset(getAssets, "fonts/Junction.otf")

  lazy val dateFormat = DateFormat.getMediumDateFormat(this)
  lazy val timeFormat = DateFormat.getTimeFormat(this)

  lazy val mainTitle = findView(TR.main_title)
  lazy val systemSpinner = findView(TR.system_spinner)
  lazy val dateButton = findView(TR.date_button)
  lazy val timeButton = findView(TR.time_button)

  var chosenTime : Calendar = null

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(TR.layout.main)

    mainTitle.setTypeface(junction)
    systemSpinner.setAdapter(new SystemAdapter(this))
  }

  override def onResume() {
    super.onResume()
    chosenTime = Calendar.getInstance(newYork, Locale.US)
    updateButtons()
  }

  private def updateButtons() {
    val d = chosenTime.getTime
    Log.d("BikeActivity", d.toString)
    dateButton setText dateFormat.format(d)
    timeButton setText timeFormat.format(d)
  }
}