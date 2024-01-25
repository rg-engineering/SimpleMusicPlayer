package eu.rg_engineering.simplemusicplayer.ui.home;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import eu.rg_engineering.simplemusicplayer.R;
import io.sentry.Sentry;


public class HomeFragment extends Fragment
   {
    private HomeViewModel homeViewModel;
    private final String TAG = "HomeFragment";
    HomeFragmentListener mCommunication;


    Context mContext;



    public interface HomeFragmentListener {
        void messageFromHomeFragment(String msg, String params);
    }
    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        mCommunication = (HomeFragmentListener) context;
        mContext = context;
        Log.d(TAG, "attached");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCommunication = null;
        Log.d(TAG, "detached");
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);

        try {

            /*
            Button btnPauseMusic = (Button) root.findViewById(R.id.PauseMusic);
            btnPauseMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "PauseMusic pressed");
                    mCommunication.messageFromHomeFragment("PauseMusic", "");
                }
            });

            Button btnPlayMusic = (Button) root.findViewById(R.id.PlayMusic);
            btnPlayMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "PlayMusic pressed");
                    mCommunication.messageFromHomeFragment("PlayMusic", "");
                }
            });

            Button btnStopMusic = (Button) root.findViewById(R.id.StopMusic);
            btnStopMusic.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Log.d(TAG, "StopMusic pressed");
                    mCommunication.messageFromHomeFragment("StopMusic", "");
                }
            });
*/


            mCommunication.messageFromHomeFragment("created", "");

        } catch (Exception ex) {
            Log.e(TAG, "exception in onCreateView " + ex);
            Sentry.captureException(ex);
        }

        Log.d(TAG, "view created");
        return root;
    }






    //todo wma kann nicht interpretiert werden
}