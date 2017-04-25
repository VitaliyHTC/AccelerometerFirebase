package com.vitaliyhtc.accelerometerfirebase.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.models.AccelerometerData;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;

// TODO: 25/04/17 if ViewHolder is used only in single Adapter put it inside of adapter class
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

    // TODO: 25/04/17 really??? getters and setters for view??? remove this crap
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

    // TODO: 25/04/17 use simple bind method to connect ViewHolder instance with single data item
    public void bind(AccelerometerData model) {
        timeStampTextView.setText(Utils.getDate(model.getTimeStamp(), TIME_FULL_FORMAT));
        String x = "X: " + model.getX();
        accelerometerXTextView.setText(x);
        String y = "Y: " + model.getY();
        accelerometerYTextView.setText(y);
        String z = "Z: " + model.getZ();
        accelerometerZTextView.setText(z);
    }
}
