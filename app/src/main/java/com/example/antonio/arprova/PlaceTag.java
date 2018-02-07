package com.example.antonio.arprova;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

/**
 * Created by Antonio on 05/02/2018.
 * .
 */

@SuppressLint("ViewConstructor") //non servono altri costruttori
public class PlaceTag extends View {

    private Place place;
    private String distanceTo;
    private float x, y;
    private Paint line, text;

    public PlaceTag(Context context, Place p, int x, int y, String distanceTo) {
        super(context);
        this.place = p;
        this.distanceTo = distanceTo;
        this.x = x;
        this.y = y;
        line = new Paint();
        line.setColor(p.getColor());
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(5);
        text = new Paint();
        text.setColor(p.getColor());
        text.setStyle(Paint.Style.FILL);
        text.setTextSize(25);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawLine(x, y, x, y - 150, line);
        canvas.drawText(place.getNome(), x + 15, y - 150 + 20, text);
        text.setTextSize(18);
        canvas.drawText(distanceTo, x + 15, y - 150 + 20 + 20, text);
    }
}
//150 lunghezza linea dovrebbe essere relativa a dimensione schermo o a distanza magari
//15 margine sinistro tra testo e linea
//20 (primo) margine top  = a dimensione testo per allineare sopra a fine linea
//20 (secondo) margine top per seconda linea di testo (per ora distanza)