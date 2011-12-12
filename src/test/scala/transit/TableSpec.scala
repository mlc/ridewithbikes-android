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
import java.util.Calendar
import com.ridewithbikes.SpecHelper._
import Result._
import collection.immutable.List

class TableSpec extends Specification {
  "Generate Tables".title

  "The Subway" should {
    "be one simple line" in {
      val nyc = findSystem("NYC Subway")
      val table = nyc.friendly_table(Calendar.getInstance())
      matchable(table) must_== List(( 0, 00,   0, 00,  List(Yes)))
    }
  }

  "The PATH" should {
    "be simple on the weekend" in {
      val path = findSystem("PATH")
      val table = path.friendly_table(makeDate("Dec 11, 2011"))
      matchable(table) must_== List(( 0, 00,   0, 00,  List(Yes)))
    }

    "deal with rush hour on weekdays" in {
      val path = findSystem("PATH")
      val table = path.friendly_table(makeDate("Dec 12, 2011"))
      matchable(table) must_== List(( 0, 00,   6, 30,  List(Yes)),
                                    ( 6, 30,   9, 30,  List(No)),
                                    ( 9, 30,  15, 30,  List(Yes)),
                                    (15, 30,  18, 30,  List(No)),
                                    (18, 30,   0, 00,  List(Yes)))
    }
  }
  
  "Metro-North" should {
    "make one hate it" in {
      val mnr = findSystem("Metro-North")
      val table = mnr.friendly_table(makeDate("Dec 12, 2011"))
      matchable(table) must_== List(( 0, 00,   5, 00,  List(Yes, Yes)),
                                    ( 5, 00,   5, 30,  List(No, Yes)),
                                    ( 5, 30,   9, 00,  List(No, Maybe)),
                                    ( 9, 00,  10, 00,  List(No, Yes)),
                                    (10, 00,  15, 00,  List(Yes, Yes)),
                                    (15, 00,  16, 00,  List(Yes, Maybe)),
                                    (16, 00,  20, 00,  List(Yes, No)),
                                    (20, 00,  20, 15,  List(Yes, Maybe)),
                                    (20, 15,   0, 00,  List(Yes, Yes)))
    }
  }

  // reformats a table for more fun with matching
  private def matchable(t: List[(Calendar, Calendar, List[Result.Result])]) = {
    for ((start, finish, results) <- t)
      yield(start.get(Calendar.HOUR_OF_DAY), start.get(Calendar.MINUTE),
        finish.get(Calendar.HOUR_OF_DAY), finish.get(Calendar.MINUTE),
        results)
  }
}