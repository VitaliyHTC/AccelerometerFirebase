package com.vitaliyhtc.accelerometerfirebase;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vitaliyhtc.accelerometerfirebase.interfaces.ActivityToDataFragment;
import com.vitaliyhtc.accelerometerfirebase.model.SessionItem;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_SESSIONS_ITEM;
import static com.vitaliyhtc.accelerometerfirebase.Config.DATE_TIME_FULL_FORMAT;
import static com.vitaliyhtc.accelerometerfirebase.Config.TIME_FULL_FORMAT;

public class DataHistoryFragment extends Fragment {

    private DatabaseReference mDatabase;
    private FirebaseRecyclerAdapter<SessionItem, DataHistoryFragment.SessionItemViewHolder> mFirebaseAdapter;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private ProgressBar mProgressBar;



    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_data_list, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        init();
    }

    private void init(){
        mDatabase = FirebaseDatabase.getInstance().getReference();

        mProgressBar = (ProgressBar) getActivity().findViewById(R.id.progressBar);
        mRecyclerView = (RecyclerView) getActivity().findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);



        DatabaseReference databaseReference = mDatabase.child(FIREBASE_DB_PATH_SESSIONS_ITEM)
                .child(((ActivityToDataFragment)getActivity()).getFirebaseUser().getUid());



        mFirebaseAdapter = new FirebaseRecyclerAdapter<SessionItem, DataHistoryFragment.SessionItemViewHolder>(
                SessionItem.class,
                R.layout.list_item_history,
                DataHistoryFragment.SessionItemViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(DataHistoryFragment.SessionItemViewHolder viewHolder, SessionItem model, int position) {
                mProgressBar.setVisibility(ProgressBar.GONE);
                if (model !=null) {
                    viewHolder.startTimeStampTextView.setText(Utils.getDate(model.getStartTime(), DATE_TIME_FULL_FORMAT));
                    viewHolder.stopTimeTextView.setText(Utils.getDate(model.getStopTime(), TIME_FULL_FORMAT));
                    viewHolder.deviceModelTextView.setText(model.getDeviceInfo().getModel());
                    String coordinatesCountString = model.getCoordinates().size() + " items.";
                    viewHolder.coordinatesCountTextView.setText(coordinatesCountString);
                    String intervalString = model.getInterval() + " seconds interval.";
                    viewHolder.intervalTextView.setText(intervalString);
                }
            }
            @Override
            public SessionItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                SessionItemViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
                viewHolder.setOnClickListener(new SessionItemViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        ((ActivityToDataFragment) getActivity()).displayHistoryItemByKey(getRef(position).getKey());
                    }
                });
                return viewHolder;
            }
        };

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int sessionItemsCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition =
                        mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                // If the recycler view is initially being loaded or the
                // user is at the bottom of the list, scroll to the bottom
                // of the list to show the newly added session items.
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (sessionItemsCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);

    }



    static class SessionItemViewHolder extends RecyclerView.ViewHolder {
        TextView startTimeStampTextView;
        TextView deviceModelTextView;
        TextView stopTimeTextView;
        TextView coordinatesCountTextView;
        TextView intervalTextView;

        private ClickListener mClickListener;

        public SessionItemViewHolder(View v){
            super(v);
            startTimeStampTextView = (TextView) v.findViewById(R.id.tv_startTimeStamp);
            deviceModelTextView = (TextView) v.findViewById(R.id.tv_deviceModel);
            stopTimeTextView = (TextView) v.findViewById(R.id.tv_stopTime);
            coordinatesCountTextView = (TextView) v.findViewById(R.id.tv_coordinatesCount);
            intervalTextView = (TextView) v.findViewById(R.id.tv_interval);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickListener.onItemClick(v, getAdapterPosition());
                }
            });
        }

        interface ClickListener{
            void onItemClick(View view, int position);
        }

        public void setOnClickListener(SessionItemViewHolder.ClickListener clickListener){
            mClickListener = clickListener;
        }
    }
}
