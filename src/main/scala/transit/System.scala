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

import Conversions._
import Condition._
import Result._
import annotation.tailrec
import collection.mutable.ListBuffer
import java.util.Calendar

object Direction extends Enumeration {
  type Direction = Value
  val Inbound, Outbound = Value
}
import Direction._

object System {
  final val TABLE_GRANULARITY = 15 // minutes
  
  protected def checker(result : Result)(condition : Condition, fdir : Direction = null)(date : Calendar, direction : Direction) = {
    if ((fdir == null || direction == fdir) && condition(date))
      result
    else
      null
  }
  protected val reject = checker(No) _
  protected val accept = checker(Yes) _
  protected val maybe  = checker(Maybe) _

  val Systems = Vector(
    new SimpleSystem("NYC Subway", 'train, accept(always, null)),
    new SimpleSystem("NYC Bus", 'bus, reject(always, null)),
    new SimpleSystem("Amtrak", 'train, reject(always, null)),
    new SimpleSystem("PATH", 'train,
      accept(weekend, null),
      accept(holiday('NewYear, 'Presidents, 'Memorial, 'Labor, 'Thanksgiving, 'Christmas), null),
      reject(rush_hour(( 6, 30), ( 9, 30)), null),
      reject(rush_hour((15, 30), (18, 30)), null),
      accept(always, null)
    ),
    new BidiSystem("Metro-North", 'train,
      reject(holiday('NewYear, 'StPatrick, 'Mother, 'ErevRosh, 'ErevYom, 'ThanksgivingEve, 'Thanksgiving, 'ChristmasEve, 'NewYearEve), null),
      reject(rush_hour((12, 00), (20, 30), holiday('MemorialFri, 'July3, 'LaborFri)), Outbound),
      accept(weekend, null),
      reject(rush_hour((16, 00), (20, 00)), Outbound),
      reject(rush_hour(( 5, 30), (12, 00), holiday('ThanksgivingFri, 'ChristmasWeek)), Outbound),
      reject(rush_hour((15, 00), (20, 30), holiday('ThanksgivingFri, 'ChristmasWeek)), Outbound),
      maybe (rush_hour(( 5, 30), ( 9, 00)), Outbound),
      maybe (rush_hour((15, 00), (16, 00)), Outbound),
      maybe (rush_hour((20, 00), (20, 15)), Outbound),
      reject(rush_hour(( 5, 00), (10, 00)), Inbound),
      // “and on other trains identified in Metro-North Timetables”
      reject(rush_hour(( 5, 00), (12, 00), holiday('ThanksgivingFri, 'ChristmasWeek)), Inbound),
      reject(rush_hour((16, 00), (20, 00), holiday('ThanksgivingFri, 'ChristmasWeek)), Inbound),
      accept(always, null)
    ),
    new BidiSystem("Long Island Rail Road", 'train,
      reject(holiday('NewYear, 'StPatrick, 'Mother, 'GoodFriday, 'Easter, 'MemorialFri, 'Memorial, 'July3Lirr, 'July4, 'ErevRosh, 'ErevYom, 'LaborFri, 'Labor, 'Indigenous, 'ThanksgivingEve, 'Thanksgiving, 'ThanksgivingFri, 'ChristmasEve, 'Christmas, 'NewYearEve), null),
      // also: “Special Events - including Belmont and Mets-Willets Point
      // trains, US Golf Open or NYC parade day trains, the ‘Montauk Century’
      // and the ‘Ride to Montauk’ annual events.”
      reject(rush_hour(( 6, 00), (10, 00), weekday), Inbound),
      reject(rush_hour((15, 00), (20, 00), weekday), Outbound),
      reject(rush_hour(( 7, 00), (10, 00), saturdays), Inbound),
      reject(rush_hour((16, 00), (18, 00), saturdays), Outbound),
      reject(rush_hour((17, 00), (20, 00), sundays), Inbound),
      reject(rush_hour((22, 00), (24, 00), sundays), Outbound),
      maybe (rush_hour((18, 00), (22, 00), summer(sundays)), Inbound),
      maybe (rush_hour((15, 00), (21, 00), summer(fridays)), Outbound),
      maybe (summer(saturdays), null),
      accept(always, null)
    ),
    new BidiSystem("NJ Transit Trains", 'train,
      reject(holiday('NewYear, 'Memorial, 'July3, 'July4, 'Labor, 'ErevRosh, 'ErevYom, 'ThanksgivingEve, 'Thanksgiving, 'ThanksgivingFri, 'ChristmasEve, 'Christmas, 'NewYearEve), null),
      accept(weekend, null),
      reject(rush_hour(( 6, 00), (10, 00)), Inbound),
      reject(rush_hour((16, 00), (19, 00)), Outbound),
      accept(always, null)
    ),
    new SimpleSystem("NJ Transit Buses", 'bus, maybe(always, null)),
    new SimpleSystem("Staten Island Ferry", 'ferry, accept(always, null))
  ).sorted

  protected def friendly(table : List[(Calendar, List[Result])]) = {
    val builder = new ListBuffer[(Calendar, Calendar, List[Result])]()

    @tailrec def build(t : List[(Calendar, List[Result])]) {
      t match {
      case Nil => ()
      case (head :: tail) =>
        val (matches, rest) = t span (p => p._2 == head._2)
        builder += ((head._1, matches.last._1 + TABLE_GRANULARITY.minutes, head._2))
        build(rest)
      }
    }

    build(table)
    builder.toList
  }
}

abstract class System(val name : String, val icon : Symbol, funs : ((Calendar, Direction) => Result)*) extends Object with Comparable[System] {
  require(name != null)

  def apply(date : Calendar, direction : Direction) = {
    (funs find (_(date, direction) != null)) match {
      case None => throw new RuntimeException("system got to the end without returning a result")
      case Some(f) => f(date, direction)
    }
  }

  def compareTo(that: System) = name.compareTo(that.name)

  val directions : List[Direction]
  def allDirections(date: Calendar) = directions map (apply(date, _))

  def table(date: Calendar) = {
    val times = (0 until 24*60 by System.TABLE_GRANULARITY) map (date.atMinutesPastMidnight(_))
    val results = times map allDirections
    (times zip results).toList
  }

  def friendly_table(date: Calendar) = {
    val base = table(date)
    System.friendly(base)
  }

  def summarize(results : List[Result]) = {
    // um. too magical?
    val groups = (directions zip results) groupBy (_._2) map {case (a,b) => (a,b map {_._1})}

    def commaed(r: Result) = groups(r).mkString(", ")

    groups.size match {
      case 0 => throw new IllegalArgumentException("wtf?")
      case 1 => groups.head._1.toString
      case _ => {
        if (groups.contains(Maybe)) {
          if (groups.contains(Yes)) {
            commaed(Yes) + " ok; maybe " + commaed(Maybe)
          } else {
            "maybe " + commaed(Maybe) + "; " + commaed(No) + " not ok"
          }
        } else {
          commaed(Yes) + " ok, but not " + commaed(No)
        }
      }
    }
  }

  def summarize(date: Calendar) : String = summarize(allDirections(date))

  def slug = name.toLowerCase.replaceAll("[^a-z0-9]+", "_")

  override def toString = name
}

class SimpleSystem(name : String, icon : Symbol, funs : ((Calendar, Direction) => Result)*)
  extends System(name, icon, funs : _*) {
  val directions = List(null)
}

class BidiSystem(name : String, icon : Symbol, funs : ((Calendar, Direction) => Result)*)
  extends System(name, icon, funs : _*) {
  val directions = List(Inbound, Outbound)
}
