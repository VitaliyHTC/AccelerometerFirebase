package com.vitaliyhtc.accelerometerfirebase.adapters;

import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.models.SessionItem;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;
import com.vitaliyhtc.accelerometerfirebase.viewholder.SessionItemViewHolder;

import static com.vitaliyhtc.accelerometerfirebase.Config.DATE_TIME_FULL_FORMAT;
import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;

public class SessionItemsDataAdapter extends FirebaseRecyclerAdapter<SessionItem, SessionItemViewHolder> {

    private SessionItemViewHolder.ClickListener mClickListener;

    public SessionItemsDataAdapter(Class<SessionItem> modelClass, int modelLayout, Class<SessionItemViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(SessionItemViewHolder viewHolder, SessionItem model, int position) {

        if (model != null) {
            viewHolder.getStartTimeStampTextView().setText(Utils.getDate(model.getStartTime(), DATE_TIME_FULL_FORMAT));
            viewHolder.getStopTimeTextView().setText(Utils.getDate(model.getStopTime(), TIME_FULL_FORMAT));
            viewHolder.getDeviceModelTextView().setText(model.getDeviceInfo().getModel());
            String coordinatesCountString = model.getCoordinates().size() + " items.";
            viewHolder.getCoordinatesCountTextView().setText(coordinatesCountString);
            String intervalString = model.getInterval() + " seconds interval.";
            viewHolder.getIntervalTextView().setText(intervalString);
        }
    }

    @Override
    public SessionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SessionItemViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        viewHolder.setOnClickListener(mClickListener);
        return viewHolder;
    }

    public void setOnClickListener(SessionItemViewHolder.ClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }
}
