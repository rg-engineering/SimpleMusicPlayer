package eu.rg_engineering.simplemusicplayer.TwonkyServer;


import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.util.Log;



import java.io.IOException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import io.sentry.Sentry;

/*
public class ScanNASFolder extends AsyncTask<String, String, String> {
    private final String TAG = "ScanNASFolder";
    private final String ServerName = "MYCLOUDEX2ULTRA";
    private final String UserName = "rene";
    private final String Password = "kenn11wort";
    private final String Path = "familie";
    private final String Path2 = "Musik\\Diane\\Das Beste";

    private void start() throws IOException {
        Log.d(TAG, "start scanning ");

//        SMBClient client = new SMBClient();
//
//        try (Connection connection = client.connect(ServerName)) {
//            AuthenticationContext ac = new AuthenticationContext(UserName, Password.toCharArray(), "DOMAIN");
//            Session session = connection.authenticate(ac);
//
//            // Connect to Share
//            try (DiskShare share = (DiskShare) session.connectShare(Path)) {
//                for (FileIdBothDirectoryInformation f : share.list(Path2, "*.mp3")) {
//                    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
//
//                    String source = Path2 + "\\" +f.getFileName();
//                    Set<SMB2ShareAccess> s = new HashSet<>();
//                    s.add(SMB2ShareAccess.ALL.iterator().next());
//                    share.openFile(source, EnumSet.of(AccessMask.GENERIC_READ), null, s, null, null);
//
//                    mmr.setDataSource(source);
//
//                    String albumName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM);
//                    Log.d(TAG,"File : " + f.getFileName() + " " + albumName);
//                }
//            }
//        }


    }

    @Override
    protected String doInBackground(String... strings) {

        try {
            start();
        } catch (IOException e) {
            Sentry.captureException(e);
            throw new RuntimeException(e);

        }

        return null;
    }
}
*/