package com.cdjysd.licenseplatelib.ui.activity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import com.cdjysd.licenseplatelib.R;
import com.cdjysd.licenseplatelib.ui.view.PlateViewfinderView;
import com.cdjysd.licenseplatelib.utils.Devcode;
import com.cdjysd.licenseplatelib.utils.Utils;
import com.kernal.plateid.PlateCfgParameter;
import com.kernal.plateid.PlateRecogService;
import com.kernal.plateid.PlateRecognitionParameter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ScenCameraActivity extends Activity implements
        SurfaceHolder.Callback, Camera.PreviewCallback {

    private SeekBar seekBar;
    private int maxZoom;    //seekbar最大缩放值
    private Camera camera;
    private SurfaceView surfaceView;
    // private TextView resultEditText;
    private Button back_btn, flash_btn, back;
    private PlateViewfinderView myview;
    private RelativeLayout re;
    private int width, height, screenWidth, screenHeight;
    private TimerTask timer;
    private int preWidth = 0;
    private int preHeight = 0;
    private String number = "", color = "", hpzl = "";
    private SurfaceHolder holder;
    private int iInitPlateIDSDK = -1;
    private int nRet = -1;
    private int imageformat = 6;// NV21 -->6
    private int bVertFlip = 0;
    private int bDwordAligned = 1;
    private String[] fieldvalue = new String[14];
    private int rotation = 0;
    private static int tempUiRot = 0;
    private Bitmap bitmap, bitmap1;
    private Vibrator mVibrator;
    private PlateRecognitionParameter prp = new PlateRecognitionParameter();
    private boolean setRecogArgs = true;// 刚进入此界面后对识别车牌函数进行参数设置
    private boolean isCamera = true;// 判断是预览识别还是视频识别 true:视频识别 false:预览识别
    private boolean recogType = true;// 记录进入此界面时是拍照识别还是视频识别 true:视频识别 false:拍照识别
    private byte[] tempData;
    private byte[] picData;
    private Timer time;
    private boolean cameraRecogUtill = false; // cameraRecogUtill
    // true:拍照识别采用拍摄照片（整图）根据路径识别，不受扫描框限制
    // false:采用视频流 单帧识别模式 识别扫描框内的车牌
//    private String path;// 圖片保存的路徑/**/
    public PlateRecogService.MyBinder recogBinder;
    private boolean isAutoFocus = true; // 是否开启自动对焦 true:开启，定时对焦 false:不开起
    // ，只在图片模糊时对焦
    private boolean sameProportion = false;   //是否在1280*960预览分辨率以下找到与屏幕比相同比例的 预览分辨率组
    private int initPreWidth = 1920; //
    private int initPreHeight = 1080;//预览分辨率筛选上限，即在筛选合适的分辨率时  在这两个值以下筛选
    private boolean isFirstIn = true;
    public ServiceConnection recogConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            recogConn = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            recogBinder = (PlateRecogService.MyBinder) service;
            iInitPlateIDSDK = recogBinder.getInitPlateIDSDK();

            if (iInitPlateIDSDK != 0) {
                nRet = iInitPlateIDSDK;
                String[] str = {"" + iInitPlateIDSDK};
                getResult(str);
            }
            // recogBinder.setRecogArgu(recogPicPath, imageformat,
            // bGetVersion, bVertFlip, bDwordAligned);
            PlateCfgParameter cfgparameter = new PlateCfgParameter();
            cfgparameter.armpolice = 4;
            cfgparameter.armpolice2 = 16;
            cfgparameter.embassy = 12;
            cfgparameter.individual = 0;
            cfgparameter.nOCR_Th = 0;
            cfgparameter.nPlateLocate_Th = 5;
            cfgparameter.onlylocation = 15;
            cfgparameter.tworowyellow = 2;
            cfgparameter.tworowarmy = 6;
            cfgparameter.szProvince = "";
            cfgparameter.onlytworowyellow = 11;
            cfgparameter.tractor = 8;
            cfgparameter.bIsNight = 1;
            cfgparameter.newEnergy = 24;
            cfgparameter.consulate = 22;
            cfgparameter.Infactory = 18;
            cfgparameter.civilAviation = 20;
            if (cameraRecogUtill) {
                imageformat = 0;
            }
            recogBinder.setRecogArgu(cfgparameter, imageformat, bVertFlip,
                    bDwordAligned);

            // fieldvalue = recogBinder.doRecog(recogPicPath, width,
            // height);

        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            getScreenSize();
            if (msg.what == 5) {
                getPreToChangView(preWidth, preHeight);
            } else {
                re.removeView(myview);
                setRotationAndView(msg.what);
                getPreToChangView(preWidth, preHeight);
                if (rotation == 90 || rotation == 270) {
                    myview = new PlateViewfinderView(ScenCameraActivity.this, width, height, true);
                } else {
                    myview = new PlateViewfinderView(ScenCameraActivity.this, width, height, false);
                }
                re.addView(myview);
                if (camera != null) {
                    camera.setDisplayOrientation(rotation);
                }
            }
            super.handleMessage(msg);
        }
    };

    ScaleGestureDetector gestureDetector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int uiRot = getWindowManager().getDefaultDisplay().getRotation();// 获取屏幕旋转的角度
        requestWindowFeature(Window.FEATURE_NO_TITLE);//
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_scencamera);
        PlateRecogService.initializeType = recogType;

        findiew();
        setRotationAndView(uiRot);
        getScreenSize();
        tempUiRot = 0;
    }

    // 设置相机取景方向和扫面框
    private void setRotationAndView(int uiRot) {
        setScreenSize(this);
//		System.out.println("屏幕宽：" + width + "     屏幕高：" + height);
        rotation = Utils.setRotation(width, height, uiRot, rotation);
        if (rotation == 90 || rotation == 270) // 竖屏状态下
        {
            setLinearButton();
        } else { // 横屏状态下
            setHorizontalButton();

        }

    }

    @SuppressLint("NewApi")
    private void findiew() {
        // TODO Auto-generated method stub

        seekBar = ((SeekBar) findViewById(R.id.seekbar));
        surfaceView = (SurfaceView) findViewById(R.id.surfaceViwe_video);
        back_btn = (Button) findViewById(R.id.back_camera);
        flash_btn = (Button) findViewById(R.id.flash_camera);
        back = (Button) findViewById(R.id.back);
        re = (RelativeLayout) findViewById(R.id.memory);
        re.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {

            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight,
                                       int oldBottom) {
                if ((bottom != oldBottom && right == oldRight) || (bottom == oldBottom && right != oldRight)) {
                    Message mesg = new Message();
                    mesg.what = 5;
                    handler.sendMessage(mesg);
                }

            }
        });
        // hiddenVirtualButtons(re);
        holder = surfaceView.getHolder();
        holder.addCallback(ScenCameraActivity.this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        // 因为箭头方向的原因，横竖屏状态下 返回按钮是两张不同的ImageView
        // 横屏状态下返回按钮

        back_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                onBackPressed();
            }
        });
        // 竖屏状态下返回按钮
        back.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub\
                onBackPressed();
            }
        });
        // 闪光灯监听事件
        flash_btn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // b = true;
                // TODO Auto-generated method stub
                if (!getPackageManager().hasSystemFeature(
                        PackageManager.FEATURE_CAMERA_FLASH)) {
                    Toast.makeText(
                            ScenCameraActivity.this,
                            getResources().getString(
                                    getResources().getIdentifier("no_flash",
                                            "string", getPackageName())),
                            Toast.LENGTH_LONG).show();
                } else {
                    if (camera != null) {
                        Camera.Parameters parameters = camera.getParameters();
                        String flashMode = parameters.getFlashMode();
                        if (flashMode
                                .equals(Camera.Parameters.FLASH_MODE_TORCH)) {

                            parameters
                                    .setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                            parameters.setExposureCompensation(0);
                        } else {
                            parameters
                                    .setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);// 闪光灯常亮
                            parameters.setExposureCompensation(-1);

                        }
                        try {
                            camera.setParameters(parameters);
                        } catch (Exception e) {

                            Toast.makeText(
                                    ScenCameraActivity.this,
                                    getResources().getString(
                                            getResources().getIdentifier(
                                                    "no_flash", "string",
                                                    getPackageName())),
                                    Toast.LENGTH_LONG).show();
                        }
                        camera.startPreview();
                    }
                }
            }

        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double zoomF = 0.0d;
                int zoom = 0;
                if (camera != null) {
                    Camera.Parameters parameters = camera.getParameters();
                    if (parameters.isZoomSupported()) {
                        zoomF = ((double) camera.getParameters().getMaxZoom()) / 100.0d;
                        zoom = (int) (progress * zoomF);
                        parameters.setZoom(zoom);
                        camera.setParameters(parameters);
                        return;
                    }
                    Toast.makeText(ScenCameraActivity.this, "不支持调焦", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    // 设置竖屏方向按钮布局
    private void setLinearButton() {
        int back_w;
        int back_h;
        int flash_w;
        int flash_h;
        int Fheight;
        int take_h;
        int take_w;
        RelativeLayout.LayoutParams layoutParams;
        back.setVisibility(View.VISIBLE);
        back_btn.setVisibility(View.GONE);
        back_h = (int) (height * 0.066796875);
        back_w = (int) (back_h * 1);
        layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);

        Fheight = (int) (width * 0.75);
        layoutParams.topMargin = (int) (((height - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
        layoutParams.leftMargin = (int) (width * 0.10486111111111111111111111111111);
        back.setLayoutParams(layoutParams);

        flash_h = (int) (height * 0.066796875);
        flash_w = (int) (flash_h * 1);
        layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
                RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);

        Fheight = (int) (width * 0.75);
        layoutParams.topMargin = (int) (((height - Fheight * 0.8 * 1.585) / 2 - flash_h) / 2);
        layoutParams.rightMargin = (int) (width * 0.10486111111111111111111111111111);
        flash_btn.setLayoutParams(layoutParams);

//        take_h = (int) (height * 0.105859375);
//        take_w = (int) (take_h * 1);
//        layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.CENTER_HORIZONTAL,
//                RelativeLayout.TRUE);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
//                RelativeLayout.TRUE);
//        layoutParams.bottomMargin = (int) (height * 0.10486111111111111111111111111111);
//
//        take_pic.setLayoutParams(layoutParams);
    }

    // 设置横屏屏方向按钮布局
    private void setHorizontalButton() {
        int back_w;
        int back_h;
        int flash_w;
        int flash_h;
        int Fheight;
        int take_h;
        int take_w;
        RelativeLayout.LayoutParams layoutParams;
        back_btn.setVisibility(View.VISIBLE);
        back.setVisibility(View.GONE);
        back_w = (int) (width * 0.066796875);
        back_h = (int) (back_w * 1);
        layoutParams = new RelativeLayout.LayoutParams(back_w, back_h);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM,
                RelativeLayout.TRUE);
        Fheight = height;

        Fheight = (int) (height * 0.75);
        layoutParams.leftMargin = (int) (((width - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
        layoutParams.bottomMargin = (int) (height * 0.10486111111111111111111111111111);
        back_btn.setLayoutParams(layoutParams);

        flash_w = (int) (width * 0.066796875);
        flash_h = (int) (flash_w * 1);
        layoutParams = new RelativeLayout.LayoutParams(flash_w, flash_h);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT,
                RelativeLayout.TRUE);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP,
                RelativeLayout.TRUE);

        Fheight = (int) (height * 0.75);
        layoutParams.leftMargin = (int) (((width - Fheight * 0.8 * 1.585) / 2 - back_h) / 2);
        layoutParams.topMargin = (int) (height * 0.10486111111111111111111111111111);
        flash_btn.setLayoutParams(layoutParams);

//        take_h = (int) (width * 0.105859375);
//        take_w = (int) (take_h * 1);
//        layoutParams = new RelativeLayout.LayoutParams(take_w, take_h);
//        layoutParams.addRule(RelativeLayout.CENTER_VERTICAL,
//                RelativeLayout.TRUE);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,
//                RelativeLayout.TRUE);
//
//        layoutParams.rightMargin = (int) (width * 0.10486111111111111111111111111111);
//        take_pic.setLayoutParams(layoutParams);
    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
        closeCamera();
    }


    int nums = -1;
    private byte[] intentNV21data;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // 实时监听屏幕旋转角度
        int uiRot = getWindowManager().getDefaultDisplay().getRotation();// 获取屏幕旋转的角度
        if (uiRot != tempUiRot) {
            Message mesg = new Message();
            mesg.what = uiRot;
            handler.sendMessage(mesg);
            tempUiRot = uiRot;
        }
        if (setRecogArgs) {
            Intent authIntent = new Intent(ScenCameraActivity.this,
                    PlateRecogService.class);
            bindService(authIntent, recogConn, Service.BIND_AUTO_CREATE);
            setRecogArgs = false;
        }
        if (iInitPlateIDSDK == 0) {
            prp.height = preHeight;//
            prp.width = preWidth;//
            // 开发码
            prp.devCode = Devcode.DEVCODE;

            if (cameraRecogUtill) {
                // 拍照识别 在使用根据图片路径识别时 执行下列代码
                if (isCamera) {

                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                    options.inPurgeable = true;
                    options.inInputShareable = true;
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
                            preWidth, preHeight, null);
                    yuvimage.compressToJpeg(
                            new Rect(0, 0, preWidth, preHeight), 100, baos);
                    bitmap = BitmapFactory.decodeByteArray(baos.toByteArray(),
                            0, baos.size(), options);
                    Matrix matrix = new Matrix();
                    matrix.reset();
                    if (rotation == 90) {
                        matrix.setRotate(90);
                    } else if (rotation == 180) {
                        matrix.setRotate(180);
                    } else if (rotation == 270) {
                        matrix.setRotate(270);
                        //
                    }
                    bitmap1 = Bitmap
                            .createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                                    bitmap.getHeight(), matrix, true);
//                    path = savePicture(bitmap1);
//                    prp.pic = path;
                    fieldvalue = recogBinder.doRecogDetail(prp);
                    nRet = recogBinder.getnRet();
                    if (nRet != 0) {

                        Toast.makeText(this, "验证失败", Toast.LENGTH_SHORT).show();
                    } else {

                        number = fieldvalue[0];
//                        color = fieldvalue[1];
                        mVibrator = (Vibrator) getApplication()
                                .getSystemService(Service.VIBRATOR_SERVICE);
                        mVibrator.vibrate(100);
                        closeCamera();
                        // 此模式下跳转 请到MemoryResultActivity 更改下代码 有注释注意查看
                        Intent intent = new Intent();
                        intent.putExtra("number", number);
//                        intent.putExtra("color", color);
//                        intent.putExtra("path", path);
                        // intent.putExtra("time", fieldvalue[11]);
//                        intent.putExtra("recogType", false);
//                        intent.putExtra("isatuo",true);//是否自动，true自动
                        setResult(RESULT_OK, intent);
                        this.finish();
                    }
                }
            } else {
                // System.out.println("视频流识别模式");

                prp.picByte = data;
                picData = data;
                if (rotation == 0) {
                    // 通知识别核心,识别前图像应先旋转的角度
                    prp.plateIDCfg.bRotate = 0;
                    setHorizontalRegion();
                } else if (rotation == 90) {

                    prp.plateIDCfg.bRotate = 1;
                    setLinearRegion();

                } else if (rotation == 180) {
                    prp.plateIDCfg.bRotate = 2;
                    setHorizontalRegion();
                } else if (rotation == 270) {
                    prp.plateIDCfg.bRotate = 3;
                    setLinearRegion();
                }
                if (isCamera) {
                    // 进行授权验证 并开始识别

                    fieldvalue = recogBinder.doRecogDetail(prp);

                    nRet = recogBinder.getnRet();

                    if (nRet != 0) {
                        String[] str = {"" + nRet};
                        getResult(str);
                    } else {
                        getResult(fieldvalue);
                        intentNV21data = data;
                    }

                }
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            initCamera(holder, initPreWidth, initPreHeight);
            getPreToChangView(preWidth, preHeight);

            if (rotation == 90 || rotation == 270) {
                myview = new PlateViewfinderView(ScenCameraActivity.this, width, height, true);
            } else {
                myview = new PlateViewfinderView(ScenCameraActivity.this, width, height, false);
            }
            re.addView(myview);

        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();

        OpenCameraAndSetParameters();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    // 设置横屏时的识别区域
    private void setHorizontalRegion() {
        this.prp.plateIDCfg.left = preWidth / 4;
        this.prp.plateIDCfg.top = preHeight / 4;
        this.prp.plateIDCfg.right = (preWidth / 4) + (preWidth / 2);
        this.prp.plateIDCfg.bottom = preHeight - (preHeight / 4);
        System.out.println("左  ：" + prp.plateIDCfg.left + "   右  ：" + prp.plateIDCfg.right + "     高：" + prp.plateIDCfg.top + "    底：" + prp.plateIDCfg.bottom);
    }

    // 设置竖屏时的识别区域
    private void setLinearRegion() {
        this.prp.plateIDCfg.left = preHeight / 24;
        this.prp.plateIDCfg.top = preWidth / 3;
        this.prp.plateIDCfg.right = (preHeight / 24) + ((preHeight * 11) / 12);
        this.prp.plateIDCfg.bottom = (preWidth / 3) + (preWidth / 3);
    }

    private void initCamera(SurfaceHolder holder, int setPreWidth, int setPreHeight) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> list = parameters.getSupportedPreviewSizes();
        Camera.Size size;

        int previewWidth = 480;
        int previewheight = 640;
        int second_previewWidth = 0;
        int second_previewheight = 0;
        if (list == null) {
            size = parameters.getPreviewSize();
            previewWidth = size.width;
            previewheight = size.height;
        } else if (list.size() == 1) {
            //设备只有一组预览分辨率
            size = list.get(0);
            previewWidth = size.width;
            previewheight = size.height;
        } else {
            Iterator paramPoint = list.iterator();
            Camera.Size localSize;
            //判断当前尺寸有没有正好是屏幕尺寸的
            do {
                if (!paramPoint.hasNext()) {
                    //遍历到最后都没找到当前屏幕尺寸的，选择最佳尺寸
                    localSize = getCloselyPreSize(parameters, setPreWidth, setPreWidth);
                    break;
                }
                localSize = (Camera.Size) paramPoint.next();
            } while ((localSize.width != setPreWidth) || (localSize.height != setPreHeight));
            previewWidth = localSize.width;
            previewheight = localSize.height;
        }
        preWidth = previewWidth;
        preHeight = previewheight;
        System.out.println("预览分辨率：" + preWidth + "    " + preHeight);
        parameters.setPictureFormat(PixelFormat.JPEG);
        parameters.setPreviewSize(preWidth, preHeight);
        if (parameters.getSupportedFocusModes().contains(
                parameters.FOCUS_MODE_CONTINUOUS_PICTURE)
                && !isAutoFocus) {
            isAutoFocus = false;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
            parameters
                    .setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        } else if (parameters.getSupportedFocusModes().contains(
                parameters.FOCUS_MODE_AUTO)) {
            isAutoFocus = true;
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        camera.setParameters(parameters);
        camera.setDisplayOrientation(rotation);
        try {
            camera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        camera.setPreviewCallback(this);  //设置相机回调的几个方法
        camera.startPreview();

    }


    private Camera.Size getCloselyPreSize(Camera.Parameters paramParameters, int paramWidth, int paramHeight) {
        float f3 = paramWidth / paramHeight;
        Iterator localIterator = paramParameters.getSupportedPreviewSizes().iterator();
        Camera.Size size2 = null;
        while (localIterator.hasNext()) {
            Camera.Size size = (Camera.Size) localIterator.next();
            if (f3 == ((float) size.width) / ((float) size.height) && size.width <= 1920 && width <= size.width) {
                size2 = size;
            }
        }
        if (paramWidth == 0 || paramHeight == 0) {
//            float f = AutoScrollHelper.NO_MAX;
            float f = 3.4028235E38F;

            while (localIterator.hasNext()) {
                Camera.Size size3 = (Camera.Size) localIterator.next();
                float abs = Math.abs(f3 - (((float) size3.width) / ((float) size3.height)));
                if (abs < f) {
                    size2 = size3;
                    f = abs;
                }
            }
            if (size2 == null) {
                Camera.Size defaltSize = paramParameters.getPreviewSize();
                size2 = defaltSize;
            }
        }
        return size2;
    }

    /**
     * @param @param fieldvalue 调用识别接口返回的数据
     * @return void 返回类型
     * @Title: getResult
     * @Description: TODO(获取结果)
     * @throwsbyte[]picdata
     */

    private void getResult(String[] fieldvalue) {

        if (nRet != 0)
        // 未通过验证 将对应错误码返回
        {
            Toast.makeText(this, "验证失败", Toast.LENGTH_SHORT).show();
        } else {
            // 通过验证 获取识别结果
            String result = "";
            String[] resultString;
            String timeString = "";
            String boolString = "";
            boolString = fieldvalue[0];

            if (boolString != null && !boolString.equals(""))
            // 检测到车牌后执行下列代码
            {

                resultString = boolString.split(";");
                int lenght = resultString.length;
                // Log.e("DEBUG", "nConfidence:" +
                // fieldvalue[4]);
                if (lenght > 0) {

                    String[] strarray = fieldvalue[4].split(";");

                    // 静态识别下 判断图像清晰度是否大于75

                    if (recogType ? true : Integer.valueOf(strarray[0]) > 75) {

                        tempData = recogBinder.getRecogData();

                        if (tempData != null) {

                            if (lenght == 1) {

                                if (fieldvalue[11] != null
                                        && !fieldvalue[11].equals("")) {
                                    int time = Integer.parseInt(fieldvalue[11]);
                                    time = time / 1000;
                                    timeString = "" + time;
                                } else {
                                    timeString = "null";
                                }

//								if (null != fieldname) {

                                BitmapFactory.Options options = new BitmapFactory.Options();
                                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                                options.inPurgeable = true;
                                options.inInputShareable = true;
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                                int Height = 0, Width = 0;
                                if (rotation == 90 || rotation == 270) {
                                    Height = preWidth;
                                    Width = preHeight;
                                } else if (rotation == 180 || rotation == 0) {
                                    Height = preHeight;
                                    Width = preWidth;
                                }
                                YuvImage yuvimage = new YuvImage(tempData,
                                        ImageFormat.NV21, Width, Height,
                                        null);
                                yuvimage.compressToJpeg(new Rect(0, 0,
                                        Width, Height), 100, baos);

                                bitmap = BitmapFactory.decodeByteArray(
                                        baos.toByteArray(), 0, baos.size(),
                                        options);

                                bitmap1 = Bitmap.createBitmap(bitmap, 0, 0,
                                        bitmap.getWidth(),
                                        bitmap.getHeight(), null, true);
//                                path = savePicture(bitmap1);

                                mVibrator = (Vibrator) getApplication()
                                        .getSystemService(
                                                Service.VIBRATOR_SERVICE);
                                mVibrator.vibrate(100);
                                closeCamera();
                                ///////高精度，识别走着
                                Intent intent = new Intent();
                                number = fieldvalue[0];
                                this.hpzl = getHpzl(fieldvalue[3]);
                                if ((this.hpzl.equals("44")) && (fieldvalue[1].equals("黄"))) {
                                    this.hpzl = "01";
                                }
                                if ((this.hpzl.equals("44")) && (fieldvalue[1].equals("蓝"))) {
                                    this.hpzl = "02";
                                }
                                if ((this.hpzl.equals("44")) && (fieldvalue[1].equals("绿"))) {
                                    this.hpzl = "52";
                                }
                                if ((this.hpzl.equals("44")) && (fieldvalue[1].equals("黄绿"))) {
                                    this.hpzl = "51";
                                }
                                if (this.number.contains("使")) {
                                    this.hpzl = "03";
                                } else if (this.number.contains("领")) {
                                    this.hpzl = "04";
                                } else if (this.number.contains("挂")) {
                                    this.hpzl = "15";
                                } else if (this.number.contains("学")) {
                                    this.hpzl = "16";
                                } else if (this.number.contains("港")) {
                                    this.hpzl = "26";
                                } else if (this.number.contains("澳")) {
                                    this.hpzl = "27";
                                } else if (this.number.contains("军")) {
                                    this.hpzl = "32";
                                }

                                this.color = getHpColor(fieldvalue[1], fieldvalue[3]);

                                intent.putExtra("number", number);
                                intent.putExtra("hpzl", hpzl);
                                intent.putExtra("color", color);
                                setResult(RESULT_OK, intent);
                                this.finish();


                            } else {
                                String itemString = "";

                                mVibrator = (Vibrator) getApplication()
                                        .getSystemService(
                                                Service.VIBRATOR_SERVICE);
                                mVibrator.vibrate(100);
                                closeCamera();
                                Intent intent = new Intent();
                                for (int i = 0; i < lenght; i++) {

                                    itemString = fieldvalue[0];
                                    resultString = itemString.split(";");
                                    number += resultString[i] + ";\n";

                                    itemString = fieldvalue[1];
                                    color += resultString[i] + ";\n";
                                    itemString = fieldvalue[11];
                                    resultString = itemString.split(";");

                                }

                                intent.putExtra("number", number);
                                setResult(RESULT_OK, intent);
                                this.finish();

                            }
                        }
                    }

                }

            } else
            // 未检测到车牌时执行下列代码
            {
                if (!recogType)
                // 预览识别执行下列代码 不是预览识别 不做处理等待下一帧
                {
                    ;
                    if (picData != null) {

                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                        options.inPurgeable = true;
                        options.inInputShareable = true;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        YuvImage yuvimage = new YuvImage(picData,
                                ImageFormat.NV21, preWidth, preHeight, null);
                        yuvimage.compressToJpeg(new Rect(0, 0, preWidth,
                                preHeight), 100, baos);
                        bitmap = BitmapFactory.decodeByteArray(
                                baos.toByteArray(), 0, baos.size(), options);

                        Matrix matrix = new Matrix();
                        matrix.reset();
                        if (rotation == 90) {
                            matrix.setRotate(90);
                        } else if (rotation == 180) {
                            matrix.setRotate(180);
                        } else if (rotation == 270) {
                            matrix.setRotate(270);
                            //
                        }
                        bitmap1 = Bitmap.createBitmap(bitmap, 0, 0,
                                bitmap.getWidth(), bitmap.getHeight(), matrix,
                                true);
//                        path = savePicture(bitmap1);

                        if (fieldvalue[11] != null
                                && !fieldvalue[11].equals("")) {
                            int time = Integer.parseInt(fieldvalue[11]);
                            time = time / 1000;
                            timeString = "" + time;
                        } else {
                            timeString = "null";
                        }

//						if (null != fieldname) {
                        mVibrator = (Vibrator) getApplication()
                                .getSystemService(Service.VIBRATOR_SERVICE);
                        mVibrator.vibrate(100);
                        closeCamera();
                        Intent intent = new Intent();
                        number = fieldvalue[0];
//                        color = fieldvalue[1];
                        if (fieldvalue[0] == null) {
                            number = "null";
                        }
//                        if (fieldvalue[1] == null) {
//                            color = "null";
//                        }
//                        int left = prp.plateIDCfg.left;
//                        int top = prp.plateIDCfg.top;
//                        int w = prp.plateIDCfg.right - prp.plateIDCfg.left;
//                        int h = prp.plateIDCfg.bottom - prp.plateIDCfg.top;

                        intent.putExtra("number", number);
//                        intent.putExtra("color", color);
////                        intent.putExtra("path", path);
//                        intent.putExtra("left", left);
//                        intent.putExtra("top", top);
//                        intent.putExtra("width", w);
//                        intent.putExtra("height", h);
//                        intent.putExtra("time", fieldvalue[11]);
//                        intent.putExtra("recogType", recogType);
//                        intent.putExtra("isatuo", true);//是否自动，true自动
                        setResult(RESULT_OK, intent);
                        this.finish();
//						}
                    }
                }
            }
        }

        nRet = -1;
        fieldvalue = null;
    }

    private String getHpColor(String color, String prehpzl) {
        String hpzlc = getHpzl(prehpzl);
        if ((hpzlc.equals("44")) && (color.equals("黄"))) {
            return "0";
        }
        if ((hpzlc.equals("44")) && (color.equals("蓝"))) {
            return "1";
        }
        if ((hpzlc.equals("44")) && (color.equals("绿"))) {
            return "5"; ////小型新能源车
        }
        if ((hpzlc.equals("44")) && (color.equals("黄绿"))) {
            return "5"; //大型新能源车
        }
        if ("1".equals(prehpzl)) {//小型汽车号
            return "1";
        }
        if ("3".equals(prehpzl)) {//大型汽车号
            return "0";
        }
        if ("4".equals(prehpzl)) {//大型汽车号
            return "0";
        }
        if ("5".equals(prehpzl)) {//警用汽车号
            return "4";
        }
        if ("6".equals(prehpzl)) {//武警号牌
            return "4";
        }
        if ("7".equals(prehpzl)) {//其他号牌
            return "-1";
        }
        if ("8".equals(prehpzl)) {//军队号牌
            return "4";
        }
        if ("9".equals(prehpzl)) {//军队号牌
            return "4";
        }
        if ("10".equals(prehpzl)) {//使馆汽车号
            return "3";
        }
        if ("11".equals(prehpzl)) {//香港入出境
            return "3";
        }
        if ("12".equals(prehpzl)) {//原农机号牌
            return "2";
        }
        if ("17".equals(prehpzl)) {//小型新能源
            return "5";
        }
        if ("18".equals(prehpzl)) {//大型新能源
            return "5";
        }
        return "-1";

    }


    private String getHpzl(String paramString) {
        if ("0".equals(paramString)) {
            return "44";
        }
        if ("1".equals(paramString)) {//小型汽车号
            return "02";
        }
        if ("2".equals(paramString)) {
            return "44";
        }
        if ("3".equals(paramString)) {//大型汽车号
            return "01";
        }
        if ("4".equals(paramString)) {//大型汽车号
            return "01";
        }
        if ("5".equals(paramString)) {//警用汽车号
            return "23";
        }
        if ("6".equals(paramString)) {//武警号牌
            return "31";
        }
        if ("7".equals(paramString)) {//其他号牌
            return "99";
        }
        if ("8".equals(paramString)) {//军队号牌
            return "32";
        }
        if ("9".equals(paramString)) {//军队号牌
            return "32";
        }
        if ("10".equals(paramString)) {//使馆汽车号
            return "03";
        }
        if ("11".equals(paramString)) {//香港入出境
            return "26";
        }
        if ("12".equals(paramString)) {//原农机号牌
            return "25";
        }
        if ("17".equals(paramString)) {//小型新能源
            return "52";
        }
        if ("18".equals(paramString)) {//大型新能源
            return "51";
        }
        return "99";
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        if (bitmap != null) {
            if (!bitmap.isRecycled()) {
                bitmap.recycle();
                bitmap = null;
            }

        }
        if (bitmap1 != null) {
            if (!bitmap1.isRecycled()) {
                bitmap1.recycle();
                bitmap1 = null;
            }

        }

        if (mVibrator != null) {
            mVibrator.cancel();
        }
        if (recogBinder != null) {
            unbindService(recogConn);
            recogBinder = null;
        }
    }

    /**
     * @return void 返回类型
     * @throws
     * @Title: closeCamera
     * @Description: TODO(这里用一句话描述这个方法的作用) 关闭相机
     */
    private void closeCamera() {
        // TODO Auto-generated method stub
        System.out.println("关闭相机 ");
        synchronized (this) {
            try {
                if (timer != null) {
                    timer.cancel();
                    timer = null;
                }
                if (time != null) {
                    time.cancel();
                    time = null;
                }
                if (camera != null) {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                    camera = null;
                }

            } catch (Exception e) {

            }
        }
    }


//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            closeCamera();
//            finish();
//        }
//        return super.onKeyDown(keyCode, event);
//    }

//    public String savePicture(Bitmap bitmap) {
//        String strCaptureFilePath = PATH + "plateID_" + pictureName() + ".jpg";
//        File dir = new File(PATH);
//        if (!dir.exists()) {
//            dir.mkdirs();
//        }
//        File file = new File(strCaptureFilePath);
//        if (file.exists()) {
//            file.delete();
//        }
//        try {
//            file.createNewFile();
//            BufferedOutputStream bos = new BufferedOutputStream(
//                    new FileOutputStream(file));
//
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//            bos.flush();
//            bos.close();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return strCaptureFilePath;
//    }

    public String pictureName() {
        String str = "";
        Time t = new Time();
        t.setToNow(); // 取得系统时间。
        int year = t.year;
        int month = t.month + 1;
        int date = t.monthDay;
        int hour = t.hour; // 0-23
        int minute = t.minute;
        int second = t.second;
        if (month < 10)
            str = String.valueOf(year) + "0" + String.valueOf(month);
        else {
            str = String.valueOf(year) + String.valueOf(month);
        }
        if (date < 10)
            str = str + "0" + String.valueOf(date + "_");
        else {
            str = str + String.valueOf(date + "_");
        }
        if (hour < 10)
            str = str + "0" + String.valueOf(hour);
        else {
            str = str + String.valueOf(hour);
        }
        if (minute < 10)
            str = str + "0" + String.valueOf(minute);
        else {
            str = str + String.valueOf(minute);
        }
        if (second < 10)
            str = str + "0" + String.valueOf(second);
        else {
            str = str + String.valueOf(second);
        }
        return str;
    }

    /**
     * @param @param context 设定文件
     * @return void 返回类型
     * @throws
     * @Title: setScreenSize
     * @Description: 这里用一句话描述这个方法的作用) 获取屏幕真实分辨率，不受虚拟按键影响
     */
    @SuppressLint("NewApi")
    private void setScreenSize(Context context) {
        int x, y;
        WindowManager wm = ((WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            Point screenSize = new Point();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                display.getRealSize(screenSize);
                x = screenSize.x;
                y = screenSize.y;
            } else {
                display.getSize(screenSize);
                x = screenSize.x;
                y = screenSize.y;
            }
        } else {
            x = display.getWidth();
            y = display.getHeight();
        }
        width = x;
        height = y;
    }

    //获取屏幕尺寸
    public void getScreenSize() {
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
    }

    //根据屏幕尺寸以及预览分辨率  给surfaceView重新定义尺寸，避免图像拉伸情况的出现
    public void getPreToChangView(int preWidth, int preHeight) {
        //横屏下
        if (width >= height) {
//			if(preWidth*screenHeight<preHeight*screenWidth){
//					//左右留边
//				int tempValue=screenHeight*preWidth/preHeight;
//					LayoutParams layoutParams= new LayoutParams(tempValue, RelativeLayout.TRUE);
//					layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//					surfaceView.setLayoutParams(layoutParams);
//			 }else if(preWidth*screenHeight>preHeight*screenWidth){//上下留边
//					int tempValue=screenWidth*preHeight/preWidth;
//				 LayoutParams layoutParams= new LayoutParams(RelativeLayout.TRUE, tempValue);
//				 layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//					surfaceView.setLayoutParams(layoutParams);
//			 }else if(preWidth*screenHeight==preHeight*screenWidth){
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.TRUE, RelativeLayout.TRUE);
            surfaceView.setLayoutParams(layoutParams);
//			 }
        }
        //竖屏下
        if (height >= width) {
//			if(preWidth*screenWidth<preHeight*screenHeight){//上下留边
//				int tempValue=screenWidth*preWidth/preHeight;
//					LayoutParams layoutParams= new LayoutParams(RelativeLayout.TRUE,tempValue);
//					layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//					surfaceView.setLayoutParams(layoutParams);
//				}else if(preWidth*screenWidth>preHeight*screenHeight){//左右留边
//					int tempValue=screenHeight*preHeight/preWidth;
//					LayoutParams layoutParams= new LayoutParams(tempValue, RelativeLayout.TRUE);
//					 layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
//					surfaceView.setLayoutParams(layoutParams);
//				}else if(preWidth*screenWidth==preHeight*screenHeight){
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.TRUE, RelativeLayout.TRUE);
            surfaceView.setLayoutParams(layoutParams);
//				}
        }
    }

    public void OpenCameraAndSetParameters() {
        try {
            if (null == camera) {
                camera = Camera.open();
                gestureDetector = new ScaleGestureDetector(this, new ScaleGestureListener());
//                surfaceView.setCamera(camera);
            }
            if (timer == null) {
                timer = new TimerTask() {
                    @Override
                    public void run() {
                        // isSuccess=false;
                        if (camera != null) {
                            try {
                                camera.autoFocus(new Camera.AutoFocusCallback() {
                                    @Override
                                    public void onAutoFocus(boolean success,
                                                            Camera camera) {
                                        // isSuccess=success;

                                    }
                                });
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    ;
                };
            }
            time = new Timer();
            time.schedule(timer, 500, 2500);
            if (!isFirstIn) {
                initCamera(holder, initPreWidth, initPreHeight);
            }
            isFirstIn = false;

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float mLastTouchX;
    private float mLastTouchY;
    /**
     * 向外放缩标志
     */
    private static final int ZOOM_OUT = 0;
    /**
     * 向内放缩标志
     */
    private static final int ZOOM_IN = 1;

    //重写onTouchEvent方法 获取手势
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //识别手势
        gestureDetector.onTouchEvent(event);
        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                mLastTouchX = event.getX();
                mLastTouchY = event.getY();
                break;
            }
            case MotionEvent.ACTION_UP: {
                break;
            }
            case MotionEvent.ACTION_POINTER_DOWN: {
                break;
            }
            case MotionEvent.ACTION_CANCEL: {
                break;
            }
        }
        return true;
    }

    //操作类
    class ScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        int mScaleFactor;

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor = (int) detector.getScaleFactor();
            Camera.Parameters params = camera.getParameters();
            int zoom = params.getZoom();
            if (mScaleFactor == ZOOM_IN) {
                if (zoom < params.getMaxZoom())
                    zoom += 1;
            } else if (mScaleFactor == ZOOM_OUT) {
                if (zoom > 0)
                    zoom -= 1;
            }
            params.setZoom(zoom);
            camera.setParameters(params);
            return false;
        }

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeCamera();
//        Intent intent = new Intent(MemoryCameraActivity.this,
//                MainActivity.class);
//        startActivity(intent);
        finish();

    }
}
