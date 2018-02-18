package com.unisa_contest.toan.look_around.places;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import com.unisa_contest.toan.look_around.Utils;

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
    //text size nome (25) e info (17), strokeWidth(5)

    public PlaceTag(Context context, Place p, int x, int y, String distanceTo) {
        super(context);
        this.place = p;
        this.distanceTo = distanceTo;
        this.x = x;
        this.y = y;
        line = new Paint();
        line.setColor(p.getColor());
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeWidth(5);//update commento su
        text = new Paint();
        text.setColor(p.getColor());
        text.setStyle(Paint.Style.FILL);
        text.setTextSize(25);//update commento su
        if (Utils.isColorDark(p.getColor()))
            text.setShadowLayer(2.0f, 0.0f, 0.0f, Color.WHITE);
        else text.setShadowLayer(2.0f, 0.0f, 0.0f, Color.BLACK);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int LINE_LENGTH = 150; //LINE_LENGTH potrebbe essere relativa a dimensione schermo o a distanza place magari
        final int LEFT_MARGIN = 15; //LEFT_MARGIN  margine sinistro tra testo e linea
        final int TOP_MARGIN = 20; //TOP_MARGIN (primo) margine top  = a dimensione testo per allineare sopra a fine linea
        //TOP_MARGIN (secondo) margine top per seconda linea di testo (indirizzo)
        //TOP_MARGIN (terzo) margine top per terza linea di testo (distanza)

//rotate si basa su dimensioni schermo "normali" (potrebbe essere necessario aggiungere currentRoll ai lati (al bearing in DrawerAsync)
        if ((null != Utils.usedSensor) && (Utils.usedSensor.equals("rotationVector")))
            canvas.rotate(-Utils.currentRoll);

//todo evitare meglio sovrapposizione tag
        //to avoid tag overlapping
        if (place.getColor() % 2 == 0) { //sposta il tag verso l'alto
            canvas.translate((float) (place.getColor() * 0.0000015), (float) (place.getColor() * 0.000013));
        } else { //sposta il tag verso il basso
            canvas.translate((float) -(place.getColor() * 0.0000015), (float) -(place.getColor() * 0.000023));
        }

        //draw
        canvas.drawLine(x, y, x, y - LINE_LENGTH, line);
        if (place.getNome().length() > 25) {
            canvas.drawText(place.getNome().substring(0, 24).concat("..."), x + LEFT_MARGIN, y - LINE_LENGTH + TOP_MARGIN, text);
        } else {
            canvas.drawText(place.getNome(), x + LEFT_MARGIN, y - LINE_LENGTH + TOP_MARGIN, text);
        }
        text.setTextSize(17);//update commento su
        if (place.getIndirizzo().length() > 25) {
            canvas.drawText(place.getIndirizzo().substring(0, 24).concat("..."), x + LEFT_MARGIN, y - LINE_LENGTH + TOP_MARGIN + TOP_MARGIN, text);
        } else {
            canvas.drawText(place.getIndirizzo(), x + LEFT_MARGIN, y - LINE_LENGTH + TOP_MARGIN + TOP_MARGIN, text);
        }
        canvas.drawText(distanceTo, x + LEFT_MARGIN, y - LINE_LENGTH + TOP_MARGIN + TOP_MARGIN + TOP_MARGIN, text);
    }
}
