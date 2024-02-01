package eu.rg_engineering.simplemusicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.collection.LruCache;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import io.sentry.Sentry;

//todo cache on disk
//todo cache on memory too small?


public class ImageDownloader {
    private final String TAG = "ImageDownloader";
    private LruCache<String, Bitmap> memoryCache;
    
    public ImageDownloader() {

        Log.d(TAG, "create ImageDownloader ");

        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 8;

        memoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };


    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemCache(key) == null) {
            int size = bitmap.getByteCount() / 1024;
            Log.d(TAG, "put bitmap into cache " + key + " size " + size + "kB");
            memoryCache.put(key, bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        Log.d(TAG, "get bitmap from cache " + key);
        return memoryCache.get(key);
    }



    public void loadBitmap(String fullURL, ImageView imageView) {

        Log.d(TAG, "load bitmap, cache size: " + memoryCache.size() + "kB");
        final Bitmap bitmap = getBitmapFromMemCache(fullURL);
        if (bitmap != null) {
            Log.d(TAG, "bitmap from cache... ");
            imageView.setImageBitmap(bitmap);
        } else {
            Log.d(TAG, "not in cache, start download... ");



            BitmapWorkerTask task = new BitmapWorkerTask(imageView, fullURL);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    class BitmapWorkerTask extends AsyncTask<String, Void, Bitmap> {

        private ImageView mImageView;
        private String mFullURL;
        public BitmapWorkerTask(ImageView imageView, String url) {
            this.mImageView=imageView;
            this.mFullURL=url;
        }

        // Decode image in background.
        @Override
        protected Bitmap doInBackground(String... urls) {

            Bitmap icon = null;

            Log.w(TAG, "download image from " + mFullURL);

            try {

                InputStream in = new java.net.URL(mFullURL).openStream();
                icon = BitmapFactory.decodeStream(in);
                addBitmapToMemoryCache(mFullURL, icon);


            } catch (Exception e) {
                Log.e(TAG, " exception in DownloadImageTask " + e.getMessage());
                e.printStackTrace();
                Sentry.captureException(e);
            }

            return icon;
        }
        protected void onPostExecute(Bitmap result) {
            mImageView.setImageBitmap(result);
        }
    }


}
