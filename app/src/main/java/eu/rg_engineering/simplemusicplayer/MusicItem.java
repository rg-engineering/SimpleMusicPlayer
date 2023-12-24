package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.sentry.Sentry;


public class MusicItem implements Parcelable {

    private String mID;
    private String mTitle;
    private String mAlbum;
    private String mArtist;
    private String mFilename;
    private int mDuration;
    private long mCurrentPlaytime;
    private int mProgress;
    private final String TAG = "MusicItem";

    public MusicItem(String title, String album, String artist, String filename, int duration) {
        mID = UUID.randomUUID().toString();
        mTitle = title;
        mAlbum = album;
        mArtist = artist;
        mFilename = filename;
        mDuration = duration;
        mCurrentPlaytime = 0;
        mProgress = 0;
    }

    public MusicItem(String serialised) {
        //Item 1,12
        mID = "";
        String[] separated = serialised.split(",");
        try {
            mID = separated[0];
            mTitle = separated[1];
            mAlbum = separated[2];
            mArtist = separated[3];
            mFilename = separated[4];
            try {
                mDuration = Integer.parseInt(separated[5]);
            } catch (NumberFormatException ex) {
                Sentry.captureException(ex);
            }
            if (mID == null || mID.isEmpty() || mID.length() < 6) {
                mID = UUID.randomUUID().toString();
            }

            mCurrentPlaytime = 0;
            mProgress = 0;
        } catch (Exception ex) {
            Log.v(TAG, "Exception in MusicItem deserialize " + ex.getLocalizedMessage());
            Sentry.captureException(ex);
        }
    }

    protected MusicItem(Parcel in) {
        try {
            mID = in.readString();
            mTitle = in.readString();
            mAlbum = in.readString();
            mArtist = in.readString();
            mFilename = in.readString();
            mDuration = in.readInt();
            mCurrentPlaytime = 0;
            mProgress = 0;
        } catch (Exception ex) {
            Log.v(TAG, "Exception in MusicItem deparcel " + ex.getLocalizedMessage());
            Sentry.captureException(ex);
        }
    }

    public static final Creator<MusicItem> CREATOR = new Creator<MusicItem>() {
        @Override
        public MusicItem createFromParcel(Parcel in) {
            return new MusicItem(in);
        }

        @Override
        public MusicItem[] newArray(int size) {
            return new MusicItem[size];
        }
    };

    public String getId() {
        return mID;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getArtist() {
        return mArtist;
    }

    public String getAlbum() {
        return mAlbum;
    }

    public String getFilename() {
        return mFilename;
    }

    public String getDuration() {
        String sDuration = Convert2Timestring(mDuration);
        return sDuration;
    }

    public void setCurrentPlaytime(long playtime) {

        //Log.d(TAG, "current play time " + playtime + " of " + mDuration);

        mCurrentPlaytime = playtime;

        long ntemp = mCurrentPlaytime * 100 / (long) mDuration;

        mProgress = (int) ntemp;
    }

    public String getCurrentPlaytime() {
        String sCurrentPlaytime = Convert2Timestring(mCurrentPlaytime);
        return sCurrentPlaytime;
    }

    public int getProgress() {
        return mProgress;
    }

    String Convert2Timestring(long duration) {
        String sRet = "";
        if (duration > 0) {
            {
                sRet = String.format("%02d:%02d:%02d",
                        TimeUnit.MILLISECONDS.toHours(duration),
                        TimeUnit.MILLISECONDS.toMinutes(duration) -
                                TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(duration)),
                        TimeUnit.MILLISECONDS.toSeconds(duration) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration)));
            }
        }
        return sRet;
    }

    private static final int lastItemId = 0;

    public static ArrayList<MusicItem> createItemsList(int numItems) {
        ArrayList<MusicItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new MusicItem("test", "test", "test", "test", 0));
        }
        return items;
    }

    public String Serialize2() {
        String sRet = "";

        sRet += mID;
        sRet += System.getProperty("line.separator");
        sRet += mTitle;
        sRet += System.getProperty("line.separator");
        sRet += mAlbum;
        sRet += System.getProperty("line.separator");
        sRet += mArtist;
        sRet += System.getProperty("line.separator");
        sRet += mFilename;
        sRet += System.getProperty("line.separator");
        sRet += String.valueOf(mDuration);
        sRet += System.getProperty("line.separator");
        return sRet;
    }

    public String Serialize(Boolean all) {
        String sRet = "";

        sRet += "," + mID;
        sRet += "," + mTitle;
        sRet += "," + mAlbum;
        sRet += "," + mArtist;
        sRet += "," + mFilename;
        sRet += "," + mDuration;
        sRet += System.getProperty("line.separator");
        return sRet;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mTitle);
        dest.writeString(mAlbum);
        dest.writeString(mArtist);
        dest.writeString(mFilename);
        dest.writeInt(mDuration);
    }
}