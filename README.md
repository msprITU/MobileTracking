# Mobile Tracking

In the context of this project state-of-the-art real-time deep object detectors are adopted to mobile phones. It is aimed to improve the stand alone performance of these algorithms by using data acquired by mobile sensors.

## Directories

- [**"MSPR-mobile"**](https://github.com/msprITU/MobileTracking/tree/master/MSPR-mobile) consists of number of video sequences recorded for performance evaluation. Ground truth data, raw data and data acquisition is included in the folder.

- [**"assets"**](https://github.com/msprITU/MobileTracking/tree/master/assets) folder contains *"DarkLabel"* annotation tool files as well as performance evaluation python notebook. 

- [**"sezai-source"**](https://github.com/msprITU/MobileTracking/tree/master/sezai-source) has two different source codes. 

    - One of them is an Android application java code, where TinyYOLOv3 is used on mobile device camera to detect a target object with in cooperate with accelerometer data. 

    - Other one is written in MATLAB; which used to report performance of TinyYOLOv3 on MSPR-mobile dataset, with accelerometer  . 


## Model Files

The model files that are used in this project are trained on COCO dataset. TinyYOLOv3 detector model file from the original source can be found [here.](https://pjreddie.com/media/files/yolov3-tiny.weights)

## MSPR-mobile 

The [dataset](https://www.youtube.com/playlist?list=PLMzonaXew-55493qE290Zo2Sp53DxTXrW) is released under the name of MSPR-mobile for public use. If you would like to download the groudtruth data please contact 
us. If you use the video dataset for your evaluations please cite the link of our data.  

## Performance Evaluation

Performance evaluation is done with this python notebook given [here.](https://github.com/msprITU/MobileTracking/blob/master/assets/Evaluate_Models.ipynb)

## Results

The results will be added in mspr YouTube account, [here.]()


