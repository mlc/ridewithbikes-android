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

import TypedResource._
import Implicits._
import android.app.DatePickerDialog.OnDateSetListener
import android.app.AlertDialog
import android.content.{DialogInterface, Context}
import kankan.wheel.widget.adapters.{ArrayWheelAdapter, NumericWheelAdapter}
import java.util.Calendar
import android.view.LayoutInflater
import java.text.{SimpleDateFormat, DateFormatSymbols}

class DateWheelDialog(val ctx: Context, val callback: OnDateSetListener)
  extends AlertDialog(ctx) with TypedDialog with DialogInterface.OnClickListener {
  lazy val inflater = ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE).asInstanceOf[LayoutInflater]
  lazy val view = inflater.inflate(R.layout.date_wheel, null)
  lazy val monthWheel = view.findView(TR.month_wheel)
  lazy val dayWheel = view.findView(TR.day_wheel)
  lazy val yearWheel = view.findView(TR.year_wheel)
  lazy val months = new DateFormatSymbols().getShortMonths
  lazy val formatter = new SimpleDateFormat("EEE, MMM d, yyyy")

  setView(view)
  setButton(ctx.getText(R.string.set), this)
  setButton2(ctx.getText(android.R.string.cancel), null : DialogInterface.OnClickListener)

  monthWheel.setViewAdapter(new ArrayWheelAdapter[String](ctx, months))
  yearWheel.setViewAdapter(new NumericWheelAdapter(ctx, 1900, 2200))
  updateDays()

  monthWheel.addChangingListener({updateDays(); updateTitle()})
  yearWheel.addChangingListener({updateDays(); updateTitle()})
  dayWheel.addChangingListener({updateTitle()})

  def year = yearWheel.getCurrentItem + 1900
  def monthOfYear = monthWheel.getCurrentItem
  def dayOfMonth = dayWheel.getCurrentItem + 1
  def year_=(year: Int) { yearWheel.setCurrentItem(year - 1900) }
  def monthOfYear_=(month: Int) { monthWheel.setCurrentItem(month) }
  def dayOfMonth_=(day: Int) { dayWheel.setCurrentItem(day - 1) }

  def onClick(dlg: DialogInterface, which: Int) {
    if (callback != null && which == DialogInterface.BUTTON_POSITIVE)
      callback.onDateSet(null, year, monthOfYear, dayOfMonth)
  }

  def updateTitle() {
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, monthOfYear)
    cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
    setTitle(formatter.format(cal.getTime))
  }

  def updateDays() {
    val cal = Calendar.getInstance
    cal.set(Calendar.YEAR, year)
    cal.set(Calendar.MONTH, monthOfYear)
    val maxdays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    dayWheel.setViewAdapter(new NumericWheelAdapter(ctx, 1, maxdays))
    val curday = maxdays min dayWheel.getCurrentItem
    dayWheel.setCurrentItem(curday - 1)
  }

  def this(ctx: Context, callback: OnDateSetListener, year: Int, monthOfYear: Int, dayOfMonth: Int) = {
    this(ctx, callback)
    this.year = year
    this.monthOfYear = monthOfYear
    this.dayOfMonth = dayOfMonth
    updateTitle()
  }
}