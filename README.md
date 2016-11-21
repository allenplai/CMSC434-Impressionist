CMSC434-Impressionist
======================

An Android app that allows you to make a impressionist painting with a picture on your phone.

![demo](http://imgur.com/a/6Z8ll)



##Features
* Square Brush
* Line Brush
* Circle Splatter Brush
* Line Splatter Brush
* Particle Emitter Brush
* Clear painting
* Save painting to gallery photos

###Code
* **ImpressionistView.Java** is a custom view and is the canvas of where the user can draw the impressionist painting
* **MainActivity.Java** handles the all buttons on the button

###USAGE
Download, open, and run on Android studio

###References
Here is the list of references used to make this app

* Drawing to offscreen bitmap and highlights use of historical points 
  * https://github.com/jonfroehlich/CMSC434DrawTest - Skeleton code for how to draw to a offscreen Bitmap
* MotionEvents
  * https://developer.android.com/reference/android/view/MotionEvent.html
* Particle Emitter
  * http://stackoverflow.com/questions/26167972/drawing-random-circles-in-random-locations-in-random-sizes-android
* Velocity Tracker to make the brush size according to the velocity 
  * https://developer.android.com/reference/android/view/VelocityTracker.html


