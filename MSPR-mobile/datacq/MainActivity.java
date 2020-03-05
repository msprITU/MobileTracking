package com.example.ulas.videorecorder;

// Android libraries
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.WindowManager;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.Toast;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.Sensor;

// OpenCV libraries
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoWriter;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

// Java libraries
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class MainActivity extends AppCompatActivity implements CvCameraViewListener2, SensorEventListener {

    // app tag
    // Generally used for showing a message in log file
    private static final String TAG = "SSD/MobileNet";

    // the camera bridge
    // God Knows what this do
    private CameraBridgeViewBase mOpenCvCameraView;

    // the output video
    VideoWriter outputVideo;

    // input frame size
    int frameWidth;
    int frameHeight;

    // accelerometer data
    float[] accelerometerData = new float[3];

    // time data , used in accelerometer
    long previousTime;
    long currentTime;
    long inferenceTime;

    // accelerometer
    private SensorManager sensorManager;
    private Sensor accelerometer;

    // boolean to start recording
    private boolean isRecording = false;

    // file name
    String fileName;

    // frame counter
    private int frameCounter = 1;

    // Initialize OpenCV manager.
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this)  {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV loaded successfully");

                    mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
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



        // start button initializations
        final Button startButton = findViewById(R.id.capture);
        startButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!isRecording)
                    startRecording();
                else
                    stopRecording();
            }
        });

        // switch to full screen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);


        // Check for camera access permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Camera access permission is granted");
            startCameraListener(); // start the camera listener
        }
        // Ask for camera access permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 0);
        }


        // Check for writing external storage permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Writing external memory permission is granted");
        }
        // Ask for writing external storage permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


        // Check for reading external storage permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Reading external memory permission is granted");
        }
        // Ask for writing external storage permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 2);
        }

        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        File F = new File(baseDir + "/DCIM" + "/" + "VideoSource" );
        if(!F.exists()){
            F.mkdir();
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

        // sensor registration
        sensorManager.registerListener(this, accelerometer, 5, 1);

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
                    startCameraListener(); // start the camera listener
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


    @Override
    public final void onAccuracyChanged(Sensor sensor, int accuracy) {}


    @ Override
    public final void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            accelerometerData = event.values;
            //saveAccelerometerData();
        }
    }



    public void onCameraViewStarted(int width, int height) {

        // initialize frame size
        frameWidth = width;
        frameHeight = height;

        Log.i(TAG,  "Camera view started");

    }



    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        Mat frame = inputFrame.rgba();  // get a new frame
        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_RGBA2BGR);  // color space change to record

        // save rich video frame by frame
        if(isRecording) {
            outputVideo.write(frame);
            saveAccelerometerData();
            frameCounter = frameCounter + 1;
        }

        Imgproc.cvtColor(frame, frame, Imgproc.COLOR_BGR2RGB);  // color space change to display
        return frame;
    }



    public void onCameraViewStopped() {}



    private void startRecording() {

        frameCounter = 1;

        // formatting the file name and path (to gallery as jpg)
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        String currentDateAndTime = sdf.format(new Date());
        fileName = "sampleCV_" + currentDateAndTime;

        // Updating the Folder, 15.10.2019 - 07:46

        String outputVideoName = Environment.getExternalStorageDirectory()
                + "/" + Environment.DIRECTORY_DCIM + "/VideoSource/" + fileName + ".avi";

        outputVideo = new VideoWriter();


        // Check for writing external storage permission
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            Log.i(TAG, "Writing external memory permission is granted");


            // open the output video
            outputVideo.open(outputVideoName,
                    VideoWriter.fourcc('M', 'J', 'P', 'G'), 30, new Size(frameWidth, frameHeight));


            Log.i(TAG, outputVideoName + " is recording");
            // showing the name and the path to the user as a toast message
            Toast.makeText(this, outputVideoName + " is recording", Toast.LENGTH_SHORT).show();
        }
        // Ask for writing external storage permission if needed
        else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }


        // check the output video
        if (outputVideo.isOpened()) {
            Log.i(TAG, "Output video opened successfully");
            isRecording = true;
            final Button startButton = findViewById(R.id.capture);
            startButton.setText("STOP");
        }
        else
            Log.i(TAG, "Output video could not opened ");

        previousTime = Calendar.getInstance().getTimeInMillis();
    }


    // stop recording
    private void stopRecording() {
        isRecording = false;
        outputVideo.release();  // close the output video

        final Button startButton = findViewById(R.id.capture);
        startButton.setText("START");

        Log.i(TAG,  "Recording" + fileName + " is stopped");
        // showing the name and the path to the user as a toast message
        Toast.makeText(this, "Recording" + fileName + " is stopped", Toast.LENGTH_SHORT).show();
    }


    // start camera listener
    private void startCameraListener() {
        // set up camera listener.
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.camera_view);
        mOpenCvCameraView.enableFpsMeter();
        mOpenCvCameraView.setMaxFrameSize(1280, 720);
        mOpenCvCameraView.setVisibility(CameraBridgeViewBase.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        Log.i(TAG,  "Camera listener started");
    }


    // save results to output log file
    private void saveAccelerometerData() {

        if(!isRecording)
            return;

        currentTime = Calendar.getInstance().getTimeInMillis();
        inferenceTime = currentTime - previousTime;
        previousTime = currentTime;

        String infoToLog = "";

        try {
            FileOutputStream logFile = new FileOutputStream(new File(Environment.getExternalStorageDirectory()
                    + "/" + Environment.DIRECTORY_DCIM + "/VideoSource/" + fileName + "_gyro.txt"), true);

            infoToLog = Integer.toString(frameCounter) + "," + Float.toString(accelerometerData[0]) + "," +
                    Float.toString(accelerometerData[1]) + "," + Float.toString(accelerometerData[2]) + "," +
                    Long.toString(inferenceTime)+ "\n";

            logFile.write((infoToLog).getBytes());
            logFile.close();

            Log.i(TAG, infoToLog);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
