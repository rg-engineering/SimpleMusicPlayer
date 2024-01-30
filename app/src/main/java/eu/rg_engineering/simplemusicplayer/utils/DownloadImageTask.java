package eu.rg_engineering.simplemusicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

import io.sentry.Sentry;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {

    private final String TAG = "DownloadImageTask";
    ImageView mImage;
    String mName;

    public DownloadImageTask(ImageView image, String name) {

        this.mImage = image;
        this.mName = name;
    }

    protected Bitmap doInBackground(String... urls) {
        String fullURL = urls[0];
        Bitmap icon = null;

        Log.d(TAG, mName + ": get image from " + fullURL);

        try {
            //icon = getBitmapFromMemCache(imageKey);

            if (icon==null) {
                InputStream in = new java.net.URL(fullURL).openStream();
                icon = BitmapFactory.decodeStream(in);
                //addBitmapToMemoryCache(String.valueOf(params[0]), icon);

            }
        } catch (Exception e) {
            Log.e(TAG, mName + ": exception in DownloadImageTask " + e.getMessage());
            e.printStackTrace();
            Sentry.captureException(e);
        }
        return icon;
    }

    protected void onPostExecute(Bitmap result) {
        mImage.setImageBitmap(result);
    }
}
