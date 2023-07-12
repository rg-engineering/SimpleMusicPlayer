package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;


public class MusicItem implements Parcelable {

    private String mTitle;
    private String mAlbum;
    private String mArtist;
    private String mFilename;
    private int mDuration;
    private String TAG = "MusicItem";

    public MusicItem(String title,String  album,String artist,String filename,int duration) {
          mTitle=title;
          mAlbum=album;
          mArtist=artist;
          mFilename=filename;
          mDuration=duration;
    }

    public MusicItem(String serialised) {
        //Item 1,12
        String[] separated = serialised.split(",");
        try {
            mTitle = separated[0];
            mAlbum = separated[1];
            mArtist = separated[2];
            mFilename = separated[3];
            mDuration = Integer.parseInt(separated[4]);
        } catch (Exception ex) {
            Log.v(TAG, "Exception in MusicItem deserialize " + ex.getLocalizedMessage());
        }
    }

    protected MusicItem(Parcel in) {
        try {
            mTitle = in.readString();
            mAlbum = in.readString();
            mArtist = in.readString();
            mFilename = in.readString();
            mDuration = in.readInt();
        } catch (Exception ex) {
            Log.v(TAG, "Exception in MusicItem deparcel " + ex.getLocalizedMessage());
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

    public String getTitle() {return mTitle;}
    public String getArtist() {return mArtist;}
    public String getAlbum() {return mAlbum;}
    public String getFilename() {return mFilename;}
    public String getDuration()
    {
        String sDuration = String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(mDuration),
                TimeUnit.MILLISECONDS.toMinutes(mDuration) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(mDuration)),
                TimeUnit.MILLISECONDS.toSeconds(mDuration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mDuration)));
        return sDuration;
    }

    private static int lastItemId = 0;
    public static ArrayList<MusicItem> createItemsList(int numItems) {
        ArrayList<MusicItem> items = new ArrayList<MusicItem>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new MusicItem("test","test","test","test",0));
        }
        return items;
    }

    public String Serialize2() {
        String sRet="";

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
        return sRet ;
    }

    public String Serialize(Boolean all) {
        String sRet="";

        sRet += "," + mTitle;
        sRet += "," + mAlbum;
        sRet += "," + mArtist;
        sRet += "," + mFilename;
        sRet += "," + String.valueOf(mDuration);
        sRet += System.getProperty("line.separator");
        return sRet ;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mAlbum);
        dest.writeString(mArtist);
        dest.writeString(mFilename);
        dest.writeInt(mDuration);


    }
}