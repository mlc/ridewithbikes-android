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
import java.text.SimpleDateFormat
import java.util.{Calendar, Locale}

object SpecHelper {
  val dateParser = new SimpleDateFormat("MMM d, yyyy", Locale.US)
  val timeParser = new SimpleDateFormat("h:mm a", Locale.US)

  def makeDay(weekday: Int) = {
    val cal = Calendar.getInstance(Locale.US)
    cal.set(Calendar.DAY_OF_WEEK, weekday)
    cal
  }

  def makeTime(t: String) = {
    val cal = Calendar.getInstance(Locale.US)
    cal.setTime(timeParser.parse(t))
    cal
  }

  def makeDate(t: String) = {
    val cal = Calendar.getInstance(Locale.US)
    cal.setTime(dateParser.parse(t))
    cal
  }
}