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

import Conversions._
import java.util.Calendar

object Condition {
  implicit def tupleToMinutes(t : (Int, Int)) = t match { case(h,m) => h*60 + m }
  type Condition = Calendar => Boolean

  val always = {c : Calendar => true}
  val never = {c : Calendar => false}
  val weekend = {c : Calendar => c.isWeekend}
  val weekday = {c : Calendar => c.isWeekday}
  val fridays = {c : Calendar => c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY}
  val saturdays = {c : Calendar => c.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY}
  val sundays = {c : Calendar => c.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY}
  def rush_hour(start_time : (Int,Int), end_time : (Int, Int), days : Condition = always) = {
    val start : Int = start_time
    val finish : Int = end_time
    c : Calendar => {
      val hm = c.get(Calendar.HOUR_OF_DAY)*60 + c.get(Calendar.MINUTE)
      hm >= start && hm < finish && days(c)
    }
  }
  def holiday(hs : Symbol*) = {
    val holidays = hs map Holiday.HOLIDAYS
    c : Calendar => { holidays exists (_(c)) }
  }
  def summer(filter : Condition)(c : Calendar) = { // Friday before Memorial Day through Labor Day
    if (!filter(c))
      false
    else {
      val month = c.get(Calendar.MONTH)
      if (month < Calendar.MAY || month > Calendar.SEPTEMBER)
        false
      else if (month > Calendar.MAY && month < Calendar.SEPTEMBER)
        true
      else {
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dow = c.get(Calendar.DAY_OF_WEEK)
        if (month == Calendar.MAY)
          day >= (dow+1)%7 + 22
        else { /* must be september */
          day <= (dow+4)%7 + 1
        }
      }
    }
  }
}