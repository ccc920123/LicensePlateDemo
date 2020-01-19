package com.cdjysd.licenseplatelib.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

/**
 * 描述： 识别车牌的框框
 * 公司：四川星盾科技股份有限公司
 * 编写人：陈渝金-pc:chenyujin
 * 时间： 2019/12/13 10:51
 * 修改人：
 * 修改时间：
 */
public class PlateViewfinderView extends View {
    public int Hlength;
    public int Wlength;
    public Rect frame;
    public int height;
    private boolean isPortrait = true;
    public int left;
    private final Paint paint = new Paint();
    private final Paint paintLine = new Paint();
    public int top;
    public int width;

    public PlateViewfinderView(Context context, int i, int i2, boolean z) {
        super(context);
        this.width = i;
        this.height = i2;
        this.isPortrait = z;
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (this.isPortrait) {
            this.left = this.width / 24;
            this.top = this.height / 3;
            this.Wlength = (this.width * 11) / 12;
            this.Hlength = this.height / 3;
        } else {
            this.left = this.width / 4;
            this.top = this.height / 4;
            this.Wlength = this.width / 2;
            this.Hlength = this.height / 2;
        }
        this.frame = new Rect(this.left, this.top, this.left + this.Wlength, this.top + this.Hlength);
        this.paint.setColor(Color.argb(100, 0, 0, 0));
        canvas.drawRect(0.0f, 0.0f, (float) this.width, (float) this.frame.top, this.paint);
        canvas.drawRect(0.0f, (float) this.frame.top, (float) this.frame.left, (float) this.frame.bottom, this.paint);
        canvas.drawRect((float) this.frame.right, (float) this.frame.top, (float) this.width, (float) this.frame.bottom, this.paint);
        canvas.drawRect(0.0f, (float) this.frame.bottom, (float) this.width, (float) this.height, this.paint);
        this.paintLine.setColor(Color.rgb(255, 0, 0));
        this.paintLine.setStrokeWidth(12.0f);
        this.paintLine.setAntiAlias(true);
        canvas.drawLine((float) (this.frame.left + 7), (float) (this.frame.top + 7), (float) (this.frame.right - 7), (float) (this.frame.top + 7), this.paintLine);
        canvas.drawLine((float) (this.frame.left + 7), (float) (this.frame.top + 2), (float) (this.frame.left + 7), (float) (this.frame.bottom - 7), this.paintLine);
        canvas.drawLine((float) (this.frame.left + 2), (float) (this.frame.bottom - 7), (float) (this.frame.right - 7), (float) (this.frame.bottom - 7), this.paintLine);
        canvas.drawLine((float) (this.frame.right - 7), (float) (this.frame.top + 2), (float) (this.frame.right - 7), (float) (this.frame.bottom - 2), this.paintLine);
    }

    }

