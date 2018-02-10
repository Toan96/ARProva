package com.unisa_contest.toan.look_around.places;

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
//150 potrebbe essere relativa a dimensione schermo o a distanza place magari
//15  margine sinistro tra testo e linea
//20 (primo) margine top  = a dimensione testo per allineare sopra a fine linea
//20 (secondo) margine top per seconda linea di testo (indirizzo)
//20 (terzo) margine top per terza linea di testo (distanza)
        canvas.drawLine(x, y, x, y - 150, line);
        if (place.getNome().length() > 25) {
            canvas.drawText(place.getNome().substring(0, 24).concat("..."), x + 15, y - 150 + 20, text);
        } else {
            canvas.drawText(place.getNome(), x + 15, y - 150 + 20, text);
        }
        text.setTextSize(18);
        if (place.getIndirizzo().length() > 25) {
            canvas.drawText(place.getIndirizzo().substring(0, 24).concat("..."), x + 15, y - 150 + 20 + 20, text);
        } else {
            canvas.drawText(place.getIndirizzo(), x + 15, y - 150 + 20 + 20, text);
        }
        canvas.drawText(distanceTo, x + 15, y - 150 + 20 + 20 + 20, text);
    }
}
