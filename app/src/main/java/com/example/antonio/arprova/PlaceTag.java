package com.example.antonio.arprova;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Antonio on 05/02/2018.
 * .
 */

public class PlaceTag extends View {

    private Place place;
    private float x, y;
    private Paint line;
    //Display disp = ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    //Display  sdisp = ((WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();

    public PlaceTag(Context context) {
        super(context);
    }

    public PlaceTag(Context context, Place p, int x, int y) {
        super(context);

        this.place = p;
        this.x = x;
        this.y = y;

        line = new Paint();
        line.setColor(p.getColor());
        line.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public PlaceTag(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public PlaceTag(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);

        canvas.drawCircle(x, y, 30, line);
    }
}
