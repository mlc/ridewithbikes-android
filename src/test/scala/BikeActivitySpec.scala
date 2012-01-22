/*
 * Copyright © 2012 Michael Castleman.
 *
 * This program is free software. It comes without any warranty, to
 * the extent permitted by applicable law. You can redistribute it
 * and/or modify it under the terms of the Do What The Fuck You Want
 * To Public License, Version 2, as published by Sam Hocevar. See
 * http://sam.zoy.org/wtfpl/COPYING for more details.
 */

package com.ridewithbikes

import com.github.jbrechtel.robospecs.RoboSpecs
import com.xtremelabs.robolectric.RobolectricConfig
import org.specs2.mock.Mockito

class BikeActivitySpec extends RoboSpecs with Mockito {
  args(sequential = true)

  override lazy val robolectricConfig = new RobolectricConfig(new java.io.File("src/main"))

  "onCreate" should {
    "set the content view" in {
      val activity = new BikeActivity
      activity.onCreate(null)
      activity.mainTitle must not beNull
    }
  }
}