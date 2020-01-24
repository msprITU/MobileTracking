# Mobile Tracking

In the context of this project state-of-the-art real-time deep object detectors are adopted to mobile phones. It is aimed to improve the stand alone performance of these algorithms by using data acquired by mobile sensors.

## Model Files

The model files that are used in this project are trained on COCO dataset. TinyYOLOv3 detector model file from the original source can be found [here.](https://pjreddie.com/media/files/yolov3-tiny.weights)

## MSPR-mobile 

We have recorded a number of video sequences for performance evaluation. Groundtruth labeling of object BBs is performed by [DarkLabel](https://darkpgmr.tistory.com/16). 

The dataset is released under the name of MSPR-mobile for public use. If you would like to download the groudtruth data please contact 
us. If you use the video dataset for your evaluations please cite the link of our data.  

## Performance Evaluation

Performance evaluation is done with this python notebook given [here.](https://github.com/msprITU/MobileTracking/blob/master/assets/Evaluate_Models.ipynb)
