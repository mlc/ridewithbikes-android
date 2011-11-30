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
import org.specs2.Specification
import java.text.SimpleDateFormat
import java.util.{Calendar, Locale}
import org.specs2.matcher.Matcher
import org.specs2.specification.{Then, Given}

class HolidaySpec extends Specification { def is =
  "Check the Holidays"                      ^
                                            p^
  // Simple holiday: July 4 every year
  "${July 4} should"                        ^ setHoliday ^
    "match ${July 4, 2011}"                 ^ holiday ^
    "not match ${July 5, 2011}"             ^ notHoliday ^
    "not match ${May 4, 2011}"              ^ notHoliday ^
    "match ${July 4, 2012}"                 ^ holiday ^
                                            endp^
  // 4th Thursday in November
  "${Thanksgiving} should"                  ^ setHoliday ^
    "match ${November 24, 2011}"            ^ holiday ^
    "not match ${November 18, 2011}"        ^ notHoliday ^
    "not match ${November 25, 2011}"        ^ notHoliday ^
    "not match ${October 28, 2011}"         ^ notHoliday ^
    "match ${November 22, 2012}"            ^ holiday ^
    "not match ${November 29, 2012}"        ^ notHoliday ^
                                            endp^
  // Day after 4th Thursday in November
  // (in e.g. 2013, not equal to 4th Friday in November)
  "${Thanksgiving Fri}day should"           ^ setHoliday ^
    "match ${November 25, 2011}"            ^ holiday ^
    "not match ${November 19, 2011}"        ^ notHoliday ^
    "not match ${November 26, 2011}"        ^ notHoliday ^
    "not match ${October 29, 2011}"         ^ notHoliday ^
    "not match ${November 22, 2013}"        ^ notHoliday ^
    "match ${November 29, 2013}"            ^ holiday ^
                                            endp^
  // last Monday in May
  // (in e.g. 2011, not equal to 4th Monday in May)
  "${Memorial} Day should"                  ^ setHoliday ^
    "match ${May 30, 2011}"                 ^ holiday ^
    "not match ${May 31, 2011}"             ^ notHoliday ^
    "not match ${May 23, 2011}"             ^ notHoliday ^
    "not match ${March 28, 2011}"           ^ notHoliday ^
    "match ${May 28, 2012}"                 ^ holiday ^
    "not match ${May 21, 2012}"             ^ notHoliday ^
                                            endp^
  // Friday before last Monday in May
  "${Memorial Fri}day should"               ^ setHoliday ^
    "match ${May 27, 2011}"                 ^ holiday ^
    "not match ${May 28, 2011}"             ^ notHoliday ^
    "not match ${May 20, 2011}"             ^ notHoliday ^
    "not match ${March 25, 2011}"           ^ notHoliday ^
    "match ${May 25, 2012}"                 ^ holiday ^
    "not match ${May 28, 2012}"             ^ notHoliday ^
                                            endp^
  // Friday before 1st Monday in Sep is sometimes in Aug!
  "${Labor Fri}day should"                  ^ setHoliday ^
    "match ${Sep 2, 2011}"                  ^ holiday ^
    "match ${Aug 31, 2012}"                 ^ holiday ^
    "not match ${Aug 28, 2015}"             ^ notHoliday ^
    "not match ${Sep 7, 2012}"              ^ notHoliday ^
                                            endp^
  // 1st Tishrei
  "${Erev Rosh} Hashana should"             ^ setHoliday ^
    "match ${Sep 28, 2011}"                 ^ holiday ^
    "not match ${Sep 29, 2012}"             ^ notHoliday ^
    "not match ${Sep 28, 2015}"             ^ notHoliday ^
    "match ${Sep 16, 2012}"                 ^ holiday ^
                                            endp^
  // Weekday before July 4
  "${July 3 Lirr} should"                   ^ setHoliday ^
    "not match ${Jun 3, 2011}"              ^ notHoliday ^
    "match ${Jul 1, 2011}"                  ^ holiday ^
    "not match ${Jul 3, 2011}"              ^ notHoliday ^
    "not match ${Jul 1, 2012}"              ^ notHoliday ^
    "match ${Jul 3, 2012}"                  ^ holiday ^
    "match ${Jul 2, 2021}"                  ^ holiday ^
                                            end

  object setHoliday extends Given[Symbol] {
    def extract(text: String) = Symbol(extract1(text).replaceAll(" +", ""))
  }

  class baseHoliday(beAsDesired : Matcher[Boolean]) extends Then[Symbol] {
    def extract(hol: Symbol, text: String) = {
      val cal = Calendar.getInstance(Locale.US)
      val d = extract1(text)
      cal.setTime(HolidaySpec.parser.parse(d))
      Holiday.HOLIDAYS(hol)(cal) must beAsDesired
    }
  }

  val holiday = new baseHoliday(beTrue)
  val notHoliday = new baseHoliday(beFalse)
}

object HolidaySpec {
  val parser = new SimpleDateFormat("MMM d, yyyy", Locale.US)
}