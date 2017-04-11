package com.vitaliyhtc.accelerometerfirebase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vitaliyhtc.accelerometerfirebase.interfaces.ActivityToDataFragment;
import com.vitaliyhtc.accelerometerfirebase.interfaces.SessionItemFragment;
import com.vitaliyhtc.accelerometerfirebase.model.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_COORDINATES;
import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;
import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_SESSIONS_ITEM;

public class DataListFragment extends Fragment implements SessionItemFragment {

    private String mSessionItemKey;

    private ActivityToDataFragment mActivityToFragment;

    private DatabaseReference mDatabase;
    private ProgressBar mProgressBar;



    @Override
    public void setSessionItemKey(String sessionItemKey) {
        if(!MainActivity.VALUE_DISPLAYED_SESSION_ITEM_KEY_OFF.equals(sessionItemKey) && sessionItemKey != null){
            mSessionItemKey = sessionItemKey;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        if (mSessionItemKey != null) {
            init();
        } else {
            mProgressBar.setVisibility(ProgressBar.GONE);
        }
    }

    private void init(){
        if(mActivityToFragment == null){
            mActivityToFragment = (ActivityToDataFragment) getActivity();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        RecyclerView recyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        DatabaseReference databaseReference = mDatabase
                .child(FIREBASE_DB_PATH_SESSIONS_ITEM)
                .child(mActivityToFragment.getFirebaseUser().getUid())
                .child(mSessionItemKey)
                .child(FIREBASE_DB_PATH_COORDINATES);

        FirebaseRecyclerAdapter<AccelerometerData, AccelerometerDataViewHolder> firebaseAdapter = new FirebaseRecyclerAdapter<AccelerometerData, AccelerometerDataViewHolder>(
                AccelerometerData.class,
                R.layout.list_item_accelerometer_data,
                AccelerometerDataViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(AccelerometerDataViewHolder viewHolder, AccelerometerData model, int position) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                if (model != null) {
                    viewHolder.timeStampTextView.setText(Utils.getDate(model.getTimeStamp(), TIME_FULL_FORMAT));
                    String x = "X: " + model.getX();
                    viewHolder.accelerometerXTextView.setText(x);
                    String y = "Y: " + model.getY();
                    viewHolder.accelerometerYTextView.setText(y);
                    String z = "Z: " + model.getZ();
                    viewHolder.accelerometerZTextView.setText(z);
                }
            }
        };

        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(firebaseAdapter);
    }



    private static class AccelerometerDataViewHolder extends RecyclerView.ViewHolder {
        TextView timeStampTextView;
        TextView accelerometerXTextView;
        TextView accelerometerYTextView;
        TextView accelerometerZTextView;

        public AccelerometerDataViewHolder(View v){
            super(v);
            timeStampTextView = (TextView) v.findViewById(R.id.tv_timeStamp);
            accelerometerXTextView = (TextView) v.findViewById(R.id.tv_accelerometerX);
            accelerometerYTextView = (TextView) v.findViewById(R.id.tv_accelerometerY);
            accelerometerZTextView = (TextView) v.findViewById(R.id.tv_accelerometerZ);
        }
    }
}
