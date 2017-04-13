package com.vitaliyhtc.accelerometerfirebase.fragments;

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
import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.interfaces.SessionItemFragment;
import com.vitaliyhtc.accelerometerfirebase.models.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.models.SessionItem;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_SESSIONS_ITEM;

public class DataGraphFragment extends Fragment implements SessionItemFragment {

    private String mSessionItemKey;

    private long mReferenceTimestamp = 0;

    private SessionItem mSessionItem;


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

    private void init() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        DatabaseReference databaseReference = database
                .child(FIREBASE_DB_PATH_SESSIONS_ITEM)
                .child(Utils.getCurrentUserUid())
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

    private void initGraph() {
        LineChart chart = (LineChart) getActivity().findViewById(R.id.lineChart1);

        chart.getDescription().setEnabled(false);

        chart.setData(getLineDataFromSessionItem(mSessionItem));

        chart.getAxisRight().setEnabled(false);
        chart.getXAxis().setEnabled(false);
        chart.invalidate();
    }


    private LineData getLineDataFromSessionItem(SessionItem sessionItem) {
        ArrayList<ILineDataSet> sets = new ArrayList<>();

        List<Entry> entriesX = new ArrayList<>();
        List<Entry> entriesY = new ArrayList<>();
        List<Entry> entriesZ = new ArrayList<>();

        for (AccelerometerData data : sessionItem.getCoordinates()) {
            if (mReferenceTimestamp == 0) {
                mReferenceTimestamp = data.getTimeStamp();
            }
            entriesX.add(new Entry((data.getTimeStamp() - mReferenceTimestamp) / 10, data.getX()));
            entriesY.add(new Entry((data.getTimeStamp() - mReferenceTimestamp) / 10, data.getY()));
            entriesZ.add(new Entry((data.getTimeStamp() - mReferenceTimestamp) / 10, data.getZ()));
        }

        LineDataSet dsx = new LineDataSet(entriesX, "X");
        LineDataSet dsy = new LineDataSet(entriesY, "Y");
        LineDataSet dsz = new LineDataSet(entriesZ, "Z");

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
