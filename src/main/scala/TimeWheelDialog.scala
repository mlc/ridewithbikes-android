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

import TypedResource._
import Implicits._
import android.app.TimePickerDialog.OnTimeSetListener
import android.app.AlertDialog
import android.content.{DialogInterface, Context}
import java.text.DateFormatSymbols
import android.view.{View, LayoutInflater}
import kankan.wheel.widget.adapters.{ArrayWheelAdapter, NumericWheelAdapter}
import android.text.format.DateFormat
import android.os.Bundle
import java.util.{Locale, Calendar}

class TimeWheelDialog(val ctx: Context, val callback: OnTimeSetListener, val alldayCallback: () => Any, val is24HourView: Boolean)
  extends AlertDialog(ctx) with TypedDialog with DialogInterface.OnClickListener {
  lazy val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
  lazy val view = inflater.inflate(R.layout.time_wheel, null)
  lazy val hourWheel = view.findView(TR.hour_wheel)
  lazy val minuteWheel = view.findView(TR.minute_wheel)
  lazy val ampmWheel = view.findView(TR.ampm_wheel)
  lazy val ampmStrings = new DateFormatSymbols().getAmPmStrings
  lazy val formatter = DateFormat.getTimeFormat(ctx)

  setView(view)
  setButton(DialogInterface.BUTTON_POSITIVE, ctx.getText(R.string.set), this)
  setButton(DialogInterface.BUTTON_NEGATIVE, ctx.getText(android.R.string.cancel), this)
  setButton(DialogInterface.BUTTON_NEUTRAL, ctx.getText(R.string.all_day), this)

  if (is24HourView) {
    hourWheel.setViewAdapter(new NumericWheelAdapter(ctx, 0, 23))
    ampmWheel.setVisibility(View.GONE)
  } else {
    hourWheel.setViewAdapter(new NumericWheelAdapter(ctx, 1, 12))
    ampmWheel.setViewAdapter(new ArrayWheelAdapter[String](ctx, ampmStrings))
    ampmWheel.addChangingListener({updateTitle()})
  }
  hourWheel.addChangingListener({updateTitle()})
  minuteWheel.addChangingListener({updateTitle()})
  minuteWheel.setViewAdapter(new NumericWheelAdapter(ctx, 0, 59, "%02d"))

  def hourOfDay = if (is24HourView) hourWheel.getCurrentItem else ((hourWheel.getCurrentItem+1)%12 + ampmWheel.getCurrentItem*12)
  def minute = minuteWheel.getCurrentItem
  def hourOfDay_=(hourOfDay: Int) {
    if (is24HourView)
      hourWheel.setCurrentItem(hourOfDay)
    else {
      hourWheel.setCurrentItem((hourOfDay + 11) % 12)
      ampmWheel.setCurrentItem(if (hourOfDay < 12) 0 else 1)
    }
  }
  def minute_=(minute: Int) { minuteWheel.setCurrentItem(minute) }
  def date : Calendar = {
    val c = Calendar.getInstance(Locale.US)
    c.set(Calendar.HOUR_OF_DAY, hourOfDay)
    c.set(Calendar.MINUTE, minute)
    c
  }
  def date_=(c: Calendar) {
    hourOfDay = c.get(Calendar.HOUR_OF_DAY)
    minute = c.get(Calendar.MINUTE)
  }

  override def onSaveInstanceState() = {
    val icicle = super.onSaveInstanceState() match {
      case null => new Bundle()
      case x => x
    }

    icicle.putInt("hourOfDay", hourOfDay)
    icicle.putInt("minute", minute)

    icicle
  }

  override def onRestoreInstanceState(icicle: Bundle) {
    super.onRestoreInstanceState(icicle)
    if (icicle != null) {
      if (icicle.containsKey("hourOfDay"))
        hourOfDay = icicle.getInt("hourOfDay")
      if (icicle.containsKey("minute"))
        minute = icicle.getInt("minute")
    }
  }

  def onClick(dlg: DialogInterface, which: Int) {
    which match {
    case DialogInterface.BUTTON_POSITIVE => if (callback != null)
      callback.onTimeSet(null, hourOfDay, minute)
    case DialogInterface.BUTTON_NEUTRAL => if (callback != null)
      alldayCallback()
    case _ => ()
    }
  }

  def updateTitle() {
    val cal = Calendar.getInstance
    cal.set(Calendar.HOUR_OF_DAY, hourOfDay)
    cal.set(Calendar.MINUTE, minute)
    setTitle(formatter.format(cal.getTime))
  }

  def this(ctx: Context, callback: OnTimeSetListener, alldayCallback : () => Any, hourOfDay: Int, minute: Int, is24HourView: Boolean) {
    this(ctx, callback, alldayCallback, is24HourView)
    this.hourOfDay = hourOfDay
    this.minute = minute
    updateTitle()
  }
}