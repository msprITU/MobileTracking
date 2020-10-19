
## Data Acquisition

The MSPR-mobile dataset is made with "Sensor Camera" application.

### Sensor Camera - Video and Sensor data acquirer application for Android

This application can be used in Android Devices, to record videos at 720p (1280x720), and desired fps.

#### Installing

The app you are installing is called **"Sensor Camera"**
- First of all download latest version of "Sensor Camera.apk"
- At the installing phase, android might give you a warning about not installing the apk, and might ask you to give permission first. If so; give permission for the apk to be installed, saying you trust this source.
#### Recording

After installation, open the app.

- It will ask for permission to use the camera, if that is the case, allow it.
- Application will also ask for permission to use media storage, to record video and sensor data it is required.
- Go to settings and enable **"Save Accelerometer Data"** to enable accelerometer data recording.
- It has pretty simple user interface, just press "Start" to start recording. When you press "STOP" the video would be recorded.
- In order to **enable accelerometer data recording**, change recording FPS and adjust preview configurations click settings button below.

## Format
Sensor Camera application records videos as avi format.<br>
All sensor datas will be saved as;<br>
  frame_number,accX,accY,accZ,ms_between_two_frames<br>
Video and sensor data files will be created under **DCIM/SensorCamera**
