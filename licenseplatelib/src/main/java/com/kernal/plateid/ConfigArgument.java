
package com.kernal.plateid;

import com.kernal.lisence.Common;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
/**
 * 描述：
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:37
 * 修改人：
 * 修改时间：
 */
public class ConfigArgument {
    public static final String TAG = "ConfigArgument";
    String cfg = "";
    String[] cfgs;
    TH_PlateIDCfg c_Config = new TH_PlateIDCfg();
    int dFormat = 0;
    int nPlateLocate_Th = 5;
    int nOCR_Th = 2;
    int bIsAutoSlope = 1;
    int nSlopeDetectRange = 0;
    String szProvince = "";
    int nContrast = 0;
    int tworowyellow = 3;
    int armpolice = 5;
    int tworowarmy = 7;
    int tractor = 9;
    int onlytworowyellow = 11;
    int embassy = 13;
    int onlylocation = 15;
    int armpolice2 = 17;

    public ConfigArgument() {
        try {
            this.cfg = this.readtxt();
            this.cfgs = this.cfg.split("==##");
            this.getConfig(this.cfgs);
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    public void getConfig(String[] cfgs) {
        if(cfgs != null && cfgs.length >= 12) {
            this.c_Config.nMinPlateWidth = this.String2Int(cfgs[0]);
            this.c_Config.nMaxPlateWidth = this.String2Int(cfgs[1]);
            this.c_Config.bVertCompress = this.String2Int(cfgs[2]);
            this.c_Config.bIsFieldImage = this.String2Int(cfgs[3]);
            this.c_Config.bOutputSingleFrame = this.String2Int(cfgs[4]);
            this.c_Config.bMovingImage = this.String2Int(cfgs[5]);
            this.c_Config.bIsNight = this.String2Int(cfgs[6]);
            this.c_Config.nImageFormat = this.String2Int(cfgs[7]);
            this.c_Config.nLastError = this.String2Int(cfgs[8]);
            this.c_Config.nErrorModelSN = this.String2Int(cfgs[9]);
            this.c_Config.reserved = cfgs[10];
        }

        if(cfgs != null && cfgs.length >= 18) {
            this.dFormat = this.String2Int(cfgs[11]);
            this.nPlateLocate_Th = this.String2Int(cfgs[12]);
            this.bIsAutoSlope = this.String2Int(cfgs[13]);
            this.nSlopeDetectRange = this.String2Int(cfgs[14]);
            this.szProvince = cfgs[15];
            this.nContrast = this.String2Int(cfgs[16]);
            this.nOCR_Th = this.String2Int(cfgs[17]);
        }

        if(cfgs != null && cfgs.length >= 26) {
            this.tworowyellow = this.String2Int(cfgs[18]);
            this.armpolice = this.String2Int(cfgs[19]);
            this.tworowarmy = this.String2Int(cfgs[20]);
            this.tractor = this.String2Int(cfgs[21]);
            this.onlytworowyellow = this.String2Int(cfgs[22]);
            this.embassy = this.String2Int(cfgs[23]);
            this.onlylocation = this.String2Int(cfgs[24]);
            this.armpolice2 = this.String2Int(cfgs[25]);
        }

    }

    public TH_PlateIDCfg getTH_PlateIDCfg() {
        return this.c_Config;
    }

    private int String2Int(String stri) {
        int nRet = 0;
        if(stri != null && !stri.equals("")) {
            try {
                nRet = Integer.parseInt(stri);
            } catch (Exception var4) {
                var4.printStackTrace();
            }
        }

        return nRet;
    }

    public String readtxt() throws IOException {
        Common common = new Common();
        String paths = common.getSDPath();
        if(!paths.equals("") && paths != null) {
            String fullpath = paths + "/wintone/plateid.cfg";
            File file = new File(fullpath);
            if(!file.exists()) {
                fullpath = paths + "/AndroidWT/plateid.cfg";
                File fileReader = new File(fullpath);
                if(!fileReader.exists()) {
                    return "";
                }
            }

            System.out.println("fullpath=" + fullpath);
            FileReader fileReader1 = new FileReader(fullpath);
            BufferedReader br = new BufferedReader(fileReader1);
            String str = "";

            for(String r = br.readLine(); r != null; r = br.readLine()) {
                str = str + r;
            }

            br.close();
            fileReader1.close();
            return str;
        } else {
            return "";
        }
    }
}
