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

import android.view.View
import android.widget.AdapterView.OnItemSelectedListener
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.widget.{TimePicker, DatePicker, AdapterView}
import kankan.wheel.widget.{WheelView, OnWheelChangedListener}

object Implicits {
  implicit def funToClickListener(f: => Any) = new View.OnClickListener {
    def onClick(p1: View) { f }
  }
  implicit def funToClickListener(f: View => Any) = new View.OnClickListener {
    def onClick(p1: View) { f(p1) }
  }
  implicit def funToAdapterViewListener(f: => Any) = new OnItemSelectedListener {
    def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long) { f }
    def onNothingSelected(p1: AdapterView[_]) {}
  }
  implicit def funToAdapterViewListener(f: (AdapterView[_], View, Int, Long) => Any) = new OnItemSelectedListener {
    def onItemSelected(parent: AdapterView[_], view: View, position: Int, id: Long) { f(parent, view, position, id) }
    def onNothingSelected(p1: AdapterView[_]) {}
  }
  implicit def funToOnDateSetListener(f: (Int, Int, Int) => Any) = new OnDateSetListener {
    def onDateSet(view: DatePicker, year: Int, monthOfYear: Int, dayOfMonth: Int) { f(year, monthOfYear, dayOfMonth) }
  }
  implicit def funToOnTimeSetListener(f: (Int, Int) => Any) = new OnTimeSetListener {
    def onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) { f(hourOfDay, minute) }
  }
  implicit def funToWheelChangedListener(f: => Any) = new OnWheelChangedListener {
    def onChanged(wheel: WheelView, oldValue: Int, newValue: Int) { f }
  }
  implicit def funToWheelChangedListener(f: WheelView => Any) = new OnWheelChangedListener {
    def onChanged(wheel: WheelView, oldValue: Int, newValue: Int) { f(wheel) }
  }
  implicit def funToWheelChangedListener(f: (WheelView, Int, Int) => Any) = new OnWheelChangedListener {
    def onChanged(wheel: WheelView, oldValue: Int, newValue: Int) { f(wheel, oldValue, newValue) }
  }
}