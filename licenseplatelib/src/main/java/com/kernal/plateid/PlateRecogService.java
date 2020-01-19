
package com.kernal.plateid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.kernal.lisence.CDKey;
import com.kernal.lisence.Common;
import com.kernal.lisence.DateAuthFileOperate;
import com.kernal.lisence.DeviceFP;
import com.kernal.lisence.MachineCode;
import com.kernal.lisence.ModeAuthFileOperate;
import com.kernal.lisence.ModeAuthFileResult;
import com.kernal.lisence.ProcedureAuthOperate;
import com.kernal.lisence.VersionAuthFileOperate;
import com.kernal.lisence.WintoneLSCOperateTools;
import com.kernal.lisence.WintoneLSCXMLInformation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
/**
 * 描述：
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:36
 * 修改人：
 * 修改时间：
 */
public class PlateRecogService extends Service {
    public static final String TAG = "PlateRecogService";
    private static Common common = new Common();
    public PlateRecogService.MyBinder binder;
    private PlateIDAPI plateIDAPI;
    private ConfigArgument configArg;
    private int iTH_InitPlateIDSDK = -1;
    private int nRet = -1;
    private int nResultNum = 0;
    public String returnGetVersion = "";
    int imageformat = 1;
    int bVertFlip = 0;
    int bDwordAligned = 1;
    private Boolean new_lsc_Boolean = Boolean.valueOf(false);
    private Boolean isTF = Boolean.valueOf(false);
    private ModeAuthFileResult mafr = new ModeAuthFileResult();
    private String productType = "10";
    public static boolean initializeType = false;
    public static boolean recogModel = true;//精准模式
    private byte[] data = null;

    public PlateRecogService() {
    }

    public IBinder onBind(Intent intent) {
        return this.binder;
    }

