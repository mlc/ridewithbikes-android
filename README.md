Ride with Bikes for Android
===========================

This is a port of [Ride with Bikes] to the Android platform.

The code is written in scala. To compile, make sure you have a copy of [sbt]
(version 0.11 has been tested) and of the [Android SDK]. Then, set your
`ANDROID_SDK` enviroment variable to point to the Android SDK as installed,
 and do

    sbt android:package-debug

Would you like to run the tests? If so, try

    sbt test

Questions, comments, suggestions, and patches are all most welcome.

Copyright and Licensing
-----------------------

Copyright © 2011 Michael Castleman.

This program is free software. It comes without any warranty, to
the extent permitted by applicable law. You can redistribute it
and/or modify it under the terms of the [Do What The Fuck You Want
To Public License], Version 2, as published by Sam Hocevar.

Some of the libraries and assets used by this project are by other authors and
are under other licenses:

[android-wheel] is Copyright 2011 Yuri Kanivets and distributed under the
[Apache License], Version 2.0.

[Junction] is Copyright 2010 Caroline Hadilaksono and distributed under the
[Open Font License], Version 1.1.

[Ride with Bikes]: http://ridewithbikes.com/
[sbt]: https://github.com/harrah/xsbt/wiki/Getting-Started-Setup
[Android SDK]: http://developer.android.com/sdk/index.html
[Do What The Fuck You Want To Public License]: http://sam.zoy.org/wtfpl/COPYING
[android-wheel]: https://code.google.com/p/android-wheel/
[Apache License]: http://www.apache.org/licenses/LICENSE-2.0
[Junction]: http://www.theleagueofmoveabletype.com/junction
[Open Font License]: http://scripts.sil.org/OFL