package com.ing.software.ticketapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Parameter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.provider.MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE;
import static com.ing.software.ticketapp.BillActivity.getCameraInstance;

/**
 * This class is fully developed by Nicola Dal Maso
 */

public class CameraActivity extends Activity {

    private Camera mCamera;
    private SurfaceView mPreview;
    private MediaRecorder mMediaRecorder;
    private byte[] imageData;
    private static  final int FOCUS_AREA_SIZE= 300;
    public static final int MEDIA_TYPE_IMAGE = 1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_camera);

        initializeComponents();
    }

    public void initializeComponents(){
        mCamera = getCameraInstance();
        Camera.Parameters p = mCamera.getParameters();
        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        p.setJpegQuality(100);
        mCamera.setParameters(p);

        final ImageButton flashButton = (ImageButton)findViewById(R.id.flashBtn);
        flashButton.setTag(0);
        flashButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));

        flashButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                final int status =(Integer) v.getTag();
                switch (status){
                    case 0:
                        v.setTag(1);
                        flashButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_on));
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
                        mCamera.setParameters(p);
                        break;
                    case 1:
                        v.setTag(2);
                        flashButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_auto));
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
                        mCamera.setParameters(p);
                        break;
                    case 2:
                        v.setTag(0);
                        flashButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_flash_off));
                        p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                        mCamera.setParameters(p);
                        break;
                }
            }
        });


        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);



        mPreview.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    focusOnTouch(event);
                }
                return true;
            }
        });

        preview.addView(mPreview);

        ImageButton captureButton = (ImageButton) findViewById(R.id.takePhoto_button);
        captureButton.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                     // get an image from the camera
                     mCamera.takePicture(null, null, mPicture);
             }
        });

        Button finishButton = (Button)findViewById(R.id.btnCheck_goBack);
        finishButton.setOnClickListener( new View.OnClickListener() {
            @Override
            public void onClick (View v) {
                finish();
            }
        });

    }

    private void focusOnTouch(MotionEvent event) {
        if (mCamera != null ) {

            Camera.Parameters parameters = mCamera.getParameters();
            if (parameters.getMaxNumMeteringAreas() > 0){
                Log.i(TAG,"fancy !");
                Rect rect = calculateFocusArea(event.getX(), event.getY());

                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
                meteringAreas.add(new Camera.Area(rect, 800));
                parameters.setFocusAreas(meteringAreas);

                mCamera.setParameters(parameters);
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }else {
                mCamera.autoFocus(mAutoFocusTakePictureCallback);
            }
        }
    }

    private Rect calculateFocusArea(float x, float y) {
        int left = clamp(Float.valueOf((x / mPreview.getWidth()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);
        int top = clamp(Float.valueOf((y / mPreview.getHeight()) * 2000 - 1000).intValue(), FOCUS_AREA_SIZE);

        return new Rect(left, top, left + FOCUS_AREA_SIZE, top + FOCUS_AREA_SIZE);
    }

    private int clamp(int touchCoordinateInCameraReper, int focusAreaSize) {
        int result;
        if (Math.abs(touchCoordinateInCameraReper)+focusAreaSize/2>1000){
            if (touchCoordinateInCameraReper>0){
                result = 1000 - focusAreaSize/2;
            } else {
                result = -1000 + focusAreaSize/2;
            }
        } else{
            result = touchCoordinateInCameraReper - focusAreaSize/2;
        }
        return result;
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();
        releaseCamera();
        // two new lines for removing the old CameraPreview
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeView(mPreview);
    }
    @Override
    public void onResume() {
        super.onResume();
        if (mCamera == null)
        {
            initializeComponents();
        }
    }

    private Camera.AutoFocusCallback mAutoFocusTakePictureCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            if (success) {
                // do something...
                Log.i("tap_to_focus","success!");
            } else {
                // do something...
                Log.i("tap_to_focus","fail!");
            }
        }
    };

    private void releaseMediaRecorder(){
        if (mMediaRecorder != null) {
            mMediaRecorder.reset();   // clear recorder configuration
            mMediaRecorder.release(); // release the recorder object
            mMediaRecorder = null;
            mCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (mCamera != null){
            mCamera.release();        // release the camera for other applications
            mCamera = null;
        }
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Singleton.getInstance().setTakenPicure(data);
            Intent intent = new Intent(getApplicationContext(), CheckPhotoActivity.class);
            startActivity(intent);
        }
    };

    public static String tempFileImage(Context context, Bitmap bitmap, String name) {

        File outputDir = context.getCacheDir();
        File imageFile = new File(outputDir, name + ".jpg");

        OutputStream os;
        try {
            os = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
        } catch (Exception e) {
            Log.e(context.getClass().getSimpleName(), "Error writing file", e);
        }

        return imageFile.getAbsolutePath();
    }

}
