package com.ridewithbikes

import _root_.android.app.Activity
import _root_.android.os.Bundle
import android.graphics.Typeface

class BikeActivity extends Activity with TypedActivity {
  lazy val junction = Typeface.createFromAsset(getAssets, "fonts/Junction.otf")

  lazy val mainTitle = findView(TR.main_title)
  lazy val systemSpinner = findView(TR.system_spinner)

  override def onCreate(savedInstanceState: Bundle) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main)

    mainTitle.setTypeface(junction)
  }
}
