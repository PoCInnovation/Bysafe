package org.dpppt.android.calibration.handwash;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.dpppt.android.calibration.R;

public class BarrierGestureInfoFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_barrier_gesture, container, false);
        return view;
    }

    public static BarrierGestureInfoFragment newInstance() {
        return new BarrierGestureInfoFragment();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button infoGovButton = view.findViewById(R.id.redirect_gov_info);
        infoGovButton.setOnClickListener(v -> {
            startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://www.gouvernement.fr/info-coronavirus")));
        });


        final ImageButton goToHandwashButton = view.findViewById(R.id.go_to_handwash);
        goToHandwashButton.setOnClickListener(v -> {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.main_fragment_container, HandwashFragment.newInstance())
                    .commit();
        });
    }
}
