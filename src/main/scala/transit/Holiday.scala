package com.ridewithbikes.transit

/*
 * Copyright © 2012 Michael Castleman.
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

import java.util.{Locale, Calendar}
import java.text.SimpleDateFormat
import Conversions._

object Holiday {
  val HOLIDAYS : Map[Symbol, Holiday] = Map(
    'NewYear -> new MonthDay(Calendar.JANUARY, 1),
    'Presidents -> new MonthWeekDow(Calendar.FEBRUARY, 3, Calendar.MONDAY),
    'StPatrick -> new MonthDay(Calendar.MARCH, 17),
    'GoodFriday -> new Lunar("4/10/2009", "4/2/2010", "4/22/2011", "4/6/2012", "3/29/2013", "4/18/2014"),
    'Easter -> new Lunar("4/12/2009", "4/4/2010", "4/24/2011", "4/8/2012", "3/31/2013", "4/20/2014"),
    'Mother -> new MonthWeekDow(Calendar.MAY, 2, Calendar.SUNDAY),
    'MemorialFri -> new MonthWeekDow(Calendar.MAY, -1, Calendar.FRIDAY, -3),
    'Memorial -> new MonthWeekDow(Calendar.MAY, -1, Calendar.MONDAY),
    'July3 -> new MonthDay(Calendar.JULY, 3),
    'July3Lirr -> new Holiday { def apply(c: Calendar) = {
      val m = c.get(Calendar.MONTH)
      val d = c.get(Calendar.DAY_OF_MONTH)
      m == Calendar.JULY && ((d == 3 && c.isWeekday) || (d < 4 && c.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY))
    } },
    'July4 -> new MonthDay(Calendar.JULY, 4),
    'LaborFri -> new MonthWeekDow(Calendar.SEPTEMBER, 1, Calendar.FRIDAY, -3),
    'Labor -> new MonthWeekDow(Calendar.SEPTEMBER, 1, Calendar.FRIDAY),
    'ErevRosh -> new Lunar("9/18/2009", "9/8/2010", "9/28/2011", "9/16/2012", "9/4/2013", "9/24/2014"),
    'ErevYom -> new Lunar("9/27/2009", "9/17/2010", "10/7/2011", "9/25/2012", "9/13/2013", "10/3/2014"),
    'Indigenous -> new MonthWeekDow(Calendar.OCTOBER, 2, Calendar.MONDAY),
    'Veterans -> new MonthDay(Calendar.NOVEMBER, 11),
    'ThanksgivingEve -> new MonthWeekDow(Calendar.NOVEMBER, 4, Calendar.WEDNESDAY, -1),
    'Thanksgiving -> new MonthWeekDow(Calendar.NOVEMBER, 4, Calendar.THURSDAY),
    'ThanksgivingFri -> new MonthWeekDow(Calendar.NOVEMBER, 4, Calendar.FRIDAY, 1),
    'ChristmasEve -> new MonthDay(Calendar.DECEMBER, 24),
    'Christmas -> new MonthDay(Calendar.DECEMBER, 25),
    'ChristmasWeek -> new Holiday { def apply(c: Calendar) = c.get(Calendar.MONTH) == Calendar.DECEMBER && c.get(Calendar.DAY_OF_MONTH) > 25 },
    'NewYearEve -> new MonthDay(Calendar.DECEMBER, 31)
  )
}

abstract class Holiday {
  def apply(c : Calendar) : Boolean
}

class MonthDay(month: Int, day: Int) extends Holiday {
  def apply(c : Calendar) = c.get(Calendar.MONTH) == month && c.get(Calendar.DAY_OF_MONTH) == day
}

class MonthWeekDow(month: Int, week: Int, dow: Int, offset: Int = 0) extends Holiday {
  val (first_possible, last_possible) =
    if (week == -1)
      (25 + offset, 31 + offset)
    else
      (1 + (week-1)*7 + offset, week*7 + offset)

  def apply(c : Calendar) = {
    val day = c.get(Calendar.DAY_OF_MONTH)
    val test_month = c.get(Calendar.MONTH)
    val test_dow = c.get(Calendar.DAY_OF_WEEK)
    val usual_result = (test_month == month && test_dow == dow && day >= first_possible && day <= last_possible)
    usual_result || (first_possible < 1 && test_dow == dow && test_month == (month-1) && day >= (first_possible + 31))
  }
}

object Lunar {
  val PARSER = new SimpleDateFormat("M/d/yyyy", Locale.US)
}

class Lunar(dates : String*) extends Holiday {
  lazy val cals = dates map(x => {
    val c : Calendar = Lunar.PARSER.parse(x)
    (c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH))
  })
  def apply(c : Calendar) = {
    val y = c.get(Calendar.YEAR)
    val m = c.get(Calendar.MONTH)
    val d = c.get(Calendar.DAY_OF_MONTH)
    cals exists { case (hy, hm, hd) => (hy == y && hm == m && hd == d) }
  }
}