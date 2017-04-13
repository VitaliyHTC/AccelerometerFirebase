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

    /**
     * @param modelClass      Firebase will marshall the data at a location into an instance of a class that you provide
     * @param modelLayout     This is the layout used to represent a single item in the list. You will be responsible for populating an
     *                        instance of the corresponding view with the data from an instance of modelClass.
     * @param viewHolderClass The class that hold references to all sub-views in an instance modelLayout.
     * @param ref             The Firebase location to watch for data changes. Can also be a slice of a location, using some
     */
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
