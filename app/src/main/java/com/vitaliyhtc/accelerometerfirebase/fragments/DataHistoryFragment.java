package com.vitaliyhtc.accelerometerfirebase.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.adapters.SessionItemsDataAdapter;
import com.vitaliyhtc.accelerometerfirebase.interfaces.HistoryItemSelectionCallback;
import com.vitaliyhtc.accelerometerfirebase.models.SessionItem;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;
import com.vitaliyhtc.accelerometerfirebase.viewholder.SessionItemViewHolder;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.vitaliyhtc.accelerometerfirebase.Config.FIREBASE_DB_PATH_SESSIONS_ITEM;

public class DataHistoryFragment extends Fragment {

    private FirebaseRecyclerAdapter<SessionItem, SessionItemViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_data_list, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    private void init() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();

        mLinearLayoutManager = new LinearLayoutManager(getContext());
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        DatabaseReference databaseReference = database.child(FIREBASE_DB_PATH_SESSIONS_ITEM)
                .child(Utils.getCurrentUserUid());

        mFirebaseAdapter = new SessionItemsDataAdapter(
                SessionItem.class,
                R.layout.list_item_history,
                SessionItemViewHolder.class,
                databaseReference);
        ((SessionItemsDataAdapter) mFirebaseAdapter).setOnClickListener(new SessionItemViewHolder.ClickListener() {
            @Override
            public void onItemClick(int position) {
                ((HistoryItemSelectionCallback) getActivity()).displayHistoryItemByKey(mFirebaseAdapter.getRef(position).getKey());
            }
        });

        mProgressBar.setVisibility(ProgressBar.GONE);

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

}
