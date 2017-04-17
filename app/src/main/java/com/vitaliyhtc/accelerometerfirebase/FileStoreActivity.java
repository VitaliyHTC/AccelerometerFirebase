package com.vitaliyhtc.accelerometerfirebase;

import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.vitaliyhtc.accelerometerfirebase.model.UploadTaskListeners;
import com.vitaliyhtc.accelerometerfirebase.model.FileInfoOnDevice;
import com.vitaliyhtc.accelerometerfirebase.model.FileInfoOnStorage;
import com.vitaliyhtc.accelerometerfirebase.model.FileStoreUploadedFiles;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * окей, тоді firebase, треба трохи переробити.
 * +++ Після логінізації екран з двома кнопками "Accelerometer" i "FileStore",
 * +++ При кліку на першу кнопку переход на твоє завдання фактично,
 * <p>
 * TODO: 2017.04.12
 * при кліку на другу кнопку відкривається новий скрін де вибираєш файл(один або декілька) і завантажуєш їх на GoogleCloudStore
 * під час завантаження показується прогрес, завантаження автоматично на паузу і продовжується в залежності від onPause, onResume
 * завантажені файли користувача записуються в окрему вітку бази і є можливість їх переглянути і скачати
 */
public class FileStoreActivity extends AppCompatActivity {

    private static final String TAG = "FileStoreActivity";
    private static final int REQUEST_CODE_SELECT_FILE = 0x0010;
    private static final String STORAGE_REFERENCE_FILES = "files";
    private static final String DATABASE_REFERENCE_FILES = "files";

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;

    private FileStoreUploadedFiles mUploadedFilesInfo;
    private List<UploadTask> mUploadTasks;
    private Map<UploadTask, UploadTaskListeners> mUploadTaskListeners;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filestore);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        init();
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

        // Find all UploadTasks under this StorageReference (in this example, there should be one)
        mUploadTasks = mStorageReference.getActiveUploadTasks();
        performFileUploadingByUploadTasks(mUploadTasks);
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
                intent = new Intent(this, LaunchActivity.class);
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

        if (requestCode == REQUEST_CODE_SELECT_FILE) {
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
                //showUploadedFiles(mUploadedFilesUrls);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException());
            }
        });
    }

    // TODO: read/write files permissions request for android 6+
    // http://stackoverflow.com/questions/30789116/implementing-a-file-picker-in-android-and-copying-the-selected-file-to-another-l
    // See dexter, and add read external storage permission in manifest and realtime permission for android 6+
    // Media store for path by uri

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
                StorageReference riversRef = mStorageReference.child(fileInfoOnDevice.getFileName());
                final UploadTask uploadTask = riversRef.putFile(file);
                mUploadTasks.add(uploadTask);
            }
        }
        performFileUploadingByUploadTasks(mUploadTasks);
    }

    private void performFileUploadingByUploadTasks(List<UploadTask> uploadTasks) {
        for (UploadTask uploadTask : uploadTasks) {
            // TODO: UI for loading init here
            registerListenersForUploadTask(uploadTask);
        }
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
                System.out.println("Upload is paused");
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
