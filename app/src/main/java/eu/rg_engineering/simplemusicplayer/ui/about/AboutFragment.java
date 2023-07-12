package eu.rg_engineering.simplemusicplayer.ui.about;


import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import eu.rg_engineering.simplemusicplayer.R;

public class AboutFragment extends Fragment {

    private AboutViewModel AboutViewModel;
    private Button btnShowDataPrivacy= null;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AboutViewModel =
                ViewModelProviders.of(this).get(AboutViewModel.class);
        View root = inflater.inflate(R.layout.fragment_about, container, false);


        btnShowDataPrivacy = root.findViewById(R.id.btnDataPrivacy);
        setShowDataPrivacyButtonListener();

        return root;
    }

    private void setShowDataPrivacyButtonListener() {
        btnShowDataPrivacy.setOnClickListener(e -> {
            Uri uri = Uri.parse("https://www.rg-engineering.de/index.php/datenschutz/weitere-datenschutzerklaerungen/datenschutzerklaerung-simple_music_player");
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            startActivity(intent);
        });
    }
}
