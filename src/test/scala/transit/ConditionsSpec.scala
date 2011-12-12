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

import org.specs2.mutable.Specification
import Condition._
import java.util.Calendar
import com.ridewithbikes.SpecHelper._

class ConditionsSpec extends Specification {
  "Check conditions".title

  "always" should {
    "return true" in { always(Calendar.getInstance()) must beTrue }
  }

  "never" should {
    "return false" in { never(Calendar.getInstance()) must beFalse }
  }

  "weekend" should {
    "be false on Wednesday" in { weekend(makeDay(Calendar.WEDNESDAY)) must beFalse}
    "be true on Saturday" in { weekend(makeDay(Calendar.SATURDAY)) must beTrue}
  }

  "weekday" should {
    "be true on Wednesday" in { weekday(makeDay(Calendar.WEDNESDAY)) must beTrue}
    "be false on Saturday" in { weekday(makeDay(Calendar.SATURDAY)) must beFalse}
  }

  "rush hour" should {
    "5:30 AM" >> notPathRush
    "6:30 AM" >> pathRush
    "7:30 AM" >> pathRush
    "9:30 AM" >> notPathRush
    "12:30 PM" >> notPathRush
    "7:30 PM" >> notPathRush
  }

  "holidays" should {
    "New Year" in { pathHolidays(makeDate("January 1, 2011")) must beTrue }
    "Thanksgiving" in { pathHolidays(makeDate("November 24, 2011")) must beTrue }
    "NOT July 4" in { pathHolidays(makeDate("July 4, 2011")) must beFalse }
  }

  "summer" should {
    "Apr 30, 2011" >> mustNotSummer
    "May 21, 2009" >> mustNotSummer
    "May 22, 2009" >> mustSummer
    "May 25, 2009" >> mustSummer
    "May 31, 2009" >> mustSummer
    "May 26, 2010" >> mustNotSummer
    "May 27, 2010" >> mustNotSummer
    "May 28, 2010" >> mustSummer
    "May 31, 2010" >> mustSummer
    "May 26, 2011" >> mustNotSummer
    "May 27, 2011" >> mustSummer
    "May 31, 2011" >> mustSummer
    "Jul 1, 2011" >> mustSummer
    "Sep 1, 2008" >> mustSummer
    "Sep 2, 2008" >> mustNotSummer
    "Sep 1, 2011" >> mustSummer
    "Sep 4, 2011" >> mustSummer
    "Sep 5, 2011" >> mustSummer
    "Sep 6, 2011" >> mustNotSummer
    "Oct 1, 2011" >> mustNotSummer

    "guard Jul 1, 2011" >> { summer(weekend)(makeDate("Jul 1, 2011")) must beFalse }
  }

  lazy val pathAmRush = rush_hour((6,30), (9,30))
  lazy val notPathRush = { t: String => pathAmRush(makeTime(t)) must beFalse }
  lazy val pathRush = { t: String => pathAmRush(makeTime(t)) must beTrue }

  lazy val pathHolidays = holiday('NewYear, 'Presidents, 'Memorial, 'Labor, 'Thanksgiving, 'Christmas)

  lazy val mustSummer = { t: String => summer(always)(makeDate(t)) must beTrue }
  lazy val mustNotSummer = { t: String => summer(always)(makeDate(t)) must beFalse }
}