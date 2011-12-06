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

import android.graphics.Typeface
import android.os.Bundle
import Implicits._
import TypedResource._
import transit.System
import java.util.{TimeZone, Locale, Calendar}
import android.text.format.DateFormat
import android.util.Log
import android.view.View
import android.app.{TimePickerDialog, DatePickerDialog, Activity}

object BikeActivity {
  val SET_DATE_REQUEST = 1
  val SET_TIME_REQUEST = 2
}

class BikeActivity extends Activity with TypedActivity with ClickableText {
  lazy val newYork = TimeZone.getTimeZone("America/New_York")
  lazy val junction = Typeface.createFromAsset(getAssets, "fonts/Junction.otf")

  lazy val dateFormat = DateFormat.getMediumDateFormat(this)
  lazy val timeFormat = DateFormat.getTimeFormat(this)

  lazy val mainTitle = findView(TR.main_title)
  lazy val systemSpinner = findView(TR.system_spinner)
  lazy val dateButton = findView(TR.date_button)
  lazy val timeButton = findView(TR.time_button)
  lazy val resultText = findView(TR.result_text)
  lazy val resultMaybe = findView(TR.result_maybe)
  lazy val resultDetails = findView(TR.result_details)
  lazy val resultPane = findView(TR.result_pane)
  lazy val systemAdapter = new SystemAdapter(this)

  val chosenTime : Calendar = Calendar.getInstance(newYork, Locale.US)

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(TR.layout.main)

    mainTitle.setTypeface(junction)
    resultText.setTypeface(junction)
    systemSpinner.setAdapter(systemAdapter)
    systemSpinner.setOnItemSelectedListener({calculateResult})
    makeClickable(resultDetails)

    dateButton.setOnClickListener({showDialog(BikeActivity.SET_DATE_REQUEST)})
    timeButton.setOnClickListener({showDialog(BikeActivity.SET_TIME_REQUEST)})
  }


  override def onRestoreInstanceState(icicle: Bundle) {
    super.onRestoreInstanceState(icicle)
    if (icicle.containsKey("chosenTime"))
      chosenTime.setTimeInMillis(icicle.getLong("chosenTime"))
  }

  override def onSaveInstanceState(icicle: Bundle) {
    super.onSaveInstanceState(icicle)
    icicle.putLong("chosenTime", chosenTime.getTimeInMillis)
  }

  override def onResume() {
    super.onResume()
    updateButtons()
  }

  override def onCreateDialog(id: Int) = id match {
    case BikeActivity.SET_DATE_REQUEST =>
      new DateWheelDialog(this, setDate _, chosenTime.get(Calendar.YEAR), chosenTime.get(Calendar.MONTH), chosenTime.get(Calendar.DAY_OF_MONTH))
    case BikeActivity.SET_TIME_REQUEST =>
      new TimePickerDialog(this, setTime _, chosenTime.get(Calendar.HOUR_OF_DAY), chosenTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this))
    case _ => null
  }

  private def updateButtons() {
    val d = chosenTime.getTime
    Log.d("BikeActivity", d.toString)
    dateButton setText dateFormat.format(d)
    timeButton setText timeFormat.format(d)
  }

  private def getDetailsId(sys: System) = {
    getResources.getIdentifier("notes_" + sys.slug, "string", "com.ridewithbikes")
  }

  private def getMaybeText(sys: System, result: String) = {
    if (result.contains("MAYBE"))
      getResources.getIdentifier("maybe_" + sys.slug, "string", "com.ridewithbikes") match {
        case 0 => None
        case id => Some(getText(id))
      }
    else
      None
  }

  private def calculateResult() {
    systemSpinner.getSelectedItem match {
      case sys : System =>
        val result = sys.summarize(chosenTime).toUpperCase
        resultText setText result
        resultPane setVisibility View.VISIBLE
        resultDetails setText getDetailsId(sys)
        getMaybeText(sys, result) match {
          case None => resultMaybe setVisibility View.GONE
          case Some(text) => {
            resultMaybe setText text
            resultMaybe setVisibility View.VISIBLE
          }
        }

      case _ => resultPane setVisibility View.GONE
    }
  }

  private def setTime(hourOfDay: Int, minute: Int) {
    chosenTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
    chosenTime.set(Calendar.MINUTE, minute)
    updateButtons()
    calculateResult()
  }

  private def setDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
    chosenTime.set(Calendar.YEAR, year)
    chosenTime.set(Calendar.MONTH, monthOfYear)
    chosenTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    updateButtons()
    calculateResult()
  }
}