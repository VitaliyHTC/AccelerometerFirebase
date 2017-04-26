package com.vitaliyhtc.accelerometerfirebase.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.vitaliyhtc.accelerometerfirebase.R;
import com.vitaliyhtc.accelerometerfirebase.models.FileInfoOnStorage;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

public class FileInfoOnStorageAdapter extends FirebaseRecyclerAdapter<FileInfoOnStorage, FileInfoOnStorageAdapter.FileInfoOnStorageViewHolder> {

    private FileInfoOnStorageViewHolder.DownloadClickListener mDownloadClickListener;
    private FileInfoOnStorageViewHolder.DeleteClickListener mDeleteClickListener;

    public FileInfoOnStorageAdapter(Class<FileInfoOnStorage> modelClass, int modelLayout, Class<FileInfoOnStorageViewHolder> viewHolderClass, DatabaseReference ref) {
        super(modelClass, modelLayout, viewHolderClass, ref);
    }

    @Override
    protected void populateViewHolder(FileInfoOnStorageViewHolder viewHolder, FileInfoOnStorage model, int position) {
        if (model != null) {
            viewHolder.bind(model);
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


    public static class FileInfoOnStorageViewHolder extends RecyclerView.ViewHolder {

        TextView filenameTextView;
        TextView mimetypeTextView;
        TextView filesizeTextView;
        ImageView downloadImageView;
        ImageView deleteImageView;

        private DownloadClickListener mDownloadClickListener;
        private DeleteClickListener mDeleteClickListener;

        public FileInfoOnStorageViewHolder(View v) {
            super(v);

            filenameTextView = (TextView) v.findViewById(R.id.tv_filename);
            mimetypeTextView = (TextView) v.findViewById(R.id.tv_mimetype);
            filesizeTextView = (TextView) v.findViewById(R.id.tv_filesize);
            downloadImageView = (ImageView) v.findViewById(R.id.tools_ib_download_file);
            deleteImageView = (ImageView) v.findViewById(R.id.tools_ib_delete_file);

            downloadImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDownloadClickListener.onItemClickDownload(getAdapterPosition());
                }
            });
            deleteImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mDeleteClickListener.onItemClickDelete(getAdapterPosition());
                }
            });
        }

        void bind(FileInfoOnStorage model) {
            filenameTextView.setText(model.getFilename());
            mimetypeTextView.setText(model.getMimeType());
            filesizeTextView.setText(Utils.humanReadableByteCount(model.getTotalByteCount()));
        }

        void setDownloadClickListener(DownloadClickListener downloadClickListener) {
            mDownloadClickListener = downloadClickListener;
        }

        void setDeleteClickListener(DeleteClickListener deleteClickListener) {
            mDeleteClickListener = deleteClickListener;
        }

        public interface DownloadClickListener {
            void onItemClickDownload(int position);
        }

        public interface DeleteClickListener {
            void onItemClickDelete(int position);
        }
    }
}
