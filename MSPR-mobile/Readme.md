## MSPR-mobile

MSPR-mobile videos are captured and annotated by Multimedia Signal Processing and Pattern Recognition Group members listed below:

Ulas Tosun (June 2019),
Sezai Burak Kantarci (December 2019)

If you would like to download the groudtruth data please contact [us](mailto::gunselb@itu.edu.tr). If you use the video dataset for your evaluations please cite the [link](https://github.com/msprITU/MobileTracking) of our data.  

## Labeling the data

<p align="center">
  <img src="MSPR-mobile/assets/dl.png">
</p>

For labeling, a basic open sourced labeling tool "DarkLabel" is used. To generate ground truth data:

1. Download **"DarkLabel"** from their [website](https://darkpgmr.tistory.com/16). Open DarkLabel.exe, select **"Open Video File ..."**

2. Select your recorded video; double click to open it.

3. The video will be shown frame by frame; left click to draw a bounding box around the object. (You can use CTRL to adjust the bounding box)

4. After you draw the bounding box for the frame; click **"Next & Predict"**

5. When all frames are done; click **"Save GT.."**. Make sure you are selecting **"frame#,x,y,w,h"** as the configuration for the GT.txt data. 

6. Upload the GT data aswell as the video file.


You can watch a tutorial video [here.](https://www.youtube.com/watch?v=vbydG78Al8s)
