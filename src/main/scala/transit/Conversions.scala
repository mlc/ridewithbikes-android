/*
 * Copyright © 2012 Michael Castleman.
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package com.ridewithbikes.transit

import java.util.{Locale, Calendar, Date}

object Conversions {
  implicit def dateToCalendar(d : Date) : Calendar = {
    val instance = Calendar.getInstance(Locale.US)
    instance.setTime(d)
    instance
  }

  implicit def wrapCalendar(cal : Calendar) = new CalendarWrapper(cal)
  implicit def unwrapCalendar(cw: CalendarWrapper) = cw.cal
  implicit def wrapSeconds(s : Int) = new SecondsWrapper(s)
}

class CalendarWrapper(val cal : Calendar) {
  def isWeekend = {
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
  }
  def isWeekday = !isWeekend
  def atHourMinute(h : Int, m : Int) = {
    val instance = cal.clone.asInstanceOf[Calendar]
    instance.set(Calendar.HOUR_OF_DAY, h)
    instance.set(Calendar.MINUTE, m)
    instance.set(Calendar.SECOND, 0)
    instance
  }
  def atMinutesPastMidnight(minutes : Int) = atHourMinute(minutes / 60, minutes % 60)
  def +(seconds: Int) = {
    val instance = cal.clone().asInstanceOf[Calendar]
    instance.add(Calendar.SECOND, seconds)
    instance
  }
  def +=(seconds: Int) = {
    cal.add(Calendar.SECOND, seconds)
    cal
  }
}

class SecondsWrapper(val s : Int) {
  def seconds = s
  def second = seconds
  def minutes = s * 60
  def minute = minutes
  def hours = s * 3600
  def hour = hours
  def days = s * 86400
  def day = days
  def to_ms = s * 1000L
}