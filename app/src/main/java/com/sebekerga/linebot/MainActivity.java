package com.sebekerga.linebot;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat mIntermediateMat;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.cameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
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
        Mat src = inputFrame.rgba();
        Mat input = new Mat(720, 1280, CvType.CV_32FC2);
        Mat output = new Mat(720, 1280, CvType.CV_32FC2);

        Point srcPts[] = new Point[4];
        srcPts[3] = new Point(1235/1240, 575/720);
        srcPts[2] = new Point(1235/1240, 145/720);
        srcPts[1] = new Point(850/124, 710/720);
        srcPts[0] = new Point(850/124, 10/720);
//        srcPts[3] = new Point((1235.0/1240.0) * src.width(), (575.0/720.0) * src.height());
//        srcPts[2] = new Point((1235.0/1240.0) * src.width(), (145.0/720.0) * src.height());
//        srcPts[1] = new Point((850.0/124.0) * src.width(), (710.0/720.0) * src.height());
//        srcPts[0] = new Point((850.0/124.0) * src.width(), (10.0/720.0) * src.height());
        List<Point> input_points = new ArrayList<Point>(Arrays.asList(srcPts));
        input = Converters.vector_Point2f_to_Mat(input_points);

        Point targetPts[] = new Point[4];
        targetPts[0] = new Point(0, 0);
        targetPts[1] = new Point(0, src.height());
        targetPts[2] = new Point(src.width(), 0);
        targetPts[3] = new Point(src.width(), src.height());
        List<Point> output_points = new ArrayList<Point>(Arrays.asList(targetPts));
        output = Converters.vector_Point2f_to_Mat(output_points);

        Mat dst = src.clone();

        Mat perspectiveTransform = Imgproc.getPerspectiveTransform(input, output);

        Imgproc.warpPerspective(src, dst, perspectiveTransform, new Size(src.width(), src.height()), Imgproc.INTER_CUBIC);

        //dst.release();
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
}
