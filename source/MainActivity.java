// Developed by: Sezai Burak Kantarci, December 2019
// For any question you can e-mail to : sezaiburakkantarci@gmail.com

package com.example.yolodetector;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.Manifest;
import android.content.pm.PackageManager;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
//import android.support.v7.app.AppCompatActivity;
//import android.support.v4.app.ActivityCompat;
//import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;

// OpenCV libraries
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.dnn.Net;
import org.opencv.dnn.Dnn;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.VideoWriter;
import org.opencv.videoio.Videoio;

// Java libraries
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import static org.opencv.imgproc.Imgproc.FONT_HERSHEY_SIMPLEX;


public class MainActivity extends AppCompatActivity {

    private String fileName;
    private EditText editText;

    private static final String TAG = "YOLO";

/*
    List<String> cocoNames = Arrays.asList("a person", "a bicycle", "a motorbike", "an airplane", "a bus",
            "a train", "a truck", "a boat", "a traffic light", "a fire hydrant", "a stop sign",
            "a parking meter", "a car", "a bench", "a bird", "a cat", "a dog", "a horse", "a sheep",
            "a cow", "an elephant", "a bear", "a zebra", "a giraffe", "a backpack", "an umbrella",
            "a handbag", "a tie", "a suitcase", "a frisbee", "skis", "a snowboard", "a sports ball",
            "a kite", "a baseball bat", "a baseball glove", "a skateboard", "a surfboard", "a tennis racket",
            "a bottle", "a wine glass", "a cup", "a fork", "a knife", "a spoon", "a bowl", "a banana",
            "an apple", "a sandwich", "an orange", "broccoli", "a carrot", "a hot dog", "a pizza",
            "a doughnut", "a cake", "a chair", "a sofa", "a potted plant", "a bed", "a dining table",
            "a toilet", "a TV monitor", "a laptop", "a computer mouse", "a remote control", "a keyboard",
            "a cell phone", "a microwave", "an oven", "a toaster", "a sink", "a refrigerator", "a book",
            "a clock", "a vase", "a pair of scissors", "a teddy bear", "a hair drier", "a toothbrush" );

    //80 elements


 */

    // The neural network
    private Net net;

    // The frame to be processed
    private Mat frame;

    // Info about the input video
    int totalFameNumber;
    int frameHeight;
    int frameWidth;

    // Initialize OpenCV manager.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)  {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");
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

        // Your code in addition to the existing code in the onCreate() of the parent class.
        super.onCreate(savedInstanceState);
        // Basically the app will look like the layout you have in activity main.
        setContentView(R.layout.activity_main);

        // edit text initialization
        // From activity_main.xml, the EditText
        editText = (EditText)findViewById(R.id.edit_text);

        // start button initializations
        final Button startButton = findViewById(R.id.start);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // set file name
                // Uses android:hint = "Enter video name"
                fileName = editText.getText().toString();

                // disable edit text field
                // when clicked, make it disappear
                editText.setEnabled(false);
                editText.setClickable(false);
                editText.setVisibility(View.GONE);

                // disable the start button
                // same thing with the start button. Looks like big play button
                startButton.setEnabled(false);
                startButton.setClickable(false);
                startButton.setVisibility(View.GONE);

                // update the text view
                TextView textView = (TextView)findViewById(R.id.text_view);
                textView.setText("Frames are processing, please wait");

