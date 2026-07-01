package com.patitasalrescate.controllers.feed;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.patitasalrescate.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentInicioAdoptante#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentInicioAdoptante extends Fragment {
    public FragmentInicioAdoptante() {
        // Required empty public constructor
    }
    public static FragmentInicioAdoptante newInstance(String param1, String param2) {
        FragmentInicioAdoptante fragment = new FragmentInicioAdoptante();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fg_inicio_adoptante, container, false);
    }
}