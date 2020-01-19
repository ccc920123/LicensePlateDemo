
package com.kernal.plateid;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.kernal.lisence.DateAuthFileOperate;
import com.kernal.lisence.MachineCode;
import com.kernal.lisence.ModeAuthFileOperate;
import com.kernal.lisence.ModeAuthFileResult;
import com.kernal.lisence.ProcedureAuthOperate;
import com.kernal.lisence.VersionAuthFileOperate;

import java.io.IOException;
import java.io.InputStream;

/**
 * 描述：
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:37
 * 修改人：
 * 修改时间：
 */
public class AuthService extends Service {
    public static final String TAG = "AuthService";
    public AuthService.MyBinder binder;
    int nRet = -1;
    private String mcode;
    private String deviceId;
    private String androId;
    private String simId;
    private TelephonyManager telephonyManager;
    private String productType = "10";
    private ModeAuthFileResult mafr = new ModeAuthFileResult();

    public AuthService() {
    }

    @Override
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
        this.telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        this.binder = new AuthService.MyBinder();
        this.telephonyManager = (TelephonyManager) this.getSystemService(TELEPHONY_SERVICE);
        StringBuilder sb = new StringBuilder();
        sb.append(this.telephonyManager.getDeviceId());
        this.deviceId = sb.toString();
        StringBuilder sb1 = new StringBuilder();
        sb1.append(Secure.getString(this.getContentResolver(), "android_id"));
        this.androId = sb1.toString();
        StringBuilder sb2 = new StringBuilder();
        sb2.append(this.telephonyManager.getSimSerialNumber());
        this.simId = sb2.toString();
        String miwenxml = this.readAssetFile("/assets/ocr/authmode.lsc");
        ModeAuthFileOperate mafo = new ModeAuthFileOperate();
        this.mafr = mafo.ReadAuthFile(miwenxml);
        if (miwenxml != null && this.mafr.isSIM(this.productType)) {
            if (this.simId != null && !this.simId.equals("") && !this.simId.equals("null")) {
                this.deviceId = this.simId;
            } else {
                Log.e("DEBUG", "DEBUG 10501");
                this.nRet = -10501;
            }
        }

        MachineCode machineCode = new MachineCode();
        this.mcode = machineCode.MachineNO("1.0", this.deviceId, this.androId, this.simId);
    }

    public class MyBinder extends Binder {
        String did = "";

        public MyBinder() {
        }

        public int getAuth(String sn, String authFile) throws Exception {
            ProcedureAuthOperate pao = new ProcedureAuthOperate(AuthService.this);
            AuthService.this.nRet = pao.getWintoneAuth(AuthService.this.productType, sn, authFile, AuthService.this.mcode, AuthService.this.deviceId, AuthService.this.androId, AuthService.this.simId, "");
            return AuthService.this.nRet;
        }

        public int getAuth(com.kernal.plateid.PlateAuthParameter pap) {
            if (AuthService.this.nRet == -10501) {
                return AuthService.this.nRet;
            } else {
                String productversionfile = pap.versionfile;
                String devcode = pap.devCode;
                if (productversionfile != null && !productversionfile.equals("")) {
                    VersionAuthFileOperate pao1 = new VersionAuthFileOperate();
                    AuthService.this.nRet = pao1.getVersionAuthFile(productversionfile, devcode, AuthService.this.productType, "", AuthService.this.deviceId);
                } else {
                    ProcedureAuthOperate pao = new ProcedureAuthOperate(AuthService.this);
                    if (pap.dataFile != null && !pap.dataFile.equals("") && !pap.dataFile.equals("null")) {
                        DateAuthFileOperate dafo = new DateAuthFileOperate();
                        AuthService.this.nRet = dafo.getDateAuth(pap.dataFile, pap.devCode, AuthService.this.productType, AuthService.this.deviceId);
                        if (AuthService.this.nRet == -10090) {
                            AuthService.this.nRet = 0;
                        }
                    } else {
                        AuthService.this.nRet = pao.getWintoneAuth(AuthService.this.productType, pap.sn, pap.authFile, AuthService.this.mcode, AuthService.this.deviceId, AuthService.this.androId, AuthService.this.simId, pap.server);
                    }
                }

                return AuthService.this.nRet;
            }
        }

        public int getnRet() {
            return AuthService.this.nRet;
        }

        public String getDeviceId() {
            return this.did;
        }
    }
}
