package com.vitaliyhtc.accelerometerfirebase.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vitaliyhtc.accelerometerfirebase.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccelerometerDataViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_timeStamp)
    TextView timeStampTextView;

    @BindView(R.id.tv_accelerometerX)
    TextView accelerometerXTextView;

    @BindView(R.id.tv_accelerometerY)
    TextView accelerometerYTextView;

    @BindView(R.id.tv_accelerometerZ)
    TextView accelerometerZTextView;

    public AccelerometerDataViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);
    }

    public TextView getTimeStampTextView() {
        return timeStampTextView;
    }

    public void setTimeStampTextView(TextView timeStampTextView) {
        this.timeStampTextView = timeStampTextView;
    }

    public TextView getAccelerometerXTextView() {
        return accelerometerXTextView;
    }

    public void setAccelerometerXTextView(TextView accelerometerXTextView) {
        this.accelerometerXTextView = accelerometerXTextView;
    }

    public TextView getAccelerometerYTextView() {
        return accelerometerYTextView;
    }

    public void setAccelerometerYTextView(TextView accelerometerYTextView) {
        this.accelerometerYTextView = accelerometerYTextView;
    }

    public TextView getAccelerometerZTextView() {
        return accelerometerZTextView;
    }

    public void setAccelerometerZTextView(TextView accelerometerZTextView) {
        this.accelerometerZTextView = accelerometerZTextView;
    }
}