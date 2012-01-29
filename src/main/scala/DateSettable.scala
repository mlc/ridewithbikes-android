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

import java.util.{Locale, Calendar}
import android.app.{TimePickerDialog, DatePickerDialog, AlertDialog}
import android.text.format.DateFormat
import android.content.{DialogInterface, Context}
import android.util.Log

trait DateSettable extends AlertDialog {
  def date_=(c: Calendar)
}

class SettableDatePickerDialog(context: Context, listener: DatePickerDialog.OnDateSetListener,
                               year: Int, monthOfYear: Int, dayOfMonth: Int)
  extends DatePickerDialog(context, listener, year, monthOfYear, dayOfMonth) with DateSettable {

  def date_=(c: Calendar) {
    updateDate(c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
  }
}

class SettableTimePickerDialog(context: Context, listener: TimePickerDialog.OnTimeSetListener,
                               val allDayCallback: () => Any,
                               hourOfDay: Int, minute: Int)
  extends TimePickerDialog(context, listener, hourOfDay, minute, DateFormat.is24HourFormat(context)) with DateSettable {

  setButton(DialogInterface.BUTTON_NEUTRAL, context.getString(R.string.all_day), this)
  def date_=(c: Calendar) {
    updateTime(c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
  }

  override def onClick(dialog: DialogInterface, which: Int) {
    Log.d("SettableTimePickerDialog", "onClick, which = " + which)
    which match {
      case DialogInterface.BUTTON_NEUTRAL =>
        if (allDayCallback != null)
          allDayCallback()
      case _ =>
        super.onClick(dialog, which)
    }
  }
}