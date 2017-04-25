package com.vitaliyhtc.accelerometerfirebase.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vitaliyhtc.accelerometerfirebase.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SessionItemViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_startTimeStamp)
    TextView startTimeStampTextView;

    @BindView(R.id.tv_deviceModel)
    TextView deviceModelTextView;

    @BindView(R.id.tv_stopTime)
    TextView stopTimeTextView;

    @BindView(R.id.tv_coordinatesCount)
    TextView coordinatesCountTextView;

    @BindView(R.id.tv_interval)
    TextView intervalTextView;

    private ClickListener mClickListener;

    public SessionItemViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
        // TODO: 25/04/17 ButterKnife? check video https://www.youtube.com/watch?v=imsr8NrIAMs&t=2062
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mClickListener.onItemClick(getAdapterPosition());
            }
        });
    }

    public void setOnClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }


    public TextView getStartTimeStampTextView() {
        return startTimeStampTextView;
    }

    public void setStartTimeStampTextView(TextView startTimeStampTextView) {
        this.startTimeStampTextView = startTimeStampTextView;
    }

    public TextView getDeviceModelTextView() {
        return deviceModelTextView;
    }

    public void setDeviceModelTextView(TextView deviceModelTextView) {
        this.deviceModelTextView = deviceModelTextView;
    }

    public TextView getStopTimeTextView() {
        return stopTimeTextView;
    }

    public void setStopTimeTextView(TextView stopTimeTextView) {
        this.stopTimeTextView = stopTimeTextView;
    }

    public TextView getCoordinatesCountTextView() {
        return coordinatesCountTextView;
    }

    public void setCoordinatesCountTextView(TextView coordinatesCountTextView) {
        this.coordinatesCountTextView = coordinatesCountTextView;
    }

    public TextView getIntervalTextView() {
        return intervalTextView;
    }

    public void setIntervalTextView(TextView intervalTextView) {
        this.intervalTextView = intervalTextView;
    }

    public interface ClickListener {
        void onItemClick(int position);
    }
}
