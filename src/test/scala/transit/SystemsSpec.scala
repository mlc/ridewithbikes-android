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
import com.ridewithbikes.SpecHelper._
import Result._
import org.specs2.specification.{Then, Given, GivenThen}

class SystemsSpec extends Specification { def is =
  "Systems"                                                  ^
                                                             p^
  "Trivial Systems"                                          ^
    "${NYC Subway} always ${Yes}"                            ! checkTrivial ^
    "${NYC Bus} always ${No}"                                ! checkTrivial ^
    "${NJ Transit Buses} always ${Maybe}"                    ! checkTrivial ^
                                                             endp^
  // the simplest nontrivial system
  "PATH"                                                     ^ setSystem ^
    "slug is ${path}"                                        ^ checkSlug ^
    "Saturday morning, ${March 26, 2011 7:30 AM}: ${Yes}"    ^ checkResult ^
    "Tuesday morning, ${March 22, 2011 7:30 AM}: ${No}"      ^ checkResult ^
    "${March 22, 2011 10:30 AM}: ${Yes}"                     ^ checkResult ^
    "${March 22, 2011 5:30 PM}: ${No}"                       ^ checkResult ^
    "${March 22, 2011 9:30 PM}: ${Yes}"                      ^ checkResult ^
    "Memorial Day, ${May 30, 2011 7:30 AM}: ${Yes}"          ^ checkResult ^
                                                             endp^
  "Metro-North"                                              ^ setSystem ^
    "slug is ${metro_north}"                                 ^ checkSlug ^
    "${Mar 29, 2011 12:00 PM} mid-day"                       ^ checkSystem("Yes") ^
    "${Mar 29, 2011 6:00 PM} PM rush"                        ^ checkSystem(ONLY_INBOUND) ^
    "${Mar 29, 2011 8:12 PM}"                                ^ checkSystem("inbound ok; maybe outbound") ^
    "${Mar 29, 2011 3:32 PM}"                                ^ checkSystem("inbound ok; maybe outbound") ^
    "${Mar 29, 2011 8:20 PM}"                                ^ checkSystem("Yes") ^
    "${Nov 25, 2011 3:13 PM} post-Thanksgiving"              ^ checkSystem(ONLY_INBOUND) ^
    "${Dec 29, 2011 8:20 PM} post-Xmas"                      ^ checkSystem(ONLY_INBOUND) ^
    "${Mar 29, 2011 5:15 AM} early rush"                     ^ checkSystem(ONLY_OUTBOUND) ^
    "${Mar 29, 2011 6:17 AM} bidi rush"                      ^ checkSystem("maybe outbound; inbound not ok") ^
    "${Mar 29, 2011 9:15 AM} late am rush"                   ^ checkSystem(ONLY_OUTBOUND) ^
    "${Nov 25, 2011 6:00 PM} post-thanksgiving pm rush"      ^ checkSystem("No") ^
    "${Mar 29, 2011 11:00 AM} late am"                       ^ checkSystem("Yes") ^
    "${Nov 25, 2011 11:00 AM} post-thanksgiving late am"     ^ checkSystem("No") ^
    "${Mar 17, 2011 12:00 PM} holiday"                       ^ checkSystem("No") ^
    "${Mar 29, 2011 12:30 PM} normal early afternoon"        ^ checkSystem("Yes") ^
    "${May 27, 2011 12:30 PM} fri before memorial day early afternoon" ^ checkSystem(ONLY_INBOUND) ^
    "${May 27, 2011 11:30 AM} fri before memorial day 11:30a" ^ checkSystem("Yes") ^
    "${Mar 26, 2011 6:00 PM} weekend"                        ^ checkSystem("Yes") ^
                                                             endp^
  "${Long Island Railroad} is NOT the right spelling"        ! misspelled ^
  "Long Island Rail Road"                                    ^ setSystem ^
    "slug is ${long_island_rail_road}"                       ^ checkSlug ^
    "${Mar 29, 2011 12:00 PM} mid-day"                       ^ checkSystem("Yes") ^
    "${Mar 29, 2011 6:30 AM} am rush"                        ^ checkSystem(ONLY_OUTBOUND) ^
    "${Mar 29, 2011 8:00 AM} am rush"                        ^ checkSystem(ONLY_OUTBOUND) ^
    "${Mar 29, 2011 5:00 PM} pm rush"                        ^ checkSystem(ONLY_INBOUND) ^
    "${Mar 29, 2011 7:00 PM} pm rush"                        ^ checkSystem(ONLY_INBOUND) ^
    "${Mar 26, 2011 6:30 AM} sat early am"                   ^ checkSystem("Yes") ^
    "${Mar 26, 2011 8:00 AM} sat am rush"                    ^ checkSystem(ONLY_OUTBOUND) ^
    "${Mar 26, 2011 5:00 PM} sat pm rush"                    ^ checkSystem(ONLY_INBOUND) ^
    "${Mar 26, 2011 7:00 PM} sat pm ok"                      ^ checkSystem("Yes") ^
    "${Mar 27, 2011 8:00 AM} sunday morning"                 ^ checkSystem("Yes") ^
    "${Mar 27, 2011 7:00 PM} sun weridness"                  ^ checkSystem(ONLY_OUTBOUND) ^
    "${Mar 27, 2011 9:00 PM} sunday"                         ^ checkSystem("Yes") ^
    "${Mar 27, 2011 11:00 PM} sun weridness"                 ^ checkSystem(ONLY_INBOUND) ^
    "${Jun 19, 2011 9:00 PM} montauk summer sun"             ^ checkSystem("outbound ok; maybe inbound") ^
    "${Jun 19, 2011 7:00 PM} montauk summer sun"             ^ checkSystem(ONLY_OUTBOUND) ^ // right?
    "${Mar 25, 2011 8:30 PM} normal fri"                     ^ checkSystem("Yes") ^
    "${Jun 17, 2011 8:30 PM} montauk summer fri"             ^ checkSystem("inbound ok; maybe outbound") ^
    "${Jun 18, 2011 12:00 PM} montauk summer sat"            ^ checkSystem("Maybe") ^
    "${Jun 18, 2011 8:00 AM} montauk summer sat am"          ^ checkSystem("maybe outbound; inbound not ok") ^
    "${Jun 18, 2011 5:00 PM} montauk summer sat pm"          ^ checkSystem("maybe inbound; outbound not ok") ^
    "${May 8, 2011 12:00 PM} mother's day"                   ^ checkSystem("No") ^
                                                             end

  val ONLY_INBOUND = "inbound ok, but not outbound"
  val ONLY_OUTBOUND = "outbound ok, but not inbound"

  object checkTrivial extends GivenThen {
    def extract(text: String) = {
      val (sysname, expected) = extract2(text)
      val sys = findSystem(sysname)
      sys(makeNow, null) must_== Result.withName(expected)
    }
  }

  object setSystem extends Given[System] {
    def extract(text: String) = findSystem(text)
  }

  object checkSlug extends Then[System] {
    def extract(sys: System, text: String) = sys.slug must_== extract1(text)
  }

  object checkResult extends Then[System] {
    def extract(sys: System, text: String) = {
      val (datestr, expected) = extract2(text)
      sys(makeDateTime(datestr), null) must_== Result.withName(expected)
    }
  }

  case class checkSystem(expected: String) extends Then[System] {
    def extract(sys: System, text: String) = {
      val datestr = extract1(text)
      sys.summarize(sys.allDirections(makeDateTime(datestr))) must beEqualTo(expected).ignoreCase
    }
  }

  object misspelled extends GivenThen {
    def extract(text: String) = findSystem(extract1(text)) must throwAn[IllegalArgumentException]
  }
}