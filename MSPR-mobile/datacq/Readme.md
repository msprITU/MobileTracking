## Data Acquisition

The MSPR-mobile dataset is made with "Video Recorder" application.

### Video Recorder

This application can be used in Android Devices, to record videos at 720p (1280x720), 30fps.

#### Getting Started

- First you need to enable developer settings for your mobile phone. There is a different route for every brand & model. Google "How to enable developer options for X" , X being your smartphone brand/model.

- After that you might need to open usb debugging, from the developer options. This step is not necessary, but if you cannot connect the device you might give it a try.

#### Installing

The app you are installing is called **"Video Recorder"**

The java file of this application can be accessed [here.](https://github.com/msprITU/MobileTracking/blob/master/MSPR-mobile/datacq/MainActivity.java) Also the apk can be found [here.](https://github.com/msprITU/MobileTracking/blob/master/MSPR-mobile/datacq/VideoRecorder.apk) Simply, download the apk file and open it.

- At the installing phase, android might give you a warning about not installing the apk, and might ask you to give permission first. If so; give permission for the apk to be installed, saying you trust this source.

#### Recording

After installation, open the app.

- It will ask for permission to use the camera, if that is the case, allow it.

- It has pretty simple user interface, just press "START" to start recording. When you press "STOP" the video would be recorded.


#### Groundtruth Labeling of Object BB's

After you have successfully recorded a video, you can use [DarkLabel](https://darkpgmr.tistory.com/16) for labeling.

