package com.vitaliyhtc.accelerometerfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.models.SessionItem;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import static com.vitaliyhtc.accelerometerfirebase.Config.DATE_TIME_FULL_FORMAT;
import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;

public class SessionItemsDataAdapter extends FirebaseRecyclerAdapter<SessionItem, SessionItemsDataAdapter.SessionItemViewHolder> {

    private ClickListener mClickListener;

    public SessionItemsDataAdapter(Class<SessionItem> modelClass, int modelLayout, Class<SessionItemViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(SessionItemViewHolder viewHolder, SessionItem model, int position) {
        if (model != null) {
            viewHolder.bind(model);
        }
    }

    @Override
    public SessionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        SessionItemViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        viewHolder.setOnClickListener(mClickListener);
        return viewHolder;
    }

    public void setOnClickListener(ClickListener mClickListener) {
        this.mClickListener = mClickListener;
    }


    public static class SessionItemViewHolder extends RecyclerView.ViewHolder {
        TextView startTimeStampTextView;
        TextView deviceModelTextView;
        TextView stopTimeTextView;
        TextView coordinatesCountTextView;
        TextView intervalTextView;

        private ClickListener mClickListener;

        public SessionItemViewHolder(View v) {
            super(v);
            startTimeStampTextView = (TextView) v.findViewById(R.id.tv_startTimeStamp);
            deviceModelTextView = (TextView) v.findViewById(R.id.tv_deviceModel);
            stopTimeTextView = (TextView) v.findViewById(R.id.tv_stopTime);
            coordinatesCountTextView = (TextView) v.findViewById(R.id.tv_coordinatesCount);
            intervalTextView = (TextView) v.findViewById(R.id.tv_interval);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(getAdapterPosition());
                }
            });
        }

        void setOnClickListener(ClickListener clickListener) {
            mClickListener = clickListener;
        }

        void bind(SessionItem model){
            startTimeStampTextView.setText(Utils.getDate(model.getStartTime(), DATE_TIME_FULL_FORMAT));
            stopTimeTextView.setText(Utils.getDate(model.getStopTime(), TIME_FULL_FORMAT));
            deviceModelTextView.setText(model.getDeviceInfo().getModel());
            String coordinatesCountString = model.getCoordinates().size() + " items.";
            coordinatesCountTextView.setText(coordinatesCountString);
            String intervalString = model.getInterval() + " seconds interval.";
            intervalTextView.setText(intervalString);
        }
    }

    public interface ClickListener {
        void onItemClick(int position);
    }
}
