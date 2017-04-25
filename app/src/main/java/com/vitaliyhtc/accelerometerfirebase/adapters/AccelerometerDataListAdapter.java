package com.vitaliyhtc.accelerometerfirebase.adapters;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.models.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.viewholder.AccelerometerDataViewHolder;

public class AccelerometerDataListAdapter extends FirebaseRecyclerAdapter<AccelerometerData, AccelerometerDataViewHolder> {

    public AccelerometerDataListAdapter(Class<AccelerometerData> modelClass, int modelLayout, Class<AccelerometerDataViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(AccelerometerDataViewHolder viewHolder, AccelerometerData model, int position) {
        if (model != null) {
            viewHolder.bind(model);
        }
    }
}
