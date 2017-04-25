package com.vitaliyhtc.accelerometerfirebase;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.vitaliyhtc.accelerometerfirebase.adapters.FileInfoOnStorageAdapter;
import com.vitaliyhtc.accelerometerfirebase.model.FileInfoOnDevice;
import com.vitaliyhtc.accelerometerfirebase.model.FileInfoOnStorage;
import com.vitaliyhtc.accelerometerfirebase.model.FileStoreUploadedFiles;
import com.vitaliyhtc.accelerometerfirebase.model.ProgressItem;
import com.vitaliyhtc.accelerometerfirebase.model.TaskListeners;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;
import com.vitaliyhtc.accelerometerfirebase.viewholder.FileInfoOnStorageViewHolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * окей, тоді firebase, треба трохи переробити.
 * + Після логінізації екран з двома кнопками "Accelerometer" i "FileStore",
 * + При кліку на першу кнопку переход на твоє завдання фактично,
 * + при кліку на другу кнопку відкривається новий скрін
 * + де вибираєш файл(один або декілька) і завантажуєш їх на GoogleCloudStore
 * + під час завантаження показується прогрес,
 * ! завантаження автоматично на паузу і продовжується в залежності від onPause, onResume
 * ! not possible to implement with firebase 10.2.1 version due to bug in client library.
 * ! see: http://stackoverflow.com/questions/43355928/resuming-uploadtask-results-in-e-storageexception-bufferedinputstream-is-closed
 * + завантажені файли користувача записуються в окрему вітку бази
 * + і є можливість їх переглянути і скачати
 */
public class FileStoreActivity extends AppCompatActivity {

    private static final String TAG = "FileStoreActivity";
    private static final int REQUEST_CODE_SELECT_FILE = 0x0010;
    private static final String STORAGE_REFERENCE_FILES = "files";
    private static final String DATABASE_REFERENCE_FILES = "files";

