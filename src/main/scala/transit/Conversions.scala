/*
 * Copyright © 2011 Michael Castleman.
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
}

class CalendarWrapper(cal : Calendar) {
  def isWeekend = {
    val dow = cal.get(Calendar.DAY_OF_WEEK)
    dow == Calendar.SATURDAY || dow == Calendar.SUNDAY
  }
  def isWeekday = !isWeekend
  def atHourMinute(h : Int, m : Int) = {
    val instance = this.clone.asInstanceOf[Calendar]
    instance.set(Calendar.HOUR, h)
    instance.set(Calendar.MINUTE, m)
    instance.set(Calendar.SECOND, 0)
    instance
  }
  def atMinutesPastMidnight(minutes : Int) = atHourMinute(minutes / 60, minutes % 60)
}
