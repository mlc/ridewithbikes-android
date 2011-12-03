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

import android.widget.TextView
import android.text.method.LinkMovementMethod

trait ClickableText {
  def makeClickable(tv: TextView) {
    tv.getMovementMethod match {
      case _: LinkMovementMethod => ()
      case _ => tv.setMovementMethod(LinkMovementMethod.getInstance())
    }
  }
}