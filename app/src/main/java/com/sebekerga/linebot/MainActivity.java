package com.sebekerga.linebot;

import android.bluetooth.BluetoothSocket;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.SeekBar;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mIntermediateMat;
    SeekBar threshSeekBar, powerSeekBar;
    int thresh, power;
    BluetoothSocket bluetoothSocket;
    InputStream inputStream;
    OutputStream outputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        threshSeekBar = (SeekBar) findViewById(R.id.threshSeekBar);
        threshSeekBar.setMax(255);
        threshSeekBar.setProgress(150);
        thresh = threshSeekBar.getProgress();
        threshSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                thresh = threshSeekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                thresh = threshSeekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                thresh = threshSeekBar.getProgress();
            }
        });
        powerSeekBar = (SeekBar) findViewById(R.id.powerSeekBar);
        powerSeekBar.setMax(100);
        powerSeekBar.setProgress(80);
        power = powerSeekBar.getProgress();
        powerSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                power = powerSeekBar.getProgress();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                power = powerSeekBar.getProgress();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                power = powerSeekBar.getProgress();
            }
        });

        bluetoothSocket = SocketHandler.getSocket();
        try {
            inputStream = bluetoothSocket.getInputStream();
            outputStream = bluetoothSocket.getOutputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    mOpenCvCameraView.enableView();
                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    public void onCameraViewStarted(int width, int height) {
        mIntermediateMat = new Mat();
    }

    public void onCameraViewStopped() {
        if (mIntermediateMat != null)
            mIntermediateMat.release();
        mIntermediateMat = null;
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();
        Mat grey = inputFrame.gray();

        int rows = rgba.height();
        int cols = rgba.width();

        int MidX = 0, MidY = 0, counter = 0;

        Mat workArea = grey.submat(0, rows, cols * 1/3 , cols * 2/3);

        Imgproc.GaussianBlur(workArea, workArea, new Size(9, 9), 10);
        Imgproc.threshold(workArea, workArea, thresh, thresh + 60, Imgproc.THRESH_BINARY_INV);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>(400);
        Imgproc.findContours(workArea, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);

        MatOfPoint2f points = new MatOfPoint2f();
        for (int i = 0; i < contours.size(); i++) {
            contours.get(i).convertTo(points, CvType.CV_32FC2);
        }
        Point[] pcontours = points.toArray();
        Point[] forpoint = { new Point(), new Point(), new Point(), new Point() };
        for (int i = 0, c = 0; i < pcontours.length && c < 4; i++) {
            Imgproc.line(workArea, pcontours[i], pcontours[i], new Scalar(70, 255, 70, 255), 3);
            counter++;
            MidX += pcontours[i].x;
            MidY += pcontours[i].y;
        }

        if(counter != 0) {
            MidX /= counter;
            MidY /= counter;
        }

        int dif = rows/2 - MidY;
        if(dif > 100) dif = 100;
        else if(dif < -100) dif = -100;

        setSpeed((byte) dif, (byte) dif);

        Imgproc.line(workArea, new Point(MidX, MidY), new Point(MidX, MidY), new Scalar(255, 70, 70, 255), 5);

        Mat dst = grey.clone();

        getDistance();

        return dst;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d("TAG", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("TAG", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void setSpeed(byte motorA, byte motorB){
        byte[] power = new byte[] {motorA, motorB};
        try {
            outputStream.write(power);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.i("Power", Integer.toString(power[0]));
    }

    public int getDistance(){
        int distance = 0;

        try {
            while(inputStream.available() != 0)
                distance = inputStream.read();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.i("Distance", Integer.toString(distance));
        return distance;
    }
}
