package com.vitaliyhtc.accelerometerfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.models.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;

public class AccelerometerDataListAdapter extends FirebaseRecyclerAdapter<AccelerometerData, AccelerometerDataListAdapter.AccelerometerDataViewHolder> {

    public AccelerometerDataListAdapter(Class<AccelerometerData> modelClass, int modelLayout, Class<AccelerometerDataViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(AccelerometerDataViewHolder viewHolder, AccelerometerData model, int position) {
        if (model != null) {
            viewHolder.bind(model);
        }
    }


    public static class AccelerometerDataViewHolder extends RecyclerView.ViewHolder {
        TextView timeStampTextView;
        TextView accelerometerXTextView;
        TextView accelerometerYTextView;
        TextView accelerometerZTextView;

        public AccelerometerDataViewHolder(View v) {
            super(v);
            timeStampTextView = (TextView) v.findViewById(R.id.tv_timeStamp);
            accelerometerXTextView = (TextView) v.findViewById(R.id.tv_accelerometerX);
            accelerometerYTextView = (TextView) v.findViewById(R.id.tv_accelerometerY);
            accelerometerZTextView = (TextView) v.findViewById(R.id.tv_accelerometerZ);
        }

        void bind(AccelerometerData model) {
            timeStampTextView.setText(Utils.getDate(model.getTimeStamp(), TIME_FULL_FORMAT));
            String x = "X: " + model.getX();
            accelerometerXTextView.setText(x);
            String y = "Y: " + model.getY();
            accelerometerYTextView.setText(y);
            String z = "Z: " + model.getZ();
            accelerometerZTextView.setText(z);
        }
    }
}
