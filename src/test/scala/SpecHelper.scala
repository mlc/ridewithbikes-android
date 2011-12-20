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
import com.ridewithbikes.transit.System
import java.util.{Calendar, Locale}
import java.text.SimpleDateFormat

object SpecHelper {
  val dateParser = new SimpleDateFormat("MMM d, yyyy", Locale.US)
  val timeParser = new SimpleDateFormat("h:mm a", Locale.US)
  val dateTimeParser = new SimpleDateFormat("MMM d, yyyy h:mm a", Locale.US)

  def makeDay(weekday: Int) = {
    val cal = Calendar.getInstance(Locale.US)
    cal.set(Calendar.DAY_OF_WEEK, weekday)
    cal
  }

  private def parse(fmt : SimpleDateFormat)(t : String) = {
    val cal = Calendar.getInstance(Locale.US)
    cal.setTime(fmt.synchronized { fmt.parse(t) })
    cal
  }

  def makeTime(s : String) = parse(timeParser)(s)
  def makeDate(s : String) = parse(dateParser)(s)
  def makeDateTime(s : String) = parse(dateTimeParser)(s)

  def makeNow = Calendar.getInstance(Locale.US)

  def findSystem(s: String) = {
    System.Systems.find(_.name.equals(s)) match {
      case None => throw new IllegalArgumentException("No such system " + s)
      case Some(sys) => sys
    }
  }
}