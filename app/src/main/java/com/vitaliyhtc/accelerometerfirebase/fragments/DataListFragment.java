package com.vitaliyhtc.accelerometerfirebase.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.activities.AccelerometerActivity;
import com.vitaliyhtc.accelerometerfirebase.adapters.AccelerometerDataListAdapter;
import com.vitaliyhtc.accelerometerfirebase.interfaces.SessionItemFragment;
import com.vitaliyhtc.accelerometerfirebase.models.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_COORDINATES;
import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_SESSIONS_ITEM;

public class DataListFragment extends Fragment implements SessionItemFragment {

    private String mSessionItemKey;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;

    @Override
    public void setSessionItemKey(String sessionItemKey) {
        if (!AccelerometerActivity.VALUE_DISPLAYED_SESSION_ITEM_KEY_OFF.equals(sessionItemKey) && sessionItemKey != null) {
            mSessionItemKey = sessionItemKey;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mSessionItemKey != null) {
            init();
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void init() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = database
                .child(FIREBASE_DB_PATH_SESSIONS_ITEM)
                .child(Utils.getCurrentUserUid())
                .child(mSessionItemKey)
                .child(FIREBASE_DB_PATH_COORDINATES);

        FirebaseRecyclerAdapter<AccelerometerData, AccelerometerDataListAdapter.AccelerometerDataViewHolder> firebaseAdapter =
                new AccelerometerDataListAdapter(
                        AccelerometerData.class,
                        R.layout.list_item_accelerometer_data,
                        AccelerometerDataListAdapter.AccelerometerDataViewHolder.class,
                        databaseReference);

        mProgressBar.setVisibility(ProgressBar.GONE);

        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(firebaseAdapter);
    }

}