    // same as name of list in FileStoreUploadedFiles
    private static final String DATABASE_REFERENCE_FILE_LIST_NAME = "uploadedFilesInfo";

    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.tools_ib_upload_file)
    ImageView mUploadImageView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.permissions_error)
    RelativeLayout mPermissionsError;

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private FileStoreUploadedFiles mUploadedFilesInfo;
    private List<UploadTask> mUploadTasks;
    private Map<UploadTask, TaskListeners<UploadTask.TaskSnapshot>> mUploadTaskListeners;
    private List<FileDownloadTask> mFileDownloadTasks;
    private Map<FileDownloadTask, TaskListeners<FileDownloadTask.TaskSnapshot>> mFileDownloadTaskListeners;
    private FirebaseRecyclerAdapter<FileInfoOnStorage, FileInfoOnStorageViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;
    private Map<String, ProgressItem> mProgressMap;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filestore);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        initVariables();
        restoreState(savedInstanceState);

        requestWriteExternalStoragePermission();
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // If there's an upload in progress, save the reference so you can query it later
        if (mStorageReference != null) {
            outState.putString("reference", mStorageReference.toString());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*
 * not possible to implement with firebase 10.2.1 version due to bug in client library.
 * see: http://stackoverflow.com/questions/43355928/resuming-uploadtask-results-in-e-storageexception-bufferedinputstream-is-closed
        for (UploadTask uploadTask : mUploadTasks) {
            uploadTask.pause();
        }
        */
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*
 * not possible to implement with firebase 10.2.1 version due to bug in client library.
 * see: http://stackoverflow.com/questions/43355928/resuming-uploadtask-results-in-e-storageexception-bufferedinputstream-is-closed
        for (UploadTask uploadTask : mUploadTasks) {
            uploadTask.resume();
        }
        */
    }

    @Override
    protected void onStop() {
        super.onStop();

        unregisterListeners();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case android.R.id.home:
                intent = new Intent(FileStoreActivity.this, LaunchActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @OnClick(R.id.tools_ib_upload_file)
    void uploadFile() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        } else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("*/*");
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECT_FILE && data != null) {
            List<Uri> urisOfFilesToUpload = new ArrayList<>();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    urisOfFilesToUpload.add(clipData.getItemAt(i).getUri());
                }
            } else {
                urisOfFilesToUpload.add(data.getData());
            }
            performFileUploadingByUri(urisOfFilesToUpload);
        }
    }

    private void requestWriteExternalStoragePermission() {
        // TODO: 25/04/17 no need to check permissions, just call dexter
        if (ActivityCompat.checkSelfPermission(FileStoreActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            init();
            mPermissionsError.setVisibility(View.GONE);
        } else {
            Dexter.withActivity(FileStoreActivity.this)
                    .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            init();
                            mPermissionsError.setVisibility(View.GONE);
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            onPermissionDeniedResume(response);
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            onPermissionRationaleShouldBeShownResume(permission, token);
                        }
                    }).check();
        }
    }

    private void onPermissionDeniedResume(PermissionDeniedResponse response) {
        mPermissionsError.setVisibility(View.VISIBLE);
    }

    private void onPermissionRationaleShouldBeShownResume(PermissionRequest permission, final PermissionToken token) {
        new AlertDialog.Builder(FileStoreActivity.this).setTitle(R.string.permission_rationale_title)
                .setMessage(R.string.permission_rationale_message)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.cancelPermissionRequest();
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        token.continuePermissionRequest();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        token.cancelPermissionRequest();
                    }
                })
                .show();
    }

    private void initVariables() {
        // TODO: 25/04/17 create firebase endpoint abstraction get work with firebase storage and firebase database in terms of this application
//        for example FirebaseHelper.getCurrentUserStoreRef(), etc, ...
        mStorageReference = FirebaseStorage.getInstance().getReference()
                .child(STORAGE_REFERENCE_FILES)
                .child(Utils.getCurrentUserUid());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_REFERENCE_FILES)
                .child(Utils.getCurrentUserUid());

        mUploadTasks = new ArrayList<>();
        mUploadTaskListeners = new HashMap<>();

        mFileDownloadTasks = new ArrayList<>();
        mFileDownloadTaskListeners = new HashMap<>();

        mProgressMap = new HashMap<>();
    }

    private void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            // If there was an upload in progress, get its reference and create a new StorageReference
            String stringRef = savedInstanceState.getString("reference");
            if (stringRef == null) {
                return;
            }
            mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);
            // Find all UploadTasks under this StorageReference
            mUploadTasks.addAll(mStorageReference.getActiveUploadTasks());
            performListenersRegistrationForUploadTasks(mUploadTasks);
            // Find all FileDownloadTasks under this StorageReference
            mFileDownloadTasks.addAll(mStorageReference.getActiveDownloadTasks());
            performListenersRegistrationForFileDownloadTasks(mFileDownloadTasks);
        }
    }

    private void init() {
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mUploadedFilesInfo = dataSnapshot.getValue(FileStoreUploadedFiles.class);
                if (mUploadedFilesInfo == null) {
                    mUploadedFilesInfo = new FileStoreUploadedFiles();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });

        mUploadImageView.setVisibility(View.VISIBLE);

        initFileList();
    }

    private void initFileList() {
        mLinearLayoutManager = new LinearLayoutManager(FileStoreActivity.this);
        mLinearLayoutManager.setStackFromEnd(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mFirebaseAdapter = new FileInfoOnStorageAdapter(
                FileInfoOnStorage.class,
                R.layout.list_item_file_info_on_storage,
                FileInfoOnStorageViewHolder.class,
                mDatabaseReference.child(DATABASE_REFERENCE_FILE_LIST_NAME));

        // TODO: 25/04/17 why not private FileInfoOnStorageAdapter mFirebaseAdapter?????
        ((FileInfoOnStorageAdapter) mFirebaseAdapter).setDownloadClickListener(new FileInfoOnStorageViewHolder.DownloadClickListener() {
            @Override
            public void onItemClickDownload(int position) {
                actionDownloadFile(mFirebaseAdapter.getItem(position));
            }
        });
        ((FileInfoOnStorageAdapter) mFirebaseAdapter).setDeleteClickListener(new FileInfoOnStorageViewHolder.DeleteClickListener() {
            @Override
            public void onItemClickDelete(int position) {
                actionDeleteFile(mFirebaseAdapter.getItem(position));
            }
        });

        mFirebaseAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                int fileItemsCount = mFirebaseAdapter.getItemCount();
                int lastVisiblePosition = mLinearLayoutManager.findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == -1 ||
                        (positionStart >= (fileItemsCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    mRecyclerView.scrollToPosition(positionStart);
                }
            }
        });

        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mFirebaseAdapter);
    }

    private void performFileUploadingByUri(List<Uri> uris) {
        for (Uri fileUri : uris) {
            FileInfoOnDevice fileInfoOnDevice = FileInfoOnDevice.getFileInfo(FileStoreActivity.this, fileUri);
            if (fileInfoOnDevice != null) {
                /*
                Log.e(TAG, "URI: " + fileUri + "; Path: " + fileInfoOnDevice.getPathToTheFile() +
                        "; Size: " + fileInfoOnDevice.getSize() + "; Name: " + fileInfoOnDevice.getFileName() +
                        "; MimeType: " + fileInfoOnDevice.getMimeType() + ";");
                */

                Uri file = Uri.fromFile(new File(fileInfoOnDevice.getPathToTheFile()));
                StorageReference fileRef = mStorageReference.child(fileInfoOnDevice.getFileName());
                final UploadTask uploadTask = fileRef.putFile(file);
                mUploadTasks.add(uploadTask);
            }
        }
        performListenersRegistrationForUploadTasks(mUploadTasks);
    }

    private void performListenersRegistrationForUploadTasks(List<UploadTask> uploadTasks) {
        for (UploadTask uploadTask : uploadTasks) {
            registerListenersForUploadTask(uploadTask);
        }
    }

    private void performListenersRegistrationForFileDownloadTasks(List<FileDownloadTask> fileDownloadTasks) {
        for (FileDownloadTask fileDownloadTask : fileDownloadTasks) {
            registerListenersForFileDownloadTask(fileDownloadTask);
        }
    }

    private void actionDownloadFile(final FileInfoOnStorage fileInfo) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StorageReference fileRef = mStorageReference.child(fileInfo.getFilename());
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(baseDir + File.separator + Environment.DIRECTORY_DOWNLOADS
                    + File.separator + fileInfo.getFilename());

            FileDownloadTask fileDownloadTask = fileRef.getFile(file);
            registerListenersForFileDownloadTask(fileDownloadTask);
        } else {
            Toast.makeText(FileStoreActivity.this,
                    R.string.filestore_toast_no_external_storage_found, Toast.LENGTH_LONG).show();
        }
    }

    private void actionDeleteFile(final FileInfoOnStorage fileInfo) {
        StorageReference fileRef = mStorageReference.child(fileInfo.getFilename());
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FileStoreActivity.this,
                        fileInfo.getFilename() + " " + getString(R.string.filestore_toast_file_has_been_deleted), Toast.LENGTH_LONG).show();
                mUploadedFilesInfo.getUploadedFilesInfo().remove(fileInfo);
                mDatabaseReference.setValue(mUploadedFilesInfo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "actionDeleteFile.onFailure: ", exception);
                Toast.makeText(FileStoreActivity.this, R.string.filestore_toast_file_deleting_failed, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerListenersForUploadTask(final UploadTask uploadTask) {
        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "onFailure: ", exception);
            }
        };
        OnSuccessListener<UploadTask.TaskSnapshot> onSuccessListener = new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                FileInfoOnStorage fileInfoOnStorage = new FileInfoOnStorage(
                        taskSnapshot.getMetadata().getName(),
                        taskSnapshot.getMetadata().getPath(),
                        taskSnapshot.getDownloadUrl().toString(),
                        taskSnapshot.getTotalByteCount(),
                        taskSnapshot.getMetadata().getContentType()
                );
                if (mUploadedFilesInfo.getUploadedFilesInfo().contains(fileInfoOnStorage)) {
                    mUploadedFilesInfo.getUploadedFilesInfo().remove(fileInfoOnStorage);
                }
                mUploadedFilesInfo.getUploadedFilesInfo().add(fileInfoOnStorage);
                mDatabaseReference.setValue(mUploadedFilesInfo);
                mUploadTasks.remove(uploadTask);
            }
        };
        OnPausedListener<UploadTask.TaskSnapshot> onPausedListener = new OnPausedListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                Log.i(TAG, "onPaused: Upload is paused");
            }
        };
        OnProgressListener<UploadTask.TaskSnapshot> onProgressListener = new OnProgressListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                // TODO: 19.04.17 bad code with hashcode. Try to find better solution.
                updateProgressBar(
                        uploadTask.hashCode() + "",
                        taskSnapshot.getBytesTransferred(),
                        taskSnapshot.getTotalByteCount()
                );
            }
        };

        mUploadTaskListeners.put(
                uploadTask,
                new TaskListeners<>(onFailureListener, onSuccessListener, onPausedListener, onProgressListener)
        );

        uploadTask.addOnFailureListener(onFailureListener);
        uploadTask.addOnSuccessListener(onSuccessListener);
        uploadTask.addOnPausedListener(onPausedListener);
        uploadTask.addOnProgressListener(onProgressListener);
    }

    private void registerListenersForFileDownloadTask(final FileDownloadTask fileDownloadTask) {
        OnFailureListener onFailureListener = new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "actionDownloadFile.onFailure: ", exception);
                Toast.makeText(FileStoreActivity.this, R.string.filestore_toast_file_downloading_failed, Toast.LENGTH_LONG).show();
            }
        };
        OnSuccessListener<FileDownloadTask.TaskSnapshot> onSuccessListener = new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(
                        FileStoreActivity.this,
                        getString(R.string.filestore_toast_file_downloaded_to_your_download_directory),
                        Toast.LENGTH_LONG
                ).show();
                mFileDownloadTasks.remove(fileDownloadTask);
            }
        };
        OnProgressListener<FileDownloadTask.TaskSnapshot> onProgressListener = new OnProgressListener<FileDownloadTask.TaskSnapshot>() {
            @SuppressWarnings("VisibleForTests")
            @Override
            public void onProgress(FileDownloadTask.TaskSnapshot taskSnapshot) {
                // TODO: 19.04.17 bad code with hashcode. Try to find better solution.
                updateProgressBar(
                        fileDownloadTask.hashCode() + "",
                        taskSnapshot.getBytesTransferred(),
                        taskSnapshot.getTotalByteCount()
                );
            }
        };

        mFileDownloadTaskListeners.put(
                fileDownloadTask,
                new TaskListeners<>(onFailureListener, onSuccessListener, null, onProgressListener)
        );

        fileDownloadTask.addOnFailureListener(onFailureListener);
        fileDownloadTask.addOnSuccessListener(onSuccessListener);
        fileDownloadTask.addOnProgressListener(onProgressListener);
    }

    private void unregisterListeners() {
        for (UploadTask uploadTask : mUploadTasks) {
            TaskListeners<UploadTask.TaskSnapshot> uploadTaskListeners = mUploadTaskListeners.get(uploadTask);
            uploadTask.removeOnFailureListener(uploadTaskListeners.getOnFailureListener());
            uploadTask.removeOnSuccessListener(uploadTaskListeners.getOnSuccessListener());
            uploadTask.removeOnPausedListener(uploadTaskListeners.getOnPausedListener());
            uploadTask.removeOnProgressListener(uploadTaskListeners.getOnProgressListener());
        }
        for (FileDownloadTask fileDownloadTask : mFileDownloadTasks) {
            TaskListeners<FileDownloadTask.TaskSnapshot> fileDownloadTaskListeners = mFileDownloadTaskListeners.get(fileDownloadTask);
            OnFailureListener onFailureListener = fileDownloadTaskListeners.getOnFailureListener();
            if (onFailureListener != null) {
                fileDownloadTask.removeOnFailureListener(onFailureListener);
            }
            OnSuccessListener<FileDownloadTask.TaskSnapshot> onSuccessListener = fileDownloadTaskListeners.getOnSuccessListener();
            if (onSuccessListener != null) {
                fileDownloadTask.removeOnSuccessListener(onSuccessListener);
            }
            OnPausedListener<FileDownloadTask.TaskSnapshot> onPausedListener = fileDownloadTaskListeners.getOnPausedListener();
            if (onPausedListener != null) {
                fileDownloadTask.removeOnPausedListener(onPausedListener);
            }
            OnProgressListener<FileDownloadTask.TaskSnapshot> onProgressListener = fileDownloadTaskListeners.getOnProgressListener();
            if (onProgressListener != null) {
                fileDownloadTask.removeOnProgressListener(onProgressListener);
            }
        }
    }


    private void updateProgressBar(String name, long bytesTransfered, long totalByteCount) {
        if (!mProgressMap.containsKey(name)) {
            mProgressMap.put(name, new ProgressItem(name, bytesTransfered, totalByteCount));
        } else {
            mProgressMap.get(name).setBytesTransfered(bytesTransfered);
            mProgressMap.get(name).setTotalByteCount(totalByteCount);
        }
        updateProgressBarUi();
    }

    private void updateProgressBarUi() {
        mProgressBar.setVisibility(View.VISIBLE);
        long bytesTransfered = 0;
        long totalByteCount = 0;
        for (ProgressItem progressItem : mProgressMap.values()) {
            bytesTransfered += progressItem.getBytesTransfered();
            totalByteCount += progressItem.getTotalByteCount();
        }
        int progress = (int) ((100 * bytesTransfered) / totalByteCount);
        mProgressBar.setProgress(progress);
        if (progress == 100) {
            mProgressBar.setVisibility(View.GONE);
            mProgressBar.setProgress(0);
            mProgressMap.clear();
        }
    }
}
