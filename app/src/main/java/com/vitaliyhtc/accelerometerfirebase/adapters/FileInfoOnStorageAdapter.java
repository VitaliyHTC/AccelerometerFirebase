package com.vitaliyhtc.accelerometerfirebase.adapters;

import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.model.FileInfoOnStorage;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;
import com.vitaliyhtc.accelerometerfirebase.viewholder.FileInfoOnStorageViewHolder;

public class FileInfoOnStorageAdapter extends FirebaseRecyclerAdapter<FileInfoOnStorage, FileInfoOnStorageViewHolder> {

    private FileInfoOnStorageViewHolder.DownloadClickListener mDownloadClickListener;
    private FileInfoOnStorageViewHolder.DeleteClickListener mDeleteClickListener;

    public FileInfoOnStorageAdapter(Class<FileInfoOnStorage> modelClass, int modelLayout, Class<FileInfoOnStorageViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(FileInfoOnStorageViewHolder viewHolder, FileInfoOnStorage model, int position) {
        if (model != null) {
            viewHolder.getFilenameTextView().setText(model.getFilename());
            viewHolder.getMimetypeTextView().setText(model.getMimeType());
            viewHolder.getFilesizeTextView().setText(Utils.humanReadableByteCount(model.getTotalByteCount()));
        }
    }

    @Override
    public FileInfoOnStorageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        FileInfoOnStorageViewHolder viewHolder = super.onCreateViewHolder(parent, viewType);
        viewHolder.setDownloadClickListener(mDownloadClickListener);
        viewHolder.setDeleteClickListener(mDeleteClickListener);
        return viewHolder;
    }

    public void setDownloadClickListener(FileInfoOnStorageViewHolder.DownloadClickListener downloadClickListener) {
        mDownloadClickListener = downloadClickListener;
    }

    public void setDeleteClickListener(FileInfoOnStorageViewHolder.DeleteClickListener deleteClickListener) {
        mDeleteClickListener = deleteClickListener;
    }
}