                // call the detector function with a delay after click
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        detector();
                    }
                }, 500);

            }
        });


        // switch to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Check for writing external storage permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Writing external memory permission is granted");
        }
        // Ask for writing external storage permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


        // Check for writing external storage permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Reading external memory permission is granted");
        }
        // Ask for writing external storage permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

    }



    @Override
    public void onResume() {
        super.onResume();


        // Check for OpenCV libraries
        if(!OpenCVLoader.initDebug()){
            Log.i(TAG, "OpenCv Libraries are not loaded");
            // If OpenCV libraries is not loaded, trying to load it again
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.i(TAG, "OpenCv Libraries are loaded successfully");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }


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

            // The permission with the request code 2 (reading external storage)
            case 2: {
                // If request is cancelled, the result arrays are empty.
                // Permission is granted
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.i(TAG, "Reading external memory permission is granted");
                }
                // Permission is denied
                else {
                    Log.i(TAG, "Reading external memory permission is denied");
                }
                return;
            }
        }
    }




    public void detector(){

        // Detection variables
        final int IN_WIDTH = 416;
        final int IN_HEIGHT = 416;
        final float WH_RATIO = (float)IN_WIDTH / IN_HEIGHT;
        final double IN_SCALE_FACTOR = 0.00392;
        final double MEAN_VAL = 0;
        final float THRESHOLD = 0.3f;      // confidence threshold

        // Initialize text view
        // Show that "Frames are processing, please wait"
        TextView textView = (TextView)findViewById(R.id.text_view);

        // Time to calculate inference time
        long previousTime = Calendar.getInstance().getTimeInMillis();

        // What is the time now ?
        long currentTime;

        // Time taken to run the code.
        long inferenceTime;

        // String to log things
        String logInfo;

        // Output file name
        String outputFileName = Environment.getExternalStorageDirectory() + "/" +
                Environment.DIRECTORY_DCIM + "/VideoResults/" + fileName + "_Result";

        // Open the input video
        VideoCapture inputVideo = new VideoCapture();
        inputVideo.open(Environment.getExternalStorageDirectory() + "/" +
                Environment.DIRECTORY_DCIM + "/VideoSource/" + fileName + ".avi");

        // Check the input video
        if (inputVideo.isOpened())
            Log.i(TAG, "Input video opened successfully");
        else {
            Log.i(TAG, "Input video could not opened");
            textView.setText("Input video could not opened");
            return;
        }


        // Obtain info about input video
        totalFameNumber = (int)inputVideo.get(Videoio.CAP_PROP_FRAME_COUNT);
        frameHeight = (int)inputVideo.get(Videoio.CAP_PROP_FRAME_HEIGHT);
        frameWidth = (int)inputVideo.get(Videoio.CAP_PROP_FRAME_WIDTH);


        // Read accelerometer data
        // Array of array, for each frame hold 3 data for x,y,z
        float[][] accelerometerData = new float[totalFameNumber][3];

        // Array, just hold for every frame number.
        float[] deltaTime = new float[totalFameNumber];
        try {

            //BufferedReader is a class which simplifies reading text from a character input stream. It buffers the characters in order to enable efficient reading of text data.
            BufferedReader in = new BufferedReader(new FileReader(Environment.getExternalStorageDirectory() + "/" +
                    Environment.DIRECTORY_DCIM + "/VideoSource/" + fileName + ".txt"));

            // Defining line, and counter.
            String line;
            int i = 0;

            // read the text file line by line
            while (true) {
                if((line = in.readLine()) == null)
                    break;

                // Break if, line is equal to null.


                // The input text is like, (frame number , accX , accY , accZ , deltaTime)
                // So we shift it.
                String[] temp = line.split(",");
                accelerometerData[i][0] = Float.parseFloat(temp[1]);
                accelerometerData[i][1] = Float.parseFloat(temp[2]);
                accelerometerData[i][2] = Float.parseFloat(temp[3]);
                deltaTime[i] = Float.parseFloat(temp[4]);
                i = i + 1;
            }
            in.close();
            Log.i(TAG, "Accelerometer data read successfully");

        } catch (IOException e) {
            System.out.println("File Read Error");
            Log.i(TAG, "Accelerometer data could not read");
            textView.setText("Accelerometer data could not read");
            return;
        }


        // Initialize the output video
        VideoWriter outputVideo = new VideoWriter();

        int frameCounter = 1;
        // Constructor for Mat frame
        frame = new Mat();

        // Read and detect the video frame by frame
        while(true) {

            // If all frames are done
            if (!inputVideo.read(frame)) {
                Log.i(TAG, "All frames are done");
                inputVideo.release();    // release the video
                break;  // terminate the detection
            }

            // Restart the info to log
            logInfo = "";

            // Calculate inference time and update the info to log
            currentTime = Calendar.getInstance().getTimeInMillis();
            inferenceTime = currentTime - previousTime;
            previousTime = currentTime;


            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);  // color space change

            Mat blob = Dnn.blobFromImage(frame, IN_SCALE_FACTOR, new Size(IN_WIDTH, IN_HEIGHT),
                    new Scalar(MEAN_VAL, MEAN_VAL, MEAN_VAL), false, false);
            net.setInput(blob);

            // First we define the result list, they will be filled with two YOLO layers
            java.util.List<Mat> result = new java.util.ArrayList<Mat>(2);


            // We make this list of 2 values, of two YOLO layers

            List<String> outBlobNames = new java.util.ArrayList<>();
            outBlobNames.add(0, "yolo_16");
            outBlobNames.add(1, "yolo_23");

            // detections is defined as a net.forward()
            //  Mat detections = net.forward();

            net.forward(result, outBlobNames);

            // List of Class ID's
            List<Integer> clsIds = new ArrayList<>();
            // List of Confidences
            List<Float> confs = new ArrayList<>();
            // List of Rectangles, the coordinates of the bounding boxes
            List<Rect> rects = new ArrayList<>();

            // is detected defined false as default
            boolean isDetected = false;

            // Iterate over the size of the list
            // If this is running for small YOLO, 2 layers here
            // If this runs on big YOLO, 3 layers here
            // Simply, for our case; this is just 2.

            for (int i = 0; i < result.size(); ++i) {

                // level is just the yololayer for each case.
                // level = yolo_16 , for the first one
                // level = yolo_23 , for the second one

                Mat level = result.get(i);

                // Iterate over each of it's rows
                for (int j = 0; j < level.rows(); ++j) {

                    // row here is kinda like my detections, i think
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

                    //The way YOLO predicts the outputs, first off all it predicts in percentage values
                    //in a frame, exactly middle, 0.5x , 0.5y
                    //At first we extract those percentages and convert to pixels,
                    //If it's bigger than threshold than we will process it
                    if (confidence > THRESHOLD) {

                        // isDetected = true;
                        // This was good the way that it is, but we need pixels.
                        int centerX = (int) (row.get(0, 0)[0] * frame.cols());
                        int centerY = (int) (row.get(0, 1)[0] * frame.rows());

                        int width = (int) (row.get(0, 2)[0] * frame.cols());
                        int height = (int) (row.get(0, 3)[0] * frame.rows());


                        // Possible addition here. If we need to make the pixels that is always positive or zero
                        // we might need to add the condition saying, if it is negative; make it zero.


                        //We calculate the top-left point coordinates
                        int left = centerX - width / 2;
                        int top = centerY - height / 2;

                        // Possible problem here. Wouldn't it be bottom left point, if we subtract the height?


                        // Each classIdPoint is added to the clsIds list
                        clsIds.add((int) classIdPoint.x);

                        // Each confidence value is added to the confs list.
                        confs.add((float) confidence);

                        // Remember, we are still in the if, so if confidence is higher than threshold, we pass the values to rects.
                        // We construct the rect object
                        // And we add it to the rects list.
                        rects.add(new Rect(left, top, width, height));

                    }
                }
            }


            // How many of those rows went through 2 YOLO layers and was added to the confs list.
            int ArrayLength = confs.size();


            if (ArrayLength >= 1) {

                // Apply non-maximum suppression procedure.
                // What is max supression ? For example for a mobile phone, there may be 2 BBOXes for the same object
                // With max supression, it decides which boxes overlap, chose the one with highest confidence

                //Non maximum supression threshold
                float nmsThresh = 0.2f;

                // Pre-process for non maximum suppression


                // Convert the vector float to matrix to get confidences to matrix of floats; from confs list.
                MatOfFloat confidences = new MatOfFloat(Converters.vector_float_to_Mat(confs));

                // Define boxesArray, from rects list values.
                Rect[] boxesArray = rects.toArray(new Rect[0]);

                // Hmm. We defined a rect[] i guess boxes should be a MatOfRect
                MatOfRect boxes = new MatOfRect(boxesArray);

                MatOfInt indices = new MatOfInt();


                // Define the NMSBoxes from our values that we calculated
                // Performs non maximum suppression given boxes and corresponding scores.
                Dnn.NMSBoxes(boxes, confidences, THRESHOLD, nmsThresh, indices);


                // Draw result boxes:
                // The ones surpass the nms are indices , "ind"
                // Basically these are the best boxes to represent each object that is found in the frame.

                // Define ind array, from indices.
                int[] ind = indices.toArray();
                for (int i = 0; i < ind.length; ++i) {

                    isDetected = true;

                    int idx = ind[i];
                    Rect box = boxesArray[idx];

                    int idGuy = clsIds.get(idx);

                    float conf = confs.get(idx);


                    List<String> cocoNames = Arrays.asList("person", "bicycle", "car", "motorbike", "aeroplane", "bus", "train", "truck", "boat", "traffic light", "fire hydrant", "stop sign", "parking meter", "bench", "bird", "cat", "dog", "horse", "sheep", "cow", "elephant", "bear", "zebra", "giraffe", "backpack", "umbrella", "handbag", "tie", "suitcase", "frisbee", "skis", "snowboard", "sports ball", "kite", "baseball bat", "baseball glove", "skateboard", "surfboard", "tennis racket", "bottle", "wine glass", "cup", "fork", "knife", "spoon", "bowl", "banana", "apple", "sandwich", "orange", "broccoli", "carrot", "hot dog", "pizza", "doughnut", "cake", "chair", "sofa", "potted plant", "bed", "dining table", "toilet", "TV monitor", "laptop", "computer mouse", "remote control", "a keyboard", "cell phone", "microwave", "oven", "toaster", "sink", "refrigerator", "book", "clock", "vase", "scissors", "teddy bear", "hair drier", "toothbrush");


                    // The number that is pressed
                    int intConf = (int) (conf * 100);

                    Imgproc.putText(frame, cocoNames.get(idGuy) + " " + intConf + "%", box.tl(), FONT_HERSHEY_SIMPLEX, 2, new Scalar(255, 255, 0), 2);


                    Imgproc.rectangle(frame, box.tl(), box.br(), new Scalar(255, 0, 0), 2);

                    // update the info to log
                    logInfo = logInfo + Integer.toString(frameCounter) + "," + Long.toString(inferenceTime) + "," +
                            cocoNames.get(idGuy) + "," + Integer.toString(box.x) + "," + Integer.toString(box.y) +
                            "," + Integer.toString(box.width) + "," + Integer.toString(box.height) +
                            "," + Double.toString(conf) + "," + Float.toString(accelerometerData[frameCounter - 1][0]) + "," +
                            Float.toString(accelerometerData[frameCounter - 1][1]) + "," + Float.toString(accelerometerData[frameCounter - 1][2]) + "\n";


                    String label = cocoNames.get(idGuy);

                    int[] baseLine = new int[1];
                    Size labelSize = Imgproc.getTextSize(label, Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, 1, baseLine);

                    // Draw background for label.
                    Imgproc.rectangle(frame, new Point(box.x, box.y - labelSize.height),
                            new Point(box.x + labelSize.width, box.y + baseLine[0]),
                            new Scalar(255, 255, 255), Core.FILLED);

                    // Write class name and confidence.
                    Imgproc.putText(frame, label, new Point(box.x,box.y),
                            Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));



                    try {
                        FileOutputStream logFile = new FileOutputStream(new File(outputFileName + ".txt"), true);

                        //logFile.write((logInfo).getBytes());
                        logFile.close();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }


            if (!isDetected)
                // update the info to log
                logInfo = Integer.toString(frameCounter) + "," + Long.toString(inferenceTime) + "," +
                        "NOTHING" + "," + Integer.toString(0) + "," + Integer.toString(0) + "," +
                        Integer.toString(0) + "," + Integer.toString(0) + "," + Double.toString(0.0) +
                        "," + Float.toString(0) + "," + Float.toString(0) + "," + Float.toString(0) + "\n";


            // open the output video
            if (frameCounter == 1) {
                outputVideo.open(outputFileName + ".avi",
                        VideoWriter.fourcc('M', 'J', 'P', 'G'), 30, new Size(frame.width(), frame.height()));

                // check the output video
                if (outputVideo.isOpened())
                    Log.i(TAG, "Output video opened successfully");
                else
                    Log.i(TAG, "Output video could not opened ");
            }

            // print camera motion info to the frame
            printCameraMotion(accelerometerData[frameCounter - 1], deltaTime[frameCounter - 1]);

            // convert RGBA to BGR (do not use alpha channel in videos)
            Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2BGR);
            outputVideo.write(frame); // write frame to the output video

            // send logged information to computer
            Log.i(TAG, logInfo);

            // save results to output log file
            try {
                FileOutputStream logFile = new FileOutputStream(new File(outputFileName + ".txt"), true);

                logFile.write((logInfo).getBytes());
                logFile.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            // increment frame counter
            frameCounter = frameCounter + 1;

        } //end of while which means end of the video


        outputVideo.release();  // close the output video

        if (!outputVideo.isOpened()) {
            Log.i(TAG, Environment.getExternalStorageDirectory() + "/" +
                    Environment.DIRECTORY_DCIM + "/VideoResults/" + fileName + "_Result" + ".avi" + " is saved");
            // showing the name and the path to the user as a toast message
            Toast.makeText(this, Environment.getExternalStorageDirectory() + "/" +
                    Environment.DIRECTORY_DCIM + "/VideoResults/" + fileName + "_Result" + ".avi"
                    + " is saved", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "Output video closed successfully");
        }
        else
            Log.i(TAG, "Output video could not closed ");


        // update the text view
        textView.setText("Detection finished successfully");



    } //end of detection



    // camera motion analyzer
    private String analyseCameraMotion(float[] accelerometer, float deltaTime) {

        float threshold = 13f;
        float[] velocity = {accelerometer[0] * deltaTime, accelerometer[1] * deltaTime, accelerometer[2] * deltaTime};

        if (velocity[0] > threshold)
            return "Down";

        else if (velocity[0] < -threshold)
            return "Up";

        else if (velocity[1] > threshold)
            return "Right";

        else if (velocity[1] < -threshold)
            return "Left";

        else
            return "Stop";

    }




    // print camera motion info to screen
    private void printCameraMotion(float[] accelerometer, float deltaTime) {

        String info = analyseCameraMotion(accelerometer, deltaTime);

        int[] baseLine = new int[1];
        Size labelSize = Imgproc.getTextSize(info, FONT_HERSHEY_SIMPLEX, 1, 1, baseLine);

        // draw background for label.
        Imgproc.rectangle(frame, new Point(10, frameHeight - 10),
                new Point(10 + labelSize.width, frameHeight - labelSize.height - 10),
                new Scalar(255, 255, 255), Core.FILLED);

        // write class name and confidence.
        Imgproc.putText(frame, info, new Point(10, frameHeight - 10),
                FONT_HERSHEY_SIMPLEX, 1, new Scalar(0, 0, 0));

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


}


