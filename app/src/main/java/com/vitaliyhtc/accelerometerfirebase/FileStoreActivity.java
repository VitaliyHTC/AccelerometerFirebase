package com.vitaliyhtc.accelerometerfirebase;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

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

    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

    private List<Uri> mUrisOfFilesToUpload;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filestore);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        init();
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

    private void init() {
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();

        mUrisOfFilesToUpload = new ArrayList<>();
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
            mUrisOfFilesToUpload.clear();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ClipData clipData = data.getClipData();
                for (int i = 0; i < clipData.getItemCount(); i++) {
                    mUrisOfFilesToUpload.add(clipData.getItemAt(i).getUri());
                }
            } else {
                mUrisOfFilesToUpload.add(data.getData());
            }
            performFileUploadingByUri(mUrisOfFilesToUpload);
        }
    }

    private void performFileUploadingByUri(List<Uri> uris) {
        // see https://github.com/haiwen/seadroid/blob/master/app/src/main/java/com/seafile/seadroid2/util/Utils.java line 524
        // TODO: all below
        // mDatabase.child(Config.FIREBASE_DB_PATH_FILES).child(mUser.getUserUid()).setValue(mFiles);

        for (Uri fileUri : uris) {
            Log.e(TAG, "performFileUploadingByUri: " + fileUri + "; Name: " + getPath(fileUri));

            // http://stackoverflow.com/questions/30789116/implementing-a-file-picker-in-android-and-copying-the-selected-file-to-another-l
            // See dexter, and add read external storage permission in manifest and realtime permission for android 6+
            // Media store for path by uri

            //mStorageReference.child(Config.FIREBASE_DB_PATH_FILES).child(Utils.getCurrentUserUid()).child(fileUri.getLastPathSegment());
        }
    }

    private String getPath(Uri uri) {
        String path = null;
        String[] projection = {
                MediaStore.MediaColumns.DATA,
                MediaStore.MediaColumns.SIZE,
                MediaStore.MediaColumns.DISPLAY_NAME,
                MediaStore.MediaColumns.MIME_TYPE
        };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) {
            path = uri.getPath();
        } else {
            cursor.moveToFirst();
            int column_index = cursor.getColumnIndexOrThrow(projection[0]);
            path = cursor.getString(column_index);

            // Create FileInfo model to store DATA, SIZE, DISPLAY_NAME, MIMI_TYPE
            // See ContentResolver, MediaStore.

            cursor.close();
        }
        return ((path == null || path.isEmpty()) ? (uri.getPath()) : path);
    }


}
