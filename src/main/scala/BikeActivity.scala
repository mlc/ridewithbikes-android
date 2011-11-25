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
import android.text.format.DateUtils
import java.util.{TimeZone, Locale, Calendar}

class BikeActivity extends Activity with TypedActivity {
  lazy val junction = Typeface.createFromAsset(getAssets, "fonts/Junction.otf")

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
    chosenTime = Calendar.getInstance(Locale.US)
    updateButtons()
  }

  private def updateButtons() {

  }
}