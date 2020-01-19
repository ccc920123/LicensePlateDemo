package com.cdjysd.licenseplatelib.utils;

import android.app.Activity;
import android.content.Intent;
import androidx.fragment.app.FragmentActivity;
import android.util.Log;

import com.cdjysd.licenseplatelib.ui.activity.ScenCameraActivity;


/**
 * 描述：
 * 公司：四川星盾科技股份有限公司
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13
 * 修改人：
 * 修改时间：
 */


public class LPalte {

    public static void openScanPlate(Activity activity, int requestCode) {

        try {
            Intent itt = new Intent();
            itt.setClass(activity, ScenCameraActivity.class);
            activity.startActivityForResult(itt, requestCode);


        } catch (Exception e) {
            Log.e("LPalte", e.getMessage());
        }

    }


    public static void openScanPlate(FragmentActivity fragmentActivity, int requestCode) {
        {
            try {
                Intent itt = new Intent();
                itt.setClass(fragmentActivity, ScenCameraActivity.class);
                fragmentActivity.startActivityForResult(itt, requestCode);


            } catch (Exception e) {
                Log.e("LPalte", e.getMessage());
            }

        }

    }
}
