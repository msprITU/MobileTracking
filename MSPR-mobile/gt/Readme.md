## Ground Truth Data

The ground truth data is generated using DarkLabel. For each mobile device brand, you can find the gt data for the recorded video as text files.

The configuration for GT texts are given as; **( frame number, x, y, w, h).**


## Data Annotation

<p align="center">
  <img src="https://github.com/msprITU/MobileTracking/blob/master/assets/dl.png">
</p>

For annotating the data, a basic open sourced labeling tool "DarkLabel" is used. To generate ground truth data:

1. Download **"DarkLabel"** from their [website](https://darkpgmr.tistory.com/16). Open DarkLabel.exe, select **"Open Video File ..."**

2. Select your recorded video; double click to open it.

3. The video will be shown frame by frame; left click to draw a bounding box around the object. (You can use CTRL to adjust the bounding box.)

4. After you draw the bounding box for the frame; click **"Next & Predict"**

5. When all frames are done; click **"Save GT.."**. Make sure you are selecting **"frame#,x,y,w,h"** as the configuration for the GT.txt data. 

You can watch a tutorial video [here.](https://www.youtube.com/watch?v=vbydG78Al8s)
