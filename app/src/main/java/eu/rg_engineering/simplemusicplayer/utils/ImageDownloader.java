package eu.rg_engineering.simplemusicplayer.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import androidx.collection.LruCache;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import eu.rg_engineering.simplemusicplayer.TrackItem;
import io.sentry.Sentry;

//todo house keeping of disk cache
//todo cache on memory too small?


public class ImageDownloader {
    private final String TAG = "ImageDownloader";
    private LruCache<String, Bitmap> memoryCache;
    private String mImageCachDir;

    public ImageDownloader(String curDir) {

        Log.d(TAG, "create ImageDownloader ");

        mImageCachDir = curDir + "/thumbs";


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

                int bytes = 0;
                if (bitmap!=null){
                    bytes= bitmap.getByteCount() / 1024;
                }

                return bytes;
            }
        };
    }

    private void addBitmapToMemoryCache(String key, Bitmap bitmap) {
        if (getBitmapFromMemoryCache(key) == null && bitmap != null) {
            int size = bitmap.getByteCount() / 1024;
            Log.d(TAG, "put bitmap into cache " + key + " size " + size + "kB");
            memoryCache.put(key, bitmap);
        }
    }

    private Bitmap getBitmapFromMemoryCache(String key) {
        Log.d(TAG, "get bitmap from cache " + key);
        return memoryCache.get(key);
    }

    private void addBitmapToDiskCache(String key, Bitmap bitmap) {
        //http://192.168.3.21:32400/library/metadata/49889/thumb/1706671847?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ
        String[] parts = key.split("[/?]");

        String filename = parts[7] + ".bmp";

        writeToFile(bitmap,filename);

    }

    private Bitmap getBitmapFromDiskCache(String key) {
        //http://192.168.3.21:32400/library/metadata/49889/thumb/1706671847?X-Plex-Token=LAtVbxshNWzuGUwtm8bJ
        String[] parts = key.split("[/?]");

        String filename = parts[7] + ".bmp";

        Bitmap bitmap=readFromFile(filename);

        return bitmap;
    }
    public void loadBitmap(String fullURL, ImageView imageView) {

        Log.d(TAG, "load bitmap, cache size: " + memoryCache.size() + "kB");
        Bitmap bitmap = getBitmapFromMemoryCache(fullURL);
        if (bitmap != null) {
            Log.d(TAG, "bitmap from cache... ");
            imageView.setImageBitmap(bitmap);
        } else {

            bitmap = getBitmapFromDiskCache(fullURL);
            if (bitmap != null) {
                Log.d(TAG, "bitmap from disk cache... ");
                imageView.setImageBitmap(bitmap);
            } else {
                Log.d(TAG, "not in cache, start download... ");

                BitmapWorkerTask task = new BitmapWorkerTask(imageView, fullURL);
                task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
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
                addBitmapToDiskCache(mFullURL, icon);


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

    private void writeToFile(Bitmap bitmap,String filename) {
        try {

            File file = new File(mImageCachDir, filename);
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // Will create parent directories if not exists
                file.createNewFile();
            }
            FileOutputStream writer = new FileOutputStream(file,true);

            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, writer);

            writer.close();
            Log.d("TAG", "Wrote to file: " + filename);

        } catch (IOException ex) {
            Log.e(TAG, "File write failed: " + ex.toString());
            Sentry.captureException(ex);
        }


    }
    private Bitmap readFromFile(String filename) {

        File newDir = new File(mImageCachDir,  filename);

        Bitmap icon=null;
        try {
            icon = BitmapFactory.decodeFile(newDir.toString());
        }
        catch (Exception ex){
            Log.e(TAG, "cold not load bitmap " + filename);
            Sentry.captureException(ex);
        }
        return icon;
    }

}