    private String readAssetFile(String filename) {
        String typeModeString = null;

        try {
            InputStream e = this.getClass().getResourceAsStream(filename);
            int size_is = e.available();
            byte[] byte_new = new byte[size_is];
            e.read(byte_new);
            e.close();
            typeModeString = new String(byte_new);
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return typeModeString;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.iTH_InitPlateIDSDK = -10003;
        this.binder = new PlateRecogService.MyBinder();
        this.configArg = new ConfigArgument();
        String miwenxml = this.readAssetFile("/assets/ocr/authmode.lsc");
        ModeAuthFileOperate mafo = new ModeAuthFileOperate();
        this.mafr = mafo.ReadAuthFile(miwenxml);
        if(miwenxml != null && this.mafr.isTF(this.productType)) {
            this.isTF = Boolean.valueOf(true);
            this.plateIDAPI = new PlateIDAPI();
            TH_PlateIDCfg telephonyManager1 = new TH_PlateIDCfg();
            DeviceFP sb1 = new DeviceFP();
            if(initializeType && recogModel) {
                telephonyManager1.bMovingImage = 2;
            }

            this.iTH_InitPlateIDSDK = this.plateIDAPI.TH_InitPlateIDSDKTF(telephonyManager1, sb1);
            Log.i("PlateRecogService", "InitSDK_TF=" + this.iTH_InitPlateIDSDK);
            System.out.println("InitSDK_TF=" + this.iTH_InitPlateIDSDK);
        } else {
            TelephonyManager telephonyManager = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
            StringBuilder sb = new StringBuilder();
            sb.append(telephonyManager.getDeviceId());
            String deviceId = sb.toString();
            System.out.println("deviceId:" + deviceId);
            StringBuilder sb1 = new StringBuilder();
            sb1.append(Secure.getString(this.getContentResolver(), "android_id"));
            String androId = sb1.toString();
            StringBuilder sb2 = new StringBuilder();
            sb2.append(telephonyManager.getSimSerialNumber());
            String simId = sb2.toString();
            if(miwenxml != null && this.mafr.isSIM(this.productType)) {
                if(simId == null || simId.equals("") || simId.equals("null")) {
                    this.iTH_InitPlateIDSDK = -10501;
                    return;
                }

                deviceId = simId;
            }

            MachineCode machineCode = new MachineCode();
            String mcode = machineCode.MachineNO("1.0", deviceId, androId, simId);
            String[] str = new String[]{"", "", "", ""};
            DeviceFP deviceFP = new DeviceFP();
            String path = Environment.getExternalStorageDirectory() + "/wintone/plateid.lsc";
            String wintonePathString = Environment.getExternalStorageDirectory() + "/AndroidWT/wt.lsc";
            String wintoneDateFilePath = Environment.getExternalStorageDirectory() + "/AndroidWT/wtdateinit.lsc";
            String versionInitFilePatnString = Environment.getExternalStorageDirectory() + "/AndroidWT/wtversioninit.lsc";
            File file = new File(path);
            File wintoneFile = new File(wintonePathString);
            File wintoneDateFile = new File(wintoneDateFilePath);
            File versionInitFile = new File(versionInitFilePatnString);
            WintoneLSCXMLInformation wli = null;
            String cdkeyString = "";
            String serialString = "";
            boolean fleg = false;
            if(miwenxml != null && this.mafr.isCheckPRJMode(this.productType)) {
                fleg = true;
                deviceFP.deviceid = "DeviceIdIsNull";
            } else if(versionInitFile.exists()) {
                if(telephonyManager.getDeviceId() == null) {
                    fleg = true;
                } else if(telephonyManager.getDeviceId().equals(this.readInitFileString(versionInitFilePatnString))) {
                    deviceFP.deviceid = this.readInitFileString(versionInitFilePatnString);
                    fleg = true;
                }
            } else if(wintoneDateFile.exists()) {
                if(telephonyManager.getDeviceId() == null) {
                    fleg = true;
                } else if(telephonyManager.getDeviceId().equals(this.readInitFileString(wintoneDateFilePath))) {
                    deviceFP.deviceid = this.readInitFileString(wintoneDateFilePath);
                    fleg = true;
                }
            } else if(file.exists() || wintoneFile.exists() || !wintoneFile.exists()) {
                ProcedureAuthOperate c_Config = new ProcedureAuthOperate(this);
                if(file.exists()) {
                    try {
                        str = c_Config.readOriginalAuthFileContent(path);
                        deviceFP.deviceid = str[3];
                        cdkeyString = str[2];
                        serialString = str[1];
                        CDKey telephonyManager11 = new CDKey();
                        fleg = telephonyManager11.checkjhm(cdkeyString, mcode, serialString);
                        if(!fleg && str.length >= 8) {
                            if(str[8] != null && str[7] != null) {
                                deviceFP.deviceid = str[9];
                            }

                            fleg = telephonyManager11.checkjhm(str[8], mcode, str[7]);
                        }
                    } catch (Exception var29) {
                        serialString = "";
                        cdkeyString = "";
                    }
                } else {
                    System.out.println("读取授权文件");
                    TelephonyManager telephonyManager1 = (TelephonyManager)this.getSystemService(TELEPHONY_SERVICE);
                    if(telephonyManager1.getDeviceId() != null && !telephonyManager1.getDeviceId().equals("")) {
                        wli = WintoneLSCOperateTools.ReadAuthFile(telephonyManager1.getDeviceId());
                    } else {
                        wli = WintoneLSCOperateTools.ReadAuthFile(Secure.getString(this.getContentResolver(), "android_id"));
                    }

                    if(wli != null) {
                        cdkeyString = wli.anoString;
                        serialString = wli.snoString;
                        deviceFP.deviceid = wli.deviceIdString;
                        CDKey cdKey = new CDKey();
                        fleg = cdKey.checkjhm(cdkeyString, mcode, serialString);
                        if(!fleg && str.length >= 8) {
                            if(str[8] != null && str[7] != null) {
                                deviceFP.deviceid = str[9];
                            }

                            fleg = cdKey.checkjhm(str[8], mcode, str[7]);
                        }
                    }
                }
            }

            if(fleg) {
                if(telephonyManager.getDeviceId() == null) {
                    deviceFP.deviceid = "DeviceIdIsNull";
                }

                this.plateIDAPI = new PlateIDAPI();
                TH_PlateIDCfg c_Config1 = new TH_PlateIDCfg();
                if(initializeType && recogModel) {
                    c_Config1.bMovingImage = 2;
                }

                this.iTH_InitPlateIDSDK = this.plateIDAPI.TH_InitPlateIDSDK(c_Config1, telephonyManager, deviceFP);
            } else {
                this.iTH_InitPlateIDSDK = -10015;
            }
        }

    }

    private String[] recogPlate(byte[] picByte, String pic, int width, int height, String userData, TH_PlateIDCfg plateIDCfg) {
        String[] fieldvalue = new String[15];
        int[] nResultNums = new int[]{10};
        int[] nRets = new int[]{-1};
        TH_PlateIDResult plateidresult = new TH_PlateIDResult();
        TH_PlateIDResult[] plateidresults=null;
        if(picByte != null && picByte.length > 0) {
            plateidresults = this.plateIDAPI.TH_RecogImageByte(picByte, width, height, plateidresult, nResultNums, plateIDCfg.left, plateIDCfg.top, plateIDCfg.right, plateIDCfg.bottom, nRets, plateIDCfg.bRotate, plateIDCfg.scale);
        }
      /*  else {
            plateidresults = this.plateIDAPI.TH_RecogImage(pic, width, height, plateidresult, nResultNums, 0, 0, 0, 0, nRets);
        }*/

        this.nRet = nRets[0];
        this.nResultNum = nResultNums[0];
        if(nRets[0] != 0) {
            fieldvalue[14] = userData;
        } else {
            fieldvalue[14] = userData;

            for(int i = 0; i < nResultNums[0]; ++i) {
                if(plateidresults != null && plateidresults[i] != null) {
                    if(i == 0) {
                        fieldvalue[0] = plateidresults[i].license;
                        fieldvalue[1] = plateidresults[i].color;
                        fieldvalue[2] = this.int2string(plateidresults[i].nColor);
                        fieldvalue[3] = this.int2string(plateidresults[i].nType);
                        fieldvalue[4] = this.int2string(plateidresults[i].nConfidence);
                        fieldvalue[5] = this.int2string(plateidresults[i].nBright);
                        fieldvalue[6] = this.int2string(plateidresults[i].nDirection);
                        fieldvalue[7] = this.int2string(plateidresults[i].left);
                        fieldvalue[8] = this.int2string(plateidresults[i].top);
                        fieldvalue[9] = this.int2string(plateidresults[i].right);
                        fieldvalue[10] = this.int2string(plateidresults[i].bottom);
                        fieldvalue[11] = this.int2string(plateidresults[i].nTime);
                        fieldvalue[12] = this.int2string(plateidresults[i].nCarBright);
                        fieldvalue[13] = this.int2string(plateidresults[i].nCarColor);
                        fieldvalue[14] = userData;
                        this.data = plateidresults[i].pbyBits;
                    } else {
                        fieldvalue[0] = fieldvalue[0] + ";" + plateidresults[i].license;
                        fieldvalue[1] = fieldvalue[1] + ";" + plateidresults[i].color;
                        fieldvalue[2] = fieldvalue[2] + ";" + this.int2string(plateidresults[i].nColor);
                        fieldvalue[3] = fieldvalue[3] + ";" + this.int2string(plateidresults[i].nType);
                        fieldvalue[4] = fieldvalue[4] + ";" + this.int2string(plateidresults[i].nConfidence);
                        fieldvalue[5] = fieldvalue[5] + ";" + this.int2string(plateidresults[i].nBright);
                        fieldvalue[6] = fieldvalue[6] + ";" + this.int2string(plateidresults[i].nDirection);
                        fieldvalue[7] = fieldvalue[7] + ";" + this.int2string(plateidresults[i].left);
                        fieldvalue[8] = fieldvalue[8] + ";" + this.int2string(plateidresults[i].top);
                        fieldvalue[9] = fieldvalue[9] + ";" + this.int2string(plateidresults[i].right);
                        fieldvalue[10] = fieldvalue[10] + ";" + this.int2string(plateidresults[i].bottom);
                        fieldvalue[11] = fieldvalue[11] + ";" + this.int2string(plateidresults[i].nTime);
                        fieldvalue[12] = fieldvalue[12] + ";" + this.int2string(plateidresults[i].nCarBright);
                        fieldvalue[13] = fieldvalue[13] + ";" + this.int2string(plateidresults[i].nCarColor);
                    }
                }
            }
        }

        System.out.println("nRet:" + this.nRet);
        plateidresults = null;
        return fieldvalue;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(this.iTH_InitPlateIDSDK == 0) {
            int iTH_UninitPlateIDSDK = this.plateIDAPI.TH_UninitPlateIDSDK();
            Log.i("PlateRecogService", "iTH_UninitPlateIDSDK=" + iTH_UninitPlateIDSDK);
        }

        this.data = null;
    }

    public String readInitFileString(String filePathString) {
        String SysCertVersion = "wtversion5_5";
        String deviceidString = "";
        File dateInitFile = new File(filePathString);
        if(dateInitFile.exists()) {
            try {
                BufferedReader e = new BufferedReader(new FileReader(dateInitFile));
                deviceidString = e.readLine();
                e.close();
                deviceidString = common.getSrcPassword(deviceidString, SysCertVersion);
            } catch (FileNotFoundException var6) {
                deviceidString = "";
                var6.printStackTrace();
            } catch (IOException var7) {
                deviceidString = "";
                var7.printStackTrace();
            } catch (Exception var8) {
                deviceidString = "";
                var8.printStackTrace();
            }
        }

        return deviceidString;
    }

    private String int2string(int i) {
        String str = "";

        try {
            str = String.valueOf(i);
        } catch (Exception var4) {
            ;
        }

        return str;
    }

    public class MyBinder extends Binder {
        public MyBinder() {
        }


        public void setRecogArgu(PlateCfgParameter cfgparameter, int imageformat, int bVertFlip, int bDwordAligned) {
            PlateRecogService.this.nRet = -10000;
            if (PlateRecogService.this.iTH_InitPlateIDSDK != 0) {
                PlateRecogService.this.nRet = -10001;
            } else {
                PlateRecogService.this.plateIDAPI.TH_SetImageFormat(imageformat, bVertFlip, bDwordAligned);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.individual);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.tworowyellow);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.armpolice);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.tworowarmy);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.tractor);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.onlytworowyellow);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.embassy);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.onlylocation);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.armpolice2);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.newEnergy);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.consulate);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.Infactory);
                PlateRecogService.this.plateIDAPI.TH_SetEnabledPlateFormat(cfgparameter.civilAviation);
                PlateRecogService.this.plateIDAPI.TH_SetRecogThreshold(cfgparameter.nPlateLocate_Th, cfgparameter.nOCR_Th);
                PlateRecogService.this.plateIDAPI.TH_SetAutoSlopeRectifyMode(cfgparameter.bIsAutoSlope, cfgparameter.nSlopeDetectRange);
                PlateRecogService.this.plateIDAPI.TH_SetProvinceOrder(cfgparameter.szProvince);
