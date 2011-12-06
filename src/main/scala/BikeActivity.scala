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
import transit.{Result, Direction, System}
import TypedResource._
import java.util.{TimeZone, Locale, Calendar}
import android.text.format.DateFormat
import android.util.Log
import android.app.Activity
import android.widget.{TextView, TableRow}
import android.view.{Gravity, View}

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
  lazy val fullDayTable = findView(TR.full_day_table)
  lazy val systemAdapter = new SystemAdapter(this)

  val chosenTime : Calendar = Calendar.getInstance(newYork, Locale.US)
  private var allDay = false

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(TR.layout.main)

    mainTitle.setTypeface(junction)
    resultText.setTypeface(junction)
    systemSpinner.setAdapter(systemAdapter)
    systemSpinner.setOnItemSelectedListener({calculateResult()})
    makeClickable(resultDetails)

    dateButton.setOnClickListener({showDialog(BikeActivity.SET_DATE_REQUEST)})
    timeButton.setOnClickListener({showDialog(BikeActivity.SET_TIME_REQUEST)})
  }


  override def onRestoreInstanceState(icicle: Bundle) {
    super.onRestoreInstanceState(icicle)
    if (icicle.containsKey("chosenTime"))
      chosenTime.setTimeInMillis(icicle.getLong("chosenTime"))
    if (icicle.containsKey("allDay"))
      allDay = icicle.getBoolean("allDay")
  }

  override def onSaveInstanceState(icicle: Bundle) {
    super.onSaveInstanceState(icicle)
    icicle.putLong("chosenTime", chosenTime.getTimeInMillis)
    icicle.putBoolean("allDay", allDay)
  }

  override def onResume() {
    super.onResume()
    updateButtons()
  }

  override def onCreateDialog(id: Int) = id match {
    case BikeActivity.SET_DATE_REQUEST =>
      new DateWheelDialog(this, setDate _, chosenTime.get(Calendar.YEAR), chosenTime.get(Calendar.MONTH), chosenTime.get(Calendar.DAY_OF_MONTH))
    case BikeActivity.SET_TIME_REQUEST =>
      new TimeWheelDialog(this, setTime _, setAllDay _, chosenTime.get(Calendar.HOUR_OF_DAY), chosenTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this))
    case _ => null
  }

  private def updateButtons() {
    val d = chosenTime.getTime
//    Log.d("BikeActivity", d.toString)
//    Log.d("BikeActivity", allDay.toString)
    dateButton setText dateFormat.format(d)
    timeButton setText (if (allDay) getString(R.string.all_day) else timeFormat.format(d))
  }

  private def getDetailsId(sys: System) = {
    getResources.getIdentifier("notes_" + sys.slug, "string", "com.ridewithbikes")
  }

  private def getMaybeText(sys: System): Option[CharSequence] = {
    getResources.getIdentifier("maybe_" + sys.slug, "string", "com.ridewithbikes") match {
      case 0 => None
      case id => Some(getText(id))
    }
  }

  private def getMaybeText(sys: System, result: String): Option[CharSequence] = {
    if (result.contains("MAYBE"))
      getMaybeText(sys)
    else
      None
  }

  private def viewWithText(txt: String) = {
    val tv = new TextView(this)
    tv setGravity Gravity.CENTER_HORIZONTAL
    tv setText txt
    tv
  }

  private def embolden(tv: TextView) = {
    tv.setTypeface(Typeface.DEFAULT_BOLD)
    tv
  }

  private def makeHeader(dirs: List[Direction.Direction]) = {
    val tr = new TableRow(this)
    tr.addView(new View(this))
    for (dir <- dirs) {
      tr.addView(embolden(viewWithText(dir.toString)))
    }

    tr
  }

  private def makeRow(row: (Calendar, Calendar, List[Result.Result])) = {
    val tr = new TableRow(this)
    val (start, end, results) = row

    tr.addView(viewWithText(timeFormat.format(start.getTime) + " \u2013 " + timeFormat.format(end.getTime)))
    for (result <- results) {
      tr.addView(viewWithText(result.toString))
    }

    tr
  }

  private def calculateResult() {
    fullDayTable.removeAllViews()

    systemSpinner.getSelectedItem match {
      case sys : System =>
        if (allDay) {
          resultText setVisibility View.GONE
          resultPane setVisibility View.VISIBLE
          if (sys.directions.length > 1)
            fullDayTable.addView(makeHeader(sys.directions))
          for (entry <- sys.friendly_table(chosenTime))
            fullDayTable.addView(makeRow(entry))
          fullDayTable setStretchAllColumns true
          fullDayTable setVisibility View.VISIBLE
        } else {
          val result = sys.summarize(chosenTime).toUpperCase
          fullDayTable setVisibility View.GONE
          resultText setVisibility View.VISIBLE
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
        }

      case _ => resultPane setVisibility View.GONE
    }
  }

  private def setTime(hourOfDay: Int, minute: Int) {
    allDay = false
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

  private def setAllDay() {
    allDay = true
    updateButtons()
    calculateResult()
  }
}