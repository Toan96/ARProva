package com.example.antonio.arprova;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.WindowManager;

import static android.os.SystemClock.sleep;

/**
 * Created by Antonio on 04/02/2018.
 * .
 */

public class PlaceDrawerASync extends AsyncTask<Object, Object, Object> {

    @Override
    protected void onPreExecute() {
        Log.d("ASync: ", "started");
    }

    @Override
    protected Object doInBackground(Object[] objects) {
        Log.d("ASync: ", "doInBackground");
        while (Utils.myLocation == null) {
            Log.d("ASync: ", "waiting for location...");
            sleep(2000);
        }

        //take screen height
        Context context = (Context) objects[0];
        int height = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getHeight();
        height /= 2;//todo altezza a cui disegnare tag
        //todo prova x
        int width = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getWidth();
        width /= 2;

        while (true) {
            for (Place p : Utils.mockPlaces) {
                //solo se mappa piccola forse
                float distanceTo = Utils.myLocation.distanceTo(p.getLocationData());
                //se la distanza di place > di raggio della mappa allora non mostrare, vai avanti
                if (distanceTo > Utils.visibleDistance || distanceTo < 0)
                    continue;
                float bearingTo = Utils.myLocation.bearingTo(p.getLocationData());
                if (bearingTo > 360)
                    bearingTo %= 360;
                if (bearingTo < 0)
                    //non cambiare, dovrebbe funzionare perché bearingTo è negativo
                    bearingTo = 360 + bearingTo;
                //se orientamento di place troppo distante dal mio allora non mostrare
                if ((bearingTo > (Utils.currentBearing + Utils.BEARING_OFFSET)) ||
                        (bearingTo < (Utils.currentBearing - Utils.BEARING_OFFSET)))
                    continue;
                Object[] toDraw = {context, p, width, height};
                publishProgress(toDraw);//dovrebbe fare il redraw di i e coordinate
            }
        }
    }

    @Override
    protected void onProgressUpdate(Object... values) {
        Log.d("ASync update: ", ((Place) values[1]).getNome());
        new PlaceTag((Context) values[0], (Place) values[1], (int) values[2], (int) values[3]);
    }

    @Override
    protected void onPostExecute(Object result) {
        Log.d("ASync: ", "onPostExecute " + result);
    }
}
