/*
 * Copyright © 2012 Michael Castleman.
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package com.ridewithbikes

import android.graphics.Typeface
import Implicits._
import transit.{Result, Direction, System}
import TypedResource._
import java.util.{TimeZone, Locale, Calendar}
import android.text.format.DateFormat
import android.widget.{TextView, TableRow}
import android.view._
import android.content.Intent
import android.text.SpannableStringBuilder
import io.BufferedSource
import android.net.Uri
import android.app.{Dialog, AlertDialog, Activity}
import android.os.{Build, Bundle}

object BikeActivity {
  final val SET_DATE_DIALOG = 1
  final val SET_TIME_DIALOG = 2
  final val ABOUT_DIALOG = 3

  final val FLATTR_THING_ID = "ce83074882a92ef56a1fb154f1406c21"
  final val MAX_SAVE_TIME = 5 * 60 * 1000L
  
  final val isHoneycomb = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB
  lazy val newYork = TimeZone.getTimeZone("America/New_York")
}

class BikeActivity extends Activity with TypedActivity with ClickableText {
  lazy val junction = Typeface.createFromAsset(getAssets, "fonts/Junction.otf")

  lazy val dateFormat = { val df = DateFormat.getMediumDateFormat(this); df.setTimeZone(BikeActivity.newYork); df }
  lazy val timeFormat = { val tf = DateFormat.getTimeFormat(this); tf.setTimeZone(BikeActivity.newYork); tf }

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

  lazy val aboutIntent = {
    val i = new Intent("org.openintents.action.SHOW_ABOUT_DIALOG")
    i.putExtra("org.openintents.extra.PACKAGE_NAME", getPackageName)
    i
  }
  lazy val flattrIntent = {
    val i = new Intent("com.flattr4android.app.DISPLAY_THING")
    i.putExtra("THING_ID", BikeActivity.FLATTR_THING_ID)
    i
  }
  lazy val flattrWebIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://flattr.com/thing/" + BikeActivity.FLATTR_THING_ID))

  lazy val selfPackageInfo = getPackageManager.getPackageInfo(getPackageName, 0)

  val chosenTime : Calendar = Calendar.getInstance(BikeActivity.newYork, Locale.US)
  private var allDay = false
  private var lastDisappearTime = 0L

  override def onCreate(icicle: Bundle) {
    super.onCreate(icicle)
    setContentView(TR.layout.main)

    mainTitle.setTypeface(junction)
    resultText.setTypeface(junction)
    systemSpinner.setAdapter(systemAdapter)
    systemSpinner.setOnItemSelectedListener({calculateResult()})
    makeClickable(resultDetails)

    dateButton.setOnClickListener({showDialog(BikeActivity.SET_DATE_DIALOG)})
    timeButton.setOnClickListener({showDialog(BikeActivity.SET_TIME_DIALOG)})
  }


  override def onRestoreInstanceState(icicle: Bundle) {
    super.onRestoreInstanceState(icicle)
    if (!icicle.containsKey("instanceSaveTime") || (java.lang.System.currentTimeMillis() - icicle.getLong("instanceSaveTime")) < BikeActivity.MAX_SAVE_TIME) {
      if (icicle.containsKey("chosenTime"))
        chosenTime.setTimeInMillis(icicle.getLong("chosenTime"))
      if (icicle.containsKey("allDay"))
        allDay = icicle.getBoolean("allDay")
    }
  }

  override def onSaveInstanceState(icicle: Bundle) {
    super.onSaveInstanceState(icicle)
    icicle.putLong("chosenTime", chosenTime.getTimeInMillis)
    icicle.putBoolean("allDay", allDay)
    icicle.putLong("instanceSaveTime", java.lang.System.currentTimeMillis())
  }

  override def onPause() {
    super.onPause()
    lastDisappearTime = java.lang.System.currentTimeMillis()
  }

  override def onResume() {
    super.onResume()
    val now = java.lang.System.currentTimeMillis()
    if (now - lastDisappearTime > BikeActivity.MAX_SAVE_TIME)
      chosenTime.setTimeInMillis(now)
    updateButtons()
  }

  override def onCreateDialog(id: Int) = id match {
    case BikeActivity.SET_DATE_DIALOG =>
      if (BikeActivity.isHoneycomb)
        new SettableDatePickerDialog(this, setDate _, chosenTime.get(Calendar.YEAR), chosenTime.get(Calendar.MONTH), chosenTime.get(Calendar.DAY_OF_MONTH))
      else
        new DateWheelDialog(this, setDate _, chosenTime.get(Calendar.YEAR), chosenTime.get(Calendar.MONTH), chosenTime.get(Calendar.DAY_OF_MONTH))
    case BikeActivity.SET_TIME_DIALOG =>
      if (BikeActivity.isHoneycomb)
        new SettableTimePickerDialog(this, setTime _, setAllDay _, chosenTime.get(Calendar.HOUR_OF_DAY), chosenTime.get(Calendar.MINUTE))
      else
        new TimeWheelDialog(this, setTime _, setAllDay _, chosenTime.get(Calendar.HOUR_OF_DAY), chosenTime.get(Calendar.MINUTE), DateFormat.is24HourFormat(this))
    case BikeActivity.ABOUT_DIALOG =>
      val message = new SpannableStringBuilder()
        .append(getText(R.string.about_comments)).append("\n\n")
        .append(getLicense)
      new AlertDialog.Builder(this)
        .setTitle(getString(selfPackageInfo.applicationInfo.labelRes) + " " + selfPackageInfo.versionName)
        .setIcon(R.drawable.icon)
        .setMessage(message)
        .setPositiveButton(android.R.string.ok, {})
        .create()
    case _ => super.onCreateDialog(id)
  }

  override def onPrepareDialog(id: Int, dialog: Dialog) {
    id match {
      case BikeActivity.SET_DATE_DIALOG | BikeActivity.SET_TIME_DIALOG =>
        dialog.asInstanceOf[DateSettable].date_=(chosenTime)
      case _ => super.onPrepareDialog(id, dialog)
    }
  }

  private def getLicense : CharSequence = {
    val builder = new StringBuilder
    val stream = getResources.openRawResource(R.raw.about_license)
    new BufferedSource(stream).getLines() foreach { _ match {
      case "" => builder.append("\n\n")
      case line => builder.append(line).append(' ')
    } }

    builder
  }

  override def onCreateOptionsMenu(menu: Menu) = {
    val inflater = getMenuInflater
    inflater.inflate(R.menu.main_menu, menu)
    true
  }

  private def haveIntent(intent: Intent): Boolean =
    !getPackageManager.queryIntentActivities(intent, 0).isEmpty

  private def haveOiAbout = haveIntent(aboutIntent)
  private def haveFlattrApp = haveIntent(flattrIntent)

  override def onOptionsItemSelected(item: MenuItem) = item.getItemId match {
    case R.id.about_menu =>
      if (haveOiAbout)
        startActivityForResult(aboutIntent, 101)
      else
        showDialog(BikeActivity.ABOUT_DIALOG)

      true

    case R.id.flattr_menu =>
      if (haveFlattrApp)
        startActivity(flattrIntent)
      else
        startActivity(flattrWebIntent)

      true

    case _ => super.onOptionsItemSelected(item)
  }

  private def updateButtons() {
    val d = chosenTime.getTime
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

  private def viewWithText(txt: CharSequence) = {
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
        resultPane setVisibility View.VISIBLE
        resultDetails setText getDetailsId(sys)

        if (allDay) {
          resultText setVisibility View.GONE
          if (sys.directions.length > 1)
            fullDayTable.addView(makeHeader(sys.directions))
          for (entry <- sys.friendly_table(chosenTime))
            fullDayTable.addView(makeRow(entry))
          fullDayTable setStretchAllColumns true
          fullDayTable setVisibility View.VISIBLE

          if (fullDayTable.getChildCount == 1)
            fullDayTable.getChildAt(0).asInstanceOf[ViewGroup].getChildAt(0).asInstanceOf[TextView].setText(R.string.all_day_result)

          resultMaybe setVisibility View.GONE // hm?
        } else {
          val result = sys.summarize(chosenTime).toUpperCase
          fullDayTable setVisibility View.GONE
          resultText setVisibility View.VISIBLE
          resultText setText result
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