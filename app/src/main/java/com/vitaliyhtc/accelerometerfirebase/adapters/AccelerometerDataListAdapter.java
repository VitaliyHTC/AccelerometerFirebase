package com.vitaliyhtc.accelerometerfirebase.adapters;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.models.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;
import com.vitaliyhtc.accelerometerfirebase.viewholder.AccelerometerDataViewHolder;

import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;

public class AccelerometerDataListAdapter extends FirebaseRecyclerAdapter<AccelerometerData, AccelerometerDataViewHolder> {

    public AccelerometerDataListAdapter(Class<AccelerometerData> modelClass, int modelLayout, Class<AccelerometerDataViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(AccelerometerDataViewHolder viewHolder, AccelerometerData model, int position) {
        if (model != null) {
            viewHolder.getTimeStampTextView().setText(Utils.getDate(model.getTimeStamp(), TIME_FULL_FORMAT));
            String x = "X: " + model.getX();
            viewHolder.getAccelerometerXTextView().setText(x);
            String y = "Y: " + model.getY();
            viewHolder.getAccelerometerYTextView().setText(y);
            String z = "Z: " + model.getZ();
            viewHolder.getAccelerometerZTextView().setText(z);
        }
    }
}
