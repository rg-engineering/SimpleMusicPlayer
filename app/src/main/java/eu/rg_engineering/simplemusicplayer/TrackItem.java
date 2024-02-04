package eu.rg_engineering.simplemusicplayer;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TrackItem implements Parcelable  {

    private final String TAG = "TrackItem";
    private final String mID;
    private String mName;
    private String mAlbum;
    private String mPath2AlbumImage;
    private String mArtist;
    private String mPath2ArtistImage;
    private String mFileName;
    private int mDuration;
    private int mProgress;
    private long mCurrentPlaytime;
    private int mAlbumIndex;

    protected TrackItem(Parcel in) {
        mID = in.readString();
        mName = in.readString();
    }
    public TrackItem(String name, String album, String artist, String filename, int duration, String path2AlbumImage, String path2ArtistImage, int albumIndex ) {
        mID = UUID.randomUUID().toString();
        mName = name;
        mAlbum = album;
        mArtist= artist;
        mFileName=filename;
        mDuration=duration;
        mProgress = 0;
        mCurrentPlaytime = 0;
        mPath2AlbumImage= path2AlbumImage;
        mPath2ArtistImage = path2ArtistImage;
        mAlbumIndex=albumIndex;

    }

    public TrackItem(String data ) {
        mID = UUID.randomUUID().toString();

        Deserialize(data);

        mProgress = 0;
        mCurrentPlaytime = 0;
    }


    public String getId() {
        return mID;
    }
    public String getName() {
        return mName;
    }
    public String getAlbum() {
        return mAlbum;
    }
    public String getArtist() {
        return mArtist;
    }
    public String getFileName() {
        return mFileName;
    }
    public String getDuration() {
        String sDuration = Convert2Timestring(mDuration);
        return sDuration;
    }
    public int getProgress() {
        return mProgress;
    }
    public String getPath2AlbumImage(){
        return mPath2AlbumImage;
    }
    public String getPath2ArtistImage(){
        return mPath2ArtistImage;
    }

    public int getAlbumIndex(){
        return mAlbumIndex;
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

    public static final Creator<TrackItem> CREATOR = new Creator<TrackItem>() {
        @Override
        public TrackItem createFromParcel(Parcel in) {
            return new TrackItem(in);
        }
        @Override
        public TrackItem[] newArray(int size) {
            return new TrackItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeString(mID);
        parcel.writeString(mName);
        parcel.writeString(mAlbum);
        parcel.writeString(mArtist);
        parcel.writeString(mFileName);
        parcel.writeInt(mDuration);
        parcel.writeString(mPath2AlbumImage);
        parcel.writeString(mPath2ArtistImage);
        parcel.writeInt(mAlbumIndex);
    }

    public static ArrayList<TrackItem> createItemsList(int numItems) {
        ArrayList<TrackItem> items = new ArrayList<>();

        for (int i = 1; i <= numItems; i++) {
            items.add(new TrackItem("track"+i,"","","",0,"","",-1));
        }
        return items;
    }

    public String Serialize2() {
        String sRet = "";

        sRet += mID;
        sRet += System.getProperty("line.separator");
        sRet += mName;
        sRet += System.getProperty("line.separator");
        sRet += mAlbum;
        sRet += System.getProperty("line.separator");
        sRet += mArtist;
        sRet += System.getProperty("line.separator");
        sRet += mFileName;
        sRet += System.getProperty("line.separator");
        sRet += mDuration;
        sRet += System.getProperty("line.separator");
        sRet += mPath2AlbumImage;
        sRet += System.getProperty("line.separator");
        sRet += mPath2ArtistImage;
        sRet += System.getProperty("line.separator");
        sRet += mAlbumIndex;
        sRet += System.getProperty("line.separator");

        return sRet;
    }

    public String Serialize(Boolean all) {
        String sRet = "";

        sRet += "'" + mID;
        sRet += "','" + mName;
        sRet += "','" + mAlbum;
        sRet += "','" + mArtist;
        sRet += "','" + mFileName;
        sRet += "','" + mDuration;
        sRet += "','" + mPath2AlbumImage;
        sRet += "','" + mPath2ArtistImage;
        sRet += "','" + mAlbumIndex;
        sRet += "'" + System.getProperty("line.separator");

        return sRet;
    }

    private void Deserialize(String data) {

        try {
            String[] parts = data.split("','");

            if (parts.length>6) {
                mName = parts[1];
                mAlbum = parts[2];
                mArtist = parts[3];
                mFileName = parts[4];
                try {
                    String sDuration = parts[5].replace("'","");
                    mDuration = Integer.parseInt(sDuration);
                } catch (NumberFormatException e) {
                    mDuration = 0;
                }
                mPath2AlbumImage = parts[6];
                mPath2ArtistImage = parts[7];
                //remove last "'"
                //mPath2ArtistImage = parts[7].substring(0, parts[7].length() - 1);
                //must be removed when adding new entries...
                try {
                    String sAlbumIndex = parts[8].replace("'","");
                    mAlbumIndex = Integer.parseInt(sAlbumIndex);
                } catch (NumberFormatException e) {
                    mAlbumIndex = -1;
                }



            }
        }
        catch (Exception ex){
            Log.e(TAG, "exception in Deserialize " + ex );
        }
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
}
