package com.example.realtimetinyyolov3;


// Developed by: Sezai Burak Kantarci, December 2019
// For any question you can e-mail to : sezaiburakkantarci@gmail.com


// UPDATE = 27.11.2019 TRYING 480P , LINE 275
// last decided on 854*480

// UPDATE LINE 408, trying to log accelerometer first.
// it saved everything. it should save only when predicted. Needs more work.

// Android libraries
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
//import android.support.constraint.solver.widgets.Rectangle;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.os.Environment;
import android.view.WindowManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Button;
import android.view.View;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;

// OpenCV libraries
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfInt4;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.CvType;
import org.opencv.utils.Converters;
import org.opencv.videoio.Videoio;

// Java libraries
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Calendar;
import java.util.List;
import java.util.Vector;

public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, SensorEventListener {

    private static final String TAG = "TinyYOLOv3";


    private static final String[] classNames = {"person", "bicycle", "motorbike", "airplane", "bus", "train",
            "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "car", "bench",
            "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe",
            "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard",
            "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard",
            "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl",
            "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza",
            "doughnut", "cake", "chair", "sofa", "potted plant", "bed", "dining table", "toilet",
            "TV monitor", "laptop", "computer mouse", "remote control", "keyboard", "cell phone",
            "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase",
            "pair of scissors", "teddy bear", "hair drier", "toothbrush"};


    private Net net;    // the neural network
    private CameraBridgeViewBase mOpenCvCameraView;
    private boolean isStarted = false;
    private String targetClass;
    private float alpha;

    private Point lp = new Point(0, 0);
    private Point udp = new Point(0, 0);
    private Point cdp = new Point(0, 0);
    private Point pp = new Point(0, 0);
    private double lw = 0;
    private double lh = 0;

    // accelerometer
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // accelerometer data
    float[] accelerometerData = new float[3];

    // velocity and position data
    float[] camVel = new float[3];
    float[] camPos = new float[3];
    float[] cpt = new float[3];

    // The frame to be processed
    private Mat frame;

    // File Name
    String fileName;

    // time data
    long previousTime = 0L;
    long currentTime = 0L;
    long deltaTimeInMs = 0L;
    float deltaTime = 0f;
    boolean isFirstTimeRead = true;
    boolean isFound = false;
    private float totalTime;
    private int periodCounter = 0;
    long currentTimeFPS = 0L;
    long lastTimeFPS = 0L;
    float fps = 0f;


    // edit fields
    private EditText editAlpha;
    private EditText editTargetClass;
    private EditText changeAlpha;
    private EditText changeTargetClass;
    private TextView FPSmeter;


    // Initialize OpenCV manager.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    break;
                }
                default: {
                    super.onManagerConnected(status);
                    break;
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  accelerometer
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        editTargetClass = (EditText) findViewById(R.id.edit_target_class);
        editAlpha = (EditText) findViewById(R.id.edit_alpha);
        FPSmeter = (TextView) findViewById(R.id.textFPS);
        changeTargetClass = (EditText) findViewById(R.id.change_target_class);
        changeAlpha = (EditText) findViewById(R.id.change_alpha);

        // Reset button initializations
        final Button resetButton = findViewById(R.id.reset);
        resetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetMotionData();
            }
        });

        resetButton.setEnabled(false);
        resetButton.setClickable(false);
        resetButton.setVisibility(View.GONE);

        FPSmeter.setEnabled(false);
        FPSmeter.setClickable(false);
        FPSmeter.setVisibility(View.GONE);

        changeTargetClass.setEnabled(false);
        changeTargetClass.setClickable(false);
        changeTargetClass.setVisibility(View.GONE);

        changeAlpha.setEnabled(false);
        changeAlpha.setClickable(false);
        changeAlpha.setVisibility(View.GONE);

        final Button quitButton = findViewById(R.id.quit);
        quitButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View d) {
                MainActivity.this.finishAffinity();
                System.exit(0);
            }
        });

        quitButton.setEnabled(true);
        quitButton.setClickable(true);
        quitButton.setVisibility(View.VISIBLE);


        // Start button initializations
        final Button startButton = findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // no input handling
                if (editTargetClass.getText().toString().equals("") || editAlpha.getText().toString().equals(""))
                    return;

                targetClass = editTargetClass.getText().toString();
                alpha = Float.parseFloat(editAlpha.getText().toString());

                startButton.setEnabled(false);
                startButton.setClickable(false);
                startButton.setVisibility(View.GONE);

                editAlpha.setEnabled(false);
                editAlpha.setClickable(false);
                editAlpha.setVisibility(View.GONE);

                editTargetClass.setEnabled(false);
                editTargetClass.setClickable(false);
                editTargetClass.setVisibility(View.GONE);

                resetButton.setEnabled(true);
                resetButton.setClickable(true);
                resetButton.setVisibility(View.VISIBLE);

                FPSmeter.setEnabled(true);
                FPSmeter.setClickable(true);
                FPSmeter.setVisibility(View.VISIBLE);

                changeTargetClass.setEnabled(true);
                changeTargetClass.setClickable(true);
                changeTargetClass.setVisibility(View.VISIBLE);
                changeTargetClass.setText(targetClass);

                changeAlpha.setEnabled(true);
                changeAlpha.setClickable(true);
                changeAlpha.setVisibility(View.VISIBLE);
                changeAlpha.setText(String.format("%.0f", alpha));

                isStarted = true;

            }
        });

        // Switch to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Check for camera access permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "Camera access permission is granted");
            // Set up camera listener.
            mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
            //mOpenCvCameraView.enableFpsMeter();
            mOpenCvCameraView.setMaxFrameSize(854, 480);
            mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
            mOpenCvCameraView.setCvCameraViewListener(this);
        }
        // Ask for camera access permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }

        // Check for writing external storage permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            Log.i(TAG, "Writing external memory permission is granted");
        }
        // Ask for writing external storage permission if needed
        else
            {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        // Check for OpenCV libraries
        if (!OpenCVLoader.initDebug()) {
            Log.i(TAG, "OpenCv Libraries are not loaded");
            // If OpenCV libraries is not loaded, trying to load it again
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.i(TAG, "OpenCv Libraries are loaded successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }

        // sensor registration
        sensorManager.registerListener(this, accelerometer, 5, 1);

        // Initialize network after loading OpenCV libraries
        String cfg = getPath("yolov3-tiny.cfg", this);
        String weights = getPath("yolov3-tiny.weights", this);

        // read the network
        net = Dnn.readNetFromDarknet(cfg, weights);
        Log.i(TAG, "Network loaded successfully");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {

            // The permission with the request code 0 (camera access)
            case 0: {
                // If request is cancelled, the result arrays are empty.
                // Permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Camera access permission is granted");
                    Toast.makeText(this, "Camera access permission is granted", Toast.LENGTH_SHORT).show();
                    // Set up camera listener.
                    mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
                    //mOpenCvCameraView.enableFpsMeter();
                    mOpenCvCameraView.setMaxFrameSize(1280, 720);
                    mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
                    mOpenCvCameraView.setCvCameraViewListener(this);
                }
                // Permission is denied
                else {
                    Log.i(TAG, "Camera access permission is denied");
                    Toast.makeText(this, "Camera access permission is denied", Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }

            // The permission with the request code 1 (writing external storage)
            case 1: {
                // If request is cancelled, the result arrays are empty.
                // Permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Writing external memory permission is granted");
                }
                // Permission is denied
                else {
                    Log.i(TAG, "Writing external memory permission is denied");
                }
                return;
            }
        }
    }

    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // take accelerometer data
            accelerometerData = event.values;

            // calculate delta time between two measurement
            currentTime = Calendar.getInstance().getTimeInMillis();
            deltaTimeInMs = (long)(currentTime - previousTime);
            deltaTime = ((float)(int)(long) deltaTimeInMs) / 1000f;
            previousTime = currentTime;


            if(!isFirstTimeRead) {
                totalTime = totalTime + deltaTime;
                periodCounter = periodCounter + 1;

                if (periodCounter != 3)
                    return;

                periodCounter = 0;

                // thresholding (filtering small noisy values)
                if (Math.abs(accelerometerData[0]) < 0.01f)
                    accelerometerData[0] = 0f;
                if (Math.abs(accelerometerData[1]) < 0.01f)
                    accelerometerData[1] = 0f;
                if (Math.abs(accelerometerData[2]) < 0.01f)
                    accelerometerData[2] = 0f;

                // calculate velocity
                camVel[0] = camVel[0] + accelerometerData[0] * totalTime;
                camVel[1] = camVel[1] + accelerometerData[1] * totalTime;
                camVel[2] = camVel[2] + accelerometerData[2] * totalTime;

                // calculate position in meters
                camPos[0] = camPos[0] + camVel[0] * totalTime;
                camPos[1] = camPos[1] + camVel[1] * totalTime;
                camPos[2] = camPos[2] + camVel[2] * totalTime;

                // calculate position in pixels
                cpt[0] = camPos[0] * alpha;
                cpt[1] = camPos[1] * alpha;
                cpt[2] = camPos[2] * alpha;

                // reset total time
                totalTime = 0f;

                String acctoLog = "";

                try {
                    FileOutputStream logFile = new FileOutputStream(new File(Environment.getExternalStorageDirectory()
                            + "/" + Environment.DIRECTORY_DCIM + "/VideoSource/" + fileName + "_acctesting.txt"), true);

                    acctoLog = Integer.toString(periodCounter) + "," + Float.toString(cpt[0]) + "," +
                            Float.toString(cpt[1]) + "," + Float.toString(cpt[2]) + "," +
                            Float.toString(deltaTime)+ "\n";

                    logFile.write((acctoLog).getBytes());
                    logFile.close();

                    Log.i(TAG, acctoLog);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            if(isFirstTimeRead)
                isFirstTimeRead = false;

        }
    }

    public void onCameraViewStarted(int width, int height) {
        //frameToSave = new Mat(width, height, CvType.CV_8UC4);  // setting sizes and data format of the matrix BGRA
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {


/*      YOLO
        Time to calculate inference time
        long previousTime = Calendar.getInstance().getTimeInMillis();

        // What is the time now ?
        long currentTime;

        // Time taken to run the code.
        long inferenceTime;


        // ADDED FROM YOLO
        //The neural network
        //private Net net;
        // FRAME DEFINED AT THE TOP
        // Info about the input video
        int totalFameNumber;
        int frameHeight;
        int frameWidth;*/

        if (!isStarted)
            return inputFrame.rgba();

        targetClass = changeTargetClass.getText().toString();
        alpha = Float.parseFloat(changeAlpha.getText().toString());

        currentTimeFPS = Calendar.getInstance().getTimeInMillis();
        deltaTimeInMs = (long) (currentTimeFPS - lastTimeFPS);
        fps = 1f / (((float) (int) (long) (currentTimeFPS - lastTimeFPS)) / 1000f);
        lastTimeFPS = currentTimeFPS;

        // set FPS
        FPSmeter.setText("FPS: " + String.format("%.1f", fps));

        // gol yedik
        isFound = false;

        final int IN_WIDTH = 416;
        final int IN_HEIGHT = 416;
        final float WH_RATIO = (float) IN_WIDTH / IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.00392;
        final double MEAN_VAL = 0;
        //float THRESHOLD = alpha;   // confidence threshold

        // gol yedik
        final float THRESHOLD = 0.5f;

        //YOLO

        // Get a new frame
        Mat frame = inputFrame.rgba();

        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2RGB);

        // Forward image through network.
        Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR, new Size(IN_WIDTH, IN_HEIGHT),
                new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false, false);

        net.setInput(blob);


        //*************************************************************************


// WORKING SLOW YOLO




        //REDUCTION POINT KIRILMA NOKTASI

        java.util.List<Mat> result = new java.util.ArrayList<Mat>(2);

        // We make this list of 2 values, of two YOLO layers
        List<String> outBlobNames = new java.util.ArrayList<>();
        outBlobNames.add(0, "yolo_16");
        outBlobNames.add(1, "yolo_23");

        //****************************************************

        // For big YOLO there are 3 layers? if you need to run big yolo just add the element
        // The tinyYolov3 layers we are looking for is from outBlobNames, and we define where to put them; which is "result"
        net.forward(result, outBlobNames);

        // From this point forward, its all over the place. Different Sources for the code.
        // This is the process that post-processes the output and draws the rectangles
        // Confidence Threshold
        // float confThreshold = 0.3f;


        // List of Class ID's
        List<Integer> clsIds = new ArrayList<>();
        // List of Confidences
        List<Float> confs = new ArrayList<>();
        // List of Rectangles, the coordinates of the bounding boxes
        List<Rect> rects = new ArrayList<>();

        List<Rect> rectsSecond = new ArrayList<>();

        List<Rect> rectsThird = new ArrayList<>();

        List<Rect> rectsFourth = new ArrayList<>();


        // Iterate over the size of the list
        // If this is running for small YOLO, 2 layers here
        // If this runs on big YOLO, 3 layers here
        // Simply, for our case; this is just 2.

        for (int i = 0; i < result.size(); ++i) {

            // level is just the yololayer for each case.
            // level = yolo_16 , for the first one
            // level = yolo_23 , for the second one

            Mat level = result.get(i);


            // Each row is a candidate detection, the 1st 4 numbers are
            // [center_x, center_y, width, height], followed by (N-4) class probabilities

            // Iterate over each of it's rows, of yolo layers.
            for (int j = 0; j < level.rows(); ++j) {

                // We access a row.
                // Still same thing, yolo_16 or yolo_23
                Mat row = level.row(j);

                // We define scores as level without the first 5 neurons(4 for bbox, and 1 for objectness score)
                // Which is how likely there is something in the bbox,and what's left are 1,2,3...80 coco classes or
                // however your custom model has, the max value location there will be
                // the class , and max value - conf

                // scores is defined with the row, from 5 to end of the level line.
                Mat scores = row.colRange(5, level.cols());

                // Defined from OpenCV , scores, used for finding max score
                // The confidence will be the max value of that guy

                // Core is a openCV class.
                // For every number in "scores", apart from the first 5, i need the max value.
                // Which will be mm.

                Core.MinMaxLocResult mm = Core.minMaxLoc(scores);

                // For 80 Neurons you get different values, we use the highest one.
                float confidence = (float) mm.maxVal;

                // We determine the class with class.id, which has the highest value
                // Defined with maxLoc.
                // The one that has the highest number, is defined as classIdPoint
                // For example; highest one is 48th and its "a bird" or something, from the classIds list.
                Point classIdPoint = mm.maxLoc;


                // The way YOLO predicts the outputs, first off all it predicts in percentage values
                // in a frame, exactly middle, 0.5x , 0.5y
                // At first we extract those percentages and convert to pixels,
                // If it's bigger than threshold than we will process it

                //THIS CHANGED. it was confThreshold now THRESHOLD

                if (confidence > THRESHOLD) {

                    // This was good the way that it is, but we need pixels.
                    int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                    int centerY = (int) (row.get(0, 1)[0] * frame.rows());

                    int width = (int) (row.get(0, 2)[0] * frame.cols());
                    int height = (int) (row.get(0, 3)[0] * frame.rows());


                    // Possible addition here. If we need to make the pixels that is always positive or zero
                    // we might need to add the condition saying, if it is negative; make it zero.


                    //We calculate the top-left point coordinates

                    // gol yerken
                    // left bottom bu.


                    // top left

                    int left = centerX - width / 2;    //left
                    int top = centerY - height / 2;    //top

                    // top right

                    int rightSmart = left + width;
                    int topSmart = top;

                    //bottom right

                    int left2 = centerX + width / 2;   //right
                    int top2 = centerY + height / 2;   //bottom

                    //bottom left

                    int left2Smart = left2 - width;
                    int top2Smart = top2;

                    //need bottomleft x,y

                    // Possible problem here. Wouldn't it be bottom left point, if we subtract the height?


                    // Each classIdPoint is added to the clsIds list
                    clsIds.add((int) classIdPoint.x);

                    // Each confidence value is added to the confs list.
                    confs.add((float) confidence);

                    // Remember, we are still in the if, so if confidence is higher than threshold, we pass the values to rects.
                    // We construct the rect object
                    // And we add it to the rects list.

                    rects.add(new Rect(left, top, width, height));  //topleft

                    rectsSecond.add(new Rect(left2,top2,width,height));    //bottomright

                    rectsThird.add(new Rect(rightSmart, topSmart, width, height));  //bottom left point

                    rectsFourth.add(new Rect(left2Smart, top2Smart, width,height));  // topright point

                }
            }
        }

        // How many of those rows went through 2 YOLO layers and was added to the confs list.
        int ArrayLength = confs.size();

        if (ArrayLength >= 1) {

            // Apply non-maximum suppression procedure.
            // What is max suppression ? For example for a mobile phone, there may be 2 BBOXes for the same object
            // With max suppression, it decides which boxes overlap, chose the one with highest confidence

            // Non maximum suppression threshold
            float nmsThresh = 0.2f;


            // Pre-process for non maximum suppression

            // Convert the vector float to matrix to get confidences to matrix of floats; from confs list.
            MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));

            // Define boxesArray, from rects list values.
            Rect[] boxesArray = rects.toArray(new Rect[0]);

            Rect[] boxesArray2 = rectsSecond.toArray(new Rect[0]);

            Rect[] boxesArray3 = rectsThird.toArray(new Rect[0]);

            Rect[] boxesArray4 = rectsFourth.toArray(new Rect[0]);

            //DEVELOPER MODE

            // Hmm. We defined a rect[] i guess boxes should be a MatOfRect
            MatOfRect boxes = new MatOfRect(boxesArray);

            MatOfInt indices = new MatOfInt();



            // Define the NMSBoxes from our values that we calculated
            // Performs non maximum suppression given boxes and corresponding scores.
            Dnn.NMSBoxes(boxes, confidences, THRESHOLD, nmsThresh, indices);


            // Draw result boxes.
            // The ones surpass the nms are indices , "ind"
            // Basically these are the best boxes to represent each object that is found in the frame.

            // Define ind array, from indices.
            int[] ind = indices.toArray();

            for (int i = 0; i < ind.length; ++i) {


                int idx = ind[i];

                Rect boxFourth = boxesArray4[idx];

                Rect boxThird = boxesArray3[idx];

                Rect boxSecond = boxesArray2[idx];

                Rect box = boxesArray[idx];

                int idGuy = clsIds.get(idx);

                float conf = confs.get(idx);

                if (!classNames[idGuy].equals(targetClass))
                    continue;

                // gol yedik
                isFound = true;

                //List<String> cocoNames = Arrays.asList("a person", "a bicycle", "a motorbike", "an airplane", "a bus", "a train", "a truck", "a boat", "a traffic light", "a fire hydrant", "a stop sign", "a parking meter", "a car", "a bench", "a bird", "a cat", "a dog", "a horse", "a sheep", "a cow", "an elephant", "a bear", "a zebra", "a giraffe", "a backpack", "an umbrella", "a handbag", "a tie", "a suitcase", "a frisbee", "skis", "a snowboard", "a sports ball", "a kite", "a baseball bat", "a baseball glove", "a skateboard", "a surfboard", "a tennis racket", "a bottle", "a wine glass", "a cup", "a fork", "a knife", "a spoon", "a bowl", "a banana", "an apple", "a sandwich", "an orange", "broccoli", "a carrot", "a hot dog", "a pizza", "a doughnut", "a cake", "a chair", "a sofa", "a potted plant", "a bed", "a dining table", "a toilet", "a TV monitor", "a laptop", "a computer mouse", "a remote control", "a keyboard", "a cell phone", "a microwave", "an oven", "a toaster", "a sink", "a refrigerator", "a book", "a clock", "a vase", "a pair of scissors", "a teddy bear", "a hair drier", "a toothbrush");

                // The number that is pressed
                int intConf = (int) (conf * 100);

                //FROM SSD
                String label = classNames[idGuy] + ": " + String.format("%.2f", conf);


                // The class text
                // Imgproc.putText(frame, cocoNames.get(idGuy) + " " + intConf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                // Imgproc.putText(frame, label + " " + conf + "%", box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);
                Imgproc.putText(frame, label + " " , box.tl(), Core.FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);

                // The box.
                //Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(0, 255, 0), 2);

                //Imgproc.rectangle(frame, boxSecond.tl(), boxSecond.br(), new Scalar(0, 0, 255), 2);

    //            Point a = box.tl();

   //             double pointtop = a.x ;  //topleft x
  //              double pointleft = a.y;  //topleft y

  //              Point b = boxSecond.br();

  //              double pointright = b.x;  //bottomright x
  //              double pointbottom = b.y; //bottomright y

                Point c = boxThird.tl();

                Point d = boxFourth.tl();


                Imgproc.rectangle(frame, new Point(c.x,c.y),
                        new Point(d.x,d.y),
                        new Scalar(0, 255, 0), 2);


                // set uncompansated delta position
                udp = new Point(c.x - lp.x, c.y - lp.y);

                // set compansated delta position
                cdp = new Point(udp.x + (double)cpt[1], udp.y + (double)cpt[0]);

                // update last position and size
                lp = new Point(c.x, c.y);
                lw = d.x - c.x;
                lh = d.y - c.y;

                break;

            }
        }


        if (!isFound) {

            // set new position
            pp = new Point(lp.x + cdp.x - (double)cpt[1], lp.y + cdp.y - (double)cpt[0]);

            // update last position
            lp = pp;

            // draw rectangle
            Imgproc.rectangle(frame, pp, new Point(pp.x + lw, pp.y + lh), new Scalar(255, 0, 0), 2);

            //Log.i(TAG, Double.toString(pp.x) + " | " + Double.toString(pp.y));
            //Log.i(TAG, cpt[0] + " | " + cpt[1] + " | " + cpt[2] + " | " + totalTime);
        }

        // reset position
        cpt[0] = 0f;
        cpt[1] = 0f;
        cpt[2] = 0f;

        return frame;

    }

    public void onCameraViewStopped() {
    }

    // Upload file to storage and return a path.
    private static String getPath(String file, Context context) {
        AssetManager assetManager = context.getAssets();
        BufferedInputStream inputStream = null;
        try {
            // Read data from assets.
            inputStream = new BufferedInputStream(assetManager.open(file));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();
            // Create copy file in storage.
            File outFile = new File(context.getFilesDir(), file);
            FileOutputStream os = new FileOutputStream(outFile);
            os.write(data);
            os.close();
            Log.i(TAG, "File loaded successfully");
            // Return a path to file which may be read in common way.
            return outFile.getAbsolutePath();
        } catch (IOException ex) {
            Log.i(TAG, "Failed to upload a file");
        }
        return "";
    }

    // Reset sensor data
    private void resetMotionData() {

        // reset velocity
        camVel[0] = 0f;
        camVel[1] = 0f;
        camVel[2] = 0f;

        // reset position in meters
        camPos[0] = 0f;
        camPos[1] = 0f;
        camPos[2] = 0f;

        // reset position in pixels
        cpt[0] = 0f;
        cpt[1] = 0f;
        cpt[2] = 0f;

    }

}
