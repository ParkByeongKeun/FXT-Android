package com.fiberfox.fxt;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.fiberfox.fxt.RestApi.ApiService;
import com.fiberfox.fxt.RestApi.ResponseEnclosure;
import com.fiberfox.fxt.utils.ConstantUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class EnclosureActivity extends Activity {
    private static final String TAG = "AndroidCameraApi";
    private ImageButton takePictureButton;
    private TextureView textureView;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private File root;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraId;
    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest captureRequest;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private File file;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    private boolean mFlashSupported;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    Dialog custom_loading_dialog;
    CustomApplication customApplication;
    private Timer timer;
    int num = 1;
    private final android.os.Handler handler = new android.os.Handler();
    boolean isUpdateImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enclosure);
        root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        customApplication = (CustomApplication)getApplication();
        initLoadingDialog();
        textureView = (TextureView) findViewById(R.id.texture);
        assert textureView != null;
        textureView.setSurfaceTextureListener(textureListener);
        takePictureButton = (ImageButton) findViewById(R.id.btn_takepicture);
        assert takePictureButton != null;
        takePictureButton.setOnClickListener(v -> takePicture());

        findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            finish();
        });
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            //open your camera here
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };
    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            //This is called when the camera is open
            Log.e(TAG, "onOpened");
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };
    final CameraCaptureSession.CaptureCallback captureCallbackListener = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Toast.makeText(EnclosureActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
            Log.d("yot132","saved = " + file);
            createCameraPreview();
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    protected void takePicture() {
        if (null == cameraDevice) {
            Log.e(TAG, "cameraDevice is null");
            return;
        }
        try {
            int width = 640;
            int height = 640;
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textureView.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            // Orientation
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
            String path = generateFileName()+".jpg";
            File file = new File(root,path);
//            File file = new File(root,"20230926_195359.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    Image image = null;
                    try {
                        image = reader.acquireLatestImage();
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    // 이미지 크기 확인
                    int imageWidth = bitmap.getWidth();
                    int imageHeight = bitmap.getHeight();
                    Log.d(TAG, "Image size: " + imageWidth + "x" + imageHeight);

                    // 이미지를 90도 회전
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90); // 회전 각도를 조절할 수 있습니다.
                    Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, imageWidth, imageHeight, matrix, false);

                    // 원하는 크기로 리사이징 (예: 640x640)
                    int targetWidth = 640;
                    int targetHeight = 640;
                    Bitmap resizedBitmap = Bitmap.createScaledBitmap(rotatedBitmap, targetWidth, targetHeight, false);

                    // 리사이징된 이미지를 파일에 저장
                    OutputStream output = null;
                    try {
                        output = new FileOutputStream(file);
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(EnclosureActivity.this, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    runOnUiThread(() -> {
                        showLoadingDialog(file);
                    });

                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(640, 640);
            Surface surface = new Surface(texture);
//            Log.d("yot132","2w = " + imageDimension.getWidth() + " h = " + imageDimension.getHeight());
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Arrays.asList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    //The camera is already closed
                    if (null == cameraDevice) {
                        return;
                    }
                    // When the session is ready, we start displaying the preview.
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(EnclosureActivity.this, "Configuration change", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        Log.e(TAG, "is camera open");
        try {
            cameraId = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assert map != null;
            imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            // Add permission for camera and let user grant the permission
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(EnclosureActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        Log.e(TAG, "openCamera X");
    }

    protected void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                // close the app
                Toast.makeText(EnclosureActivity.this, "Sorry!!!, you can't use this app without granting permission", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e(TAG, "onResume");
        startBackgroundThread();
        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    @Override
    protected void onPause() {
        Log.e(TAG, "onPause");
        //closeCamera();
        stopBackgroundThread();
        super.onPause();
    }


    public String generateFileName() {
        // 현재 날짜 및 시간을 가져오기
        Date currentDate = new Date();

        // 파일 이름에 사용할 형식 지정
        String fileNameFormat = "yyyyMMdd_HHmmss";

        // SimpleDateFormat을 사용하여 형식에 맞게 날짜를 문자열로 변환
        SimpleDateFormat sdf = new SimpleDateFormat(fileNameFormat, Locale.getDefault());
        String formattedDate = sdf.format(currentDate);

        // 파일 이름 생성 (예: 20220101_123456)
        return "file_" + formattedDate;
    }


    public void initLoadingDialog() {
        custom_loading_dialog = new Dialog(this);
        custom_loading_dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        custom_loading_dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        custom_loading_dialog.setContentView(R.layout.custom_dialog_loading);
        custom_loading_dialog.setCancelable(false);
    }

    public void showLoadingDialog(File file) {
        boolean check = updateImage(file);
        runOnUiThread(() -> {
            custom_loading_dialog.show();
        });

        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                update();
                ProgressBar progressBar = custom_loading_dialog.findViewById(R.id.progress);
                progressBar.setProgress(50 + num);
                if(progressBar.getProgress() >= 100) {
                    timer.cancel();
                    custom_loading_dialog.dismiss();
                    if(check) {
                        Log.d("yot132","success");
                    }else {
                        Log.d("yot132","anomaly");
                    }
//                    if(loss >= customApplication.lossThreshold |
//                            leftAngle >= customApplication.angleThreshold |
//                            rightAngle >= customApplication.angleThreshold |
//                            isAnomaly) {
//                        runOnUiThread(() -> {
//                            tvPassFailTitle.setText("FAIL");
//                            mTextViewPassFail.setText(strPassFail);
//                            tvPassFailTitle.setTextColor(getResources().getColor(R.color.red));
//                        });
//
//                        ArrayList<String> check = getStringArrayPref(FusionSpliceDetailActivity.this,PREFS_NAME);
//                        boolean checkDialog = false;
//                        for(int i = 0 ; i < check.size() ; i++) {
//                            if (dialogBean.getId().equals(check.get(i))) {
//                                checkDialog = true;
//                            }
//                        }
//                        if(!checkDialog) {
//                            showCheckDialog();
//                        }
//                    }
                }
            }
        };
        timer = new Timer();
        timer.schedule(timerTask, 0, 50);
    }

    private void update(){
        Runnable runnable = () -> {
            if(num>50){
                timer.cancel();
            }else{
                num += 2;
            }
        };
        handler.post(runnable);
    }

    boolean updateImage(final File file) {
        isUpdateImage = false;
        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), file);
        MultipartBody.Part fileToUpload = MultipartBody.Part.createFormData("image", file.getName(), requestBody);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("Content-Type", "multipart/form-data").build();
            return chain.proceed(request);
        });
        Gson gson = new GsonBuilder().setLenient().create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://ijoon.iptime.org:15039")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(httpClient.build())
                .build();
        ApiService service = retrofit.create(ApiService.class);
        service.enclosure(fileToUpload).enqueue(new Callback<ResponseEnclosure>() {
            @Override
            public void onResponse(Call<ResponseEnclosure> call, Response<ResponseEnclosure> response) {
                ResponseEnclosure result = response.body();
                Log.d("yot132","isAnomaly = " + result.getResult());
                List<List<Float>> items = result.getResult();
                Thread thread = new Thread(() -> {
                    try {
                        Thread.sleep(1400);
                        Intent intent = new Intent(EnclosureActivity.this,EnclosureDetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(ConstantUtil.StrConstant.YOLO, (Serializable) items);
                        intent.putExtras(bundle);
                        intent.putExtra("img",file.getAbsolutePath());
                        startActivity(intent);
                        isUpdateImage = true;
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });
                thread.start();
            }

            @Override
            public void onFailure(Call<ResponseEnclosure> call, Throwable t) {
                isUpdateImage = false;
            }
        });
        Log.d("yot132","isUpdateImage = " + isUpdateImage);
        return isUpdateImage;
    }
}