//                if (cfgparameter.nContrast != 0) {
//                    iTH_SetDayNightMode = PlateRecogService.this.plateIDAPI.TH_SetContrast(cfgparameter.nContrast);
//                } else {
//                    boolean iTH_SetDayNightMode1 = false;
//                }
//
//                iTH_SetDayNightMode = PlateRecogService.this.plateIDAPI.TH_SetDayNightMode(cfgparameter.bIsNight);
                PlateRecogService.this.nRet = 0;
            }

        }

        public String[] doRecogDetail(PlateRecognitionParameter prp) {
            PlateRecogService.this.new_lsc_Boolean = Boolean.valueOf(false);
            String[] fieldvalue = null;
            if(PlateRecogService.this.iTH_InitPlateIDSDK != 0) {
                PlateRecogService.this.nRet = -10001;
            } else {
                boolean devCheck = true;
                int devCheck1;
                if(!PlateRecogService.this.mafr.isCheckDevType(PlateRecogService.this.productType) && !prp.isCheckDevType) {
                    devCheck1 = 0;
                } else {
                    devCheck1 = PlateRecogService.this.mafr.isAllowDevTypeAndDevCode(PlateRecogService.this.productType, prp.devCode);
                }

                boolean check = true;
                int check1;
                if(prp.versionfile != null && !prp.versionfile.equals("")) {
                    VersionAuthFileOperate file3 = new VersionAuthFileOperate();
                    check1 = file3.verifyVersionAuthFile(prp.versionfile, prp.devCode, PlateRecogService.this.productType, "");
                } else if(prp.dataFile != null && !prp.dataFile.equals("") && !prp.dataFile.equals("null")) {
                    DateAuthFileOperate file2 = new DateAuthFileOperate();
                    check1 = file2.verifyDateAuthFile(prp.dataFile, prp.devCode, PlateRecogService.this.productType);
                    if(check1 == -10090 && devCheck1 == 0) {
                        if(MathRandom.PercentageRandom() == 5) {
                            Toast.makeText(PlateRecogService.this.getApplicationContext(), "您的授权已到期，请在�?��月内申�\ue1ec延期，否则将不能使用识别功能", Toast.LENGTH_SHORT).show();
                        }

                        check1 = 0;
                    }
                } else if(PlateRecogService.this.isTF.booleanValue()) {
                    check1 = 0;
                } else if(PlateRecogService.this.mafr.isCheckPRJMode(PlateRecogService.this.productType)) {

                    check1=0;
                } else {
//                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/wintone/plateid.lsc");
//                    if(file.exists()) {
//                        check1 = 0;
//                    } else {
//                        PlateRecogService.this.new_lsc_Boolean = Boolean.valueOf(true);
//                        WintoneLSCXMLInformation wlxi = null;
//                        TelephonyManager telephonyManager1 = (TelephonyManager)PlateRecogService.this.getSystemService(TELEPHONY_SERVICE);
//                        if(telephonyManager1.getDeviceId() != null && !telephonyManager1.getDeviceId().equals("")) {
//                            wlxi = WintoneLSCOperateTools.ReadAuthFile(telephonyManager1.getDeviceId());
//                        } else {
//                            wlxi = WintoneLSCOperateTools.ReadAuthFile(Secure.getString(PlateRecogService.this.getContentResolver(), "android_id"));
//                        }
//
//                        check1 = WintoneAuthOperateTools.accordTypeDateNumber(PlateRecogService.this.productType, wlxi.typeStrings, wlxi.duedateStrings, wlxi.sumStrings);
//                    }
                    check1=0;
                }

                if(check1 == 0 && devCheck1 == 0) {
                    fieldvalue = PlateRecogService.this.recogPlate(prp.picByte, prp.pic, prp.width, prp.height, prp.userData, prp.plateIDCfg);
                    if(PlateRecogService.this.new_lsc_Boolean.booleanValue()) {
                        WintoneLSCOperateTools.ModifyNumberInAuthFileByProjectType(PlateRecogService.this.productType);
                    }
                } else {
                    PlateRecogService.this.nRet = check1;
                    if(devCheck1 != 0) {
                        PlateRecogService.this.nRet = devCheck1;
                    }

                    fieldvalue = new String[15];
                    fieldvalue[14] = prp.userData;
                }
            }

            return fieldvalue;
        }

        public String[] doRecogDetail(byte[] picByte, String pic, int width, int height, String userData) {
            PlateRecognitionParameter prp = new PlateRecognitionParameter();
            prp.picByte = picByte;
            prp.pic = pic;
            prp.width = width;
            prp.height = height;
            prp.userData = userData;
            return this.doRecogDetail(prp);
        }

        public String[] doRecog(String pic, int width, int height) {
            return this.doRecogDetail((byte[])null, pic, width, height, (String)null);
        }

        public String[] doRecog(String pic, int width, int height, String userData) {
            return this.doRecogDetail((byte[])null, pic, width, height, userData);
        }

        public String[] doRecog(byte[] picByte, int width, int height, String userData) {
            PlateRecogService.this.imageformat = 6;
            PlateRecogService.this.plateIDAPI.TH_SetImageFormat(PlateRecogService.this.imageformat, PlateRecogService.this.bVertFlip, PlateRecogService.this.bDwordAligned);
            return this.doRecogDetail(picByte, (String)null, width, height, userData);
        }

        public int getInitPlateIDSDK() {
            return PlateRecogService.this.iTH_InitPlateIDSDK;
        }

        public int getnRet() {
            return PlateRecogService.this.nRet;
        }

        public int getnResultNums() {
            return PlateRecogService.this.nResultNum;
        }

        public int UninitPlateIDSDK() {
            int iTH_UninitPlateIDSDK = -1;
            if(PlateRecogService.this.iTH_InitPlateIDSDK == 0) {
                iTH_UninitPlateIDSDK = PlateRecogService.this.plateIDAPI.TH_UninitPlateIDSDK();
                Log.v("PlateRecogService", "iTH_UninitPlateIDSDK=" + iTH_UninitPlateIDSDK);
            }

            return iTH_UninitPlateIDSDK;
        }

        public byte[] getRecogData() {
            return PlateRecogService.this.data;
        }
    }
}
