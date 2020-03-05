## MainActivity.java

The main code for the Android application is given [here.](https://github.com/msprITU/MobileTracking/blob/master/sezai-source/MainActivity.java)

It uses mobile devices camera with a defined target object and detect objects with TinyYOLOv3 as well as accelerometer. 

## TinyYOLOv3_Accelerometer_Integrated.m

This matlab routine is to evaluate results of TinyYOLOv3 - Accelerometer integrated algorithm. 

It uses the GT data, Accelerometer data and TinyYOLOv3 detection results. 

Also to report the results; raw videos are used and the algorithm decision is plotted on the videos.
