package com.vitaliyhtc.accelerometerfirebase;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.vitaliyhtc.accelerometerfirebase.interfaces.ActivityToDataFragment;
import com.vitaliyhtc.accelerometerfirebase.interfaces.SessionItemFragment;
import com.vitaliyhtc.accelerometerfirebase.model.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.model.SessionItem;

import java.util.ArrayList;
import java.util.List;

import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_SESSIONS_ITEM;

public class DataGraphFragment extends Fragment implements SessionItemFragment {

    private String mSessionItemKey;

    private ActivityToDataFragment mActivityToFragment;

    private DatabaseReference mDatabase;

    private long mReferenceTimestamp = 0;

    private SessionItem mSessionItem;

    private List<Entry> entriesX;
    private List<Entry> entriesY;
    private List<Entry> entriesZ;
    private LineDataSet dsx;
    private LineDataSet dsy;
    private LineDataSet dsz;



    @Override
    public void setSessionItemKey(String sessionItemKey) {
        mSessionItemKey = sessionItemKey;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_graph, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (mSessionItemKey != null) {
            init();
        }
    }

    private void init(){
        if(mActivityToFragment == null){
            mActivityToFragment = (ActivityToDataFragment) getActivity();
        }

        mDatabase = FirebaseDatabase.getInstance().getReference();

        DatabaseReference databaseReference = mDatabase
                .child(FIREBASE_DB_PATH_SESSIONS_ITEM)
                .child(mActivityToFragment.getFirebaseUser().getUid())
                .child(mSessionItemKey);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mSessionItem = dataSnapshot.getValue(SessionItem.class);
                initGraph();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("DataGraphFragment", "loadSessionItem:onCanceled()", databaseError.toException());
            }
        });
    }

    private void initGraph(){
        LineChart chart = (LineChart) getActivity().findViewById(R.id.lineChart1);

        chart.getDescription().setEnabled(false);

        chart.setData(getData());

        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.invalidate();
    }

    private LineData getData(){
        ArrayList<ILineDataSet> sets = new ArrayList<>();

        entriesX = new ArrayList<>();
        entriesY = new ArrayList<>();
        entriesZ = new ArrayList<>();

        for (AccelerometerData data : mSessionItem.getCoordinates()) {
            if(mReferenceTimestamp == 0){
                mReferenceTimestamp = data.getTimeStamp();
            }
            entriesX.add(new Entry((data.getTimeStamp() - mReferenceTimestamp)/10, data.getX()));
            entriesY.add(new Entry((data.getTimeStamp() - mReferenceTimestamp)/10, data.getY()));
            entriesZ.add(new Entry((data.getTimeStamp() - mReferenceTimestamp)/10, data.getZ()));
        }

        dsx = new LineDataSet(entriesX, "X");
        dsy = new LineDataSet(entriesY, "Y");
        dsz = new LineDataSet(entriesZ, "Z");

        dsx.setLineWidth(2f);
        dsy.setLineWidth(2f);
        dsz.setLineWidth(2f);

        dsx.setDrawCircles(false);
        dsy.setDrawCircles(false);
        dsz.setDrawCircles(false);

        dsx.setColor(Color.RED);
        dsy.setColor(Color.GREEN);
        dsz.setColor(Color.BLUE);

        sets.add(dsx);
        sets.add(dsy);
        sets.add(dsz);

        return new LineData(sets);
    }
}