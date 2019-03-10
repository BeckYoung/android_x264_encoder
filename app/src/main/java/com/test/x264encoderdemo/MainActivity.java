package com.test.x264encoderdemo;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import example.sszpf.x264.x264sdk;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, Camera
        .PreviewCallback {
    private static final String TAG = MainActivity.class.getSimpleName();

    private SurfaceView surfaceview;

    private SurfaceHolder surfaceHolder;
    private Button btn_switch;

    private Camera camera;

    private Parameters parameters;
    // 图片旋转角度
    private int degree;

    private int width = 640;

    private int height = 480;

    private int fps = 20;

    private int bitrate = 90000;

    private x264sdk x264;
    // 时间戳
    private int timespan = bitrate / fps;
    // 总时间
    private long time;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceview = (SurfaceView) findViewById(R.id.surfaceview);
        btn_switch = (Button) findViewById(R.id.btn_switch);
        surfaceHolder = surfaceview.getHolder();
        surfaceHolder.addCallback(this);
        x264 = new x264sdk(l);
        btn_switch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean selected = btn_switch.isSelected();
                btn_switch.setSelected(!selected);
                if (btn_switch.isSelected()) {
                    // 停止编码
                    btn_switch.setText(R.string.app_stop);
                } else {
                    btn_switch.setText(R.string.app_start);
                }
            }
        });
        createfile();
    }

    private x264sdk.listener l = new x264sdk.listener() {

        @Override
        public void h264data(byte[] buffer, int length) {
            // TODO Auto-generated method stub
            try {
                outputStream.write(buffer, 0, buffer.length);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() +
            "/x264_test.h264";
    private BufferedOutputStream outputStream;
    FileOutputStream outStream;

    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {

        if (btn_switch.isSelected()) {
            // 编码
            time += timespan;
            byte[] yuv420 = new byte[width * height * 3 / 2];
            YUV420SP2YUV420(data, yuv420, width, height);
            byte[] yuv420_rotate = new byte[width * height * 3 / 2];
            YuvUtils.getInstance().yuv420Rotate90(yuv420, yuv420_rotate, width, height, degree);
            x264.PushOriStream(yuv420_rotate, yuv420_rotate.length, time);
        }
    }


    @Override
    public void surfaceCreated(SurfaceHolder holder) {

//        x264.initX264Encode(width, height, fps, bitrate);
        // 上面进行了yuv数据的旋转，宽高正好相反
        x264.initX264Encode(height, width, fps, bitrate);
        camera = getBackCamera();
        startcamera(camera);
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {


    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        if (null != camera) {
            camera.setPreviewCallback(null);
            camera.stopPreview();
            camera.release();
            camera = null;
        }
        x264.CloseX264Encode();
        try {
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void startcamera(Camera mCamera) {
        if (mCamera != null) {
            try {
                mCamera.setPreviewCallback(this);
                setCameraDisplayOrientation(Camera.CameraInfo.CAMERA_FACING_BACK,mCamera);
//                mCamera.setDisplayOrientation(90);
                if (parameters == null) {
                    parameters = mCamera.getParameters();
                }
                parameters = mCamera.getParameters();
                parameters.setPreviewFormat(ImageFormat.NV21);
                List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
                for (Camera.Size size : sizeList) {
                    Log.d(TAG, "support width = " + size.width + ",height = " + size.height);
                }
                parameters.setPreviewSize(width, height);
                mCamera.setParameters(parameters);
                mCamera.setPreviewDisplay(surfaceHolder);
                mCamera.startPreview();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Camera getBackCamera() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera
            // instance
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c; // returns null if camera is unavailable
    }

    private void setCameraDisplayOrientation(int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }
        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        this.degree = result;
        Log.d(TAG, "result = " + result);
        camera.setDisplayOrientation(result);
    }

    // nv21 to yuv420
    private void YUV420SP2YUV420(byte[] yuv420sp, byte[] yuv420, int width, int height) {
        if (yuv420sp == null || yuv420 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        //copy y
        for (i = 0; i < framesize; i++) {
            yuv420[i] = yuv420sp[i];
        }
        i = 0;
        for (j = 0; j < framesize / 2; j += 2) {
            yuv420[i + framesize * 5 / 4] = yuv420sp[j + framesize];
            i++;
        }
        i = 0;
        for (j = 1; j < framesize / 2; j += 2) {
            yuv420[i + framesize] = yuv420sp[j + framesize];
            i++;
        }
    }
}
