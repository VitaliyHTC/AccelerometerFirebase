package com.vitaliyhtc.accelerometerfirebase.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.vitaliyhtc.accelerometerfirebase.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class FileInfoOnStorageViewHolder extends RecyclerView.ViewHolder {

    @BindView(R.id.tv_filename)
    TextView filenameTextView;

    @BindView(R.id.tv_mimetype)
    TextView mimetypeTextView;

    @BindView(R.id.tv_filesize)
    TextView filesizeTextView;

    @BindView(R.id.tools_ib_download_file)
    ImageView downloadImageView;

    @BindView(R.id.tools_ib_delete_file)
    ImageView deleteImageView;

    private DownloadClickListener mDownloadClickListener;
    private DeleteClickListener mDeleteClickListener;

    public FileInfoOnStorageViewHolder(View v) {
        super(v);
        ButterKnife.bind(this, v);

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

    public TextView getFilenameTextView() {
        return filenameTextView;
    }

    public void setFilenameTextView(TextView filenameTextView) {
        this.filenameTextView = filenameTextView;
    }

    public TextView getMimetypeTextView() {
        return mimetypeTextView;
    }

    public void setMimetypeTextView(TextView mimetypeTextView) {
        this.mimetypeTextView = mimetypeTextView;
    }

    public TextView getFilesizeTextView() {
        return filesizeTextView;
    }

    public void setFilesizeTextView(TextView filesizeTextView) {
        this.filesizeTextView = filesizeTextView;
    }

    public DownloadClickListener getDownloadClickListener() {
        return mDownloadClickListener;
    }

    public void setDownloadClickListener(DownloadClickListener downloadClickListener) {
        mDownloadClickListener = downloadClickListener;
    }

    public DeleteClickListener getDeleteClickListener() {
        return mDeleteClickListener;
    }

    public void setDeleteClickListener(DeleteClickListener deleteClickListener) {
        mDeleteClickListener = deleteClickListener;
    }

    public interface DownloadClickListener {
        void onItemClickDownload(int position);
    }

    public interface DeleteClickListener {
        void onItemClickDelete(int position);
    }
}
