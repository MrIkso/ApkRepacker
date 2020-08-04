package com.mrikso.apkrepacker.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.mrikso.apkrepacker.R;

import com.mrikso.apkrepacker.utils.FragmentUtils;

import org.jetbrains.annotations.NotNull;

public class OtherFragment extends Fragment implements View.OnClickListener{

    public static final String TAG = "OtherFragment";

    public OtherFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_other, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        LinearLayout checkUpdates = view.findViewById(R.id.action_check_for_updates);
        LinearLayout settings = view.findViewById(R.id.action_settings);
        LinearLayout about = view.findViewById(R.id.action_about);

        checkUpdates.setOnClickListener(this);
        settings.setOnClickListener(this);
        about.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.action_check_for_updates:
                Toast.makeText(requireContext(), "On developing!", Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_settings:

                FragmentUtils.add(new SettingsFragment(),getParentFragmentManager(),android.R.id.content);
                break;
            case R.id.action_about:
                FragmentUtils.add(new AboutFragment(),getParentFragmentManager(),android.R.id.content);
                break;
        }
    }
}
