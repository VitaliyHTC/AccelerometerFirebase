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
import com.vitaliyhtc.accelerometerfirebase.model.UploadTaskListeners;
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
 * +++ Після логінізації екран з двома кнопками "Accelerometer" i "FileStore",
 * +++ При кліку на першу кнопку переход на твоє завдання фактично,
 * TODO: 2017.04.12
 * +++ при кліку на другу кнопку відкривається новий скрін
 * +++ де вибираєш файл(один або декілька) і завантажуєш їх на GoogleCloudStore
 * під час завантаження показується прогрес,
 * +++ завантаження автоматично на паузу і продовжується в залежності від onPause, onResume
 * +++ завантажені файли користувача записуються в окрему вітку бази
 * і є можливість їх переглянути і скачати
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
    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private FileStoreUploadedFiles mUploadedFilesInfo;
    private List<UploadTask> mUploadTasks;
    private Map<UploadTask, UploadTaskListeners> mUploadTaskListeners;
    private FirebaseRecyclerAdapter<FileInfoOnStorage, FileInfoOnStorageViewHolder> mFirebaseAdapter;
    private LinearLayoutManager mLinearLayoutManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filestore);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

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
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // If there was an upload in progress, get its reference and create a new StorageReference
        final String stringRef = savedInstanceState.getString("reference");
        if (stringRef == null) {
            return;
        }
        mStorageReference = FirebaseStorage.getInstance().getReferenceFromUrl(stringRef);
        // Find all UploadTasks under this StorageReference
        mUploadTasks.addAll(mStorageReference.getActiveUploadTasks());
        performListenersRegistrationForUploadTasks(mUploadTasks);
    }

    @Override
    protected void onPause() {
        super.onPause();
        for (UploadTask uploadTask : mUploadTasks) {
            uploadTask.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        for (UploadTask uploadTask : mUploadTasks) {
            uploadTask.resume();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        for (UploadTask uploadTask : mUploadTasks) {
            UploadTaskListeners uploadTaskListeners = mUploadTaskListeners.get(uploadTask);
            uploadTask.removeOnFailureListener(uploadTaskListeners.getOnFailureListener());
            uploadTask.removeOnSuccessListener(uploadTaskListeners.getOnSuccessListener());
            uploadTask.removeOnPausedListener(uploadTaskListeners.getOnPausedListener());
            uploadTask.removeOnProgressListener(uploadTaskListeners.getOnProgressListener());
        }
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
        if (ActivityCompat.checkSelfPermission(FileStoreActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            Dexter.withActivity(FileStoreActivity.this)
                    .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            init();
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
        // TODO: show error. Unable to perform any operations with files. Permission denied. etc...
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

    private void init() {
        mStorageReference = FirebaseStorage.getInstance().getReference()
                .child(STORAGE_REFERENCE_FILES)
                .child(Utils.getCurrentUserUid());
        mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_REFERENCE_FILES)
                .child(Utils.getCurrentUserUid());

        mUploadTasks = new ArrayList<>();
        mUploadTaskListeners = new HashMap<>();

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
            // TODO: init UI for uploading progress
            registerListenersForUploadTask(uploadTask);
        }
    }

    private void actionDownloadFile(final FileInfoOnStorage fileInfo) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            StorageReference fileRef = mStorageReference.child(fileInfo.getFilename());
            String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
            File file = new File(baseDir + File.separator + Environment.DIRECTORY_DOWNLOADS
                    + File.separator + fileInfo.getFilename());

            fileRef.getFile(file).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Toast.makeText(
                            FileStoreActivity.this,
                            fileInfo.getFilename() + " downloaded to your ExternalStorage Download directory.",
                            Toast.LENGTH_LONG
                    ).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    Log.e(TAG, "actionDownloadFile.onFailure: ", exception);
                    Toast.makeText(FileStoreActivity.this, "File downloading failed!", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            Toast.makeText(FileStoreActivity.this,
                    "No ExternalStorage found. Unable to download file.", Toast.LENGTH_LONG).show();
        }
    }

    private void actionDeleteFile(final FileInfoOnStorage fileInfo) {
        StorageReference fileRef = mStorageReference.child(fileInfo.getFilename());
        fileRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(FileStoreActivity.this,
                        fileInfo.getFilename() + " deleting successful!", Toast.LENGTH_LONG).show();
                mUploadedFilesInfo.getUploadedFilesInfo().remove(fileInfo);
                mDatabaseReference.setValue(mUploadedFilesInfo);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.e(TAG, "actionDeleteFile.onFailure: ", exception);
                Toast.makeText(FileStoreActivity.this, "File deleting failed!", Toast.LENGTH_LONG).show();
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
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                mUploadedFilesInfo.getUploadedFilesInfo().add(new FileInfoOnStorage(
                        taskSnapshot.getMetadata().getName(),
                        taskSnapshot.getMetadata().getPath(),
                        taskSnapshot.getDownloadUrl().toString(),
                        taskSnapshot.getTotalByteCount(),
                        taskSnapshot.getMetadata().getContentType()
                ));
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
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                System.out.println("Upload is " + progress + "% done");
            }
        };

        mUploadTaskListeners.put(
                uploadTask,
                new UploadTaskListeners(
                        onFailureListener, onSuccessListener, onPausedListener, onProgressListener)
        );

        uploadTask.addOnFailureListener(onFailureListener);
        uploadTask.addOnSuccessListener(onSuccessListener);
        uploadTask.addOnPausedListener(onPausedListener);
        uploadTask.addOnProgressListener(onProgressListener);
    }

}
