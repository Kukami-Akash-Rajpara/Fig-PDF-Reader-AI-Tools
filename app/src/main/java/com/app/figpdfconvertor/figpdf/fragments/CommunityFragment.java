package com.app.figpdfconvertor.figpdf.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.app.figpdfconvertor.figpdf.R;

public class CommunityFragment extends Fragment {

    public CommunityFragment() { }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        // Inflate fragment layout
        return inflater.inflate(R.layout.fragment_community, container, false);
    }
}