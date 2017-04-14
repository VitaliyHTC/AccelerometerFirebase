package com.vitaliyhtc.accelerometerfirebase;

import android.content.ClipData;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
            Log.e(TAG, "performFileUploadingByUri: " + fileUri+"; Name: "+getFilenamefromUri(this, fileUri));

            // http://stackoverflow.com/questions/30789116/implementing-a-file-picker-in-android-and-copying-the-selected-file-to-another-l
            // See dexter, and add read external storage permission in manifest and realtime permission for android 6+
            // Media store for path by uri

            //mStorageReference.child(Config.FIREBASE_DB_PATH_FILES).child(Utils.getCurrentUserUid()).child(fileUri.getLastPathSegment());
        }
    }


    public static String getFilenamefromUri(Context context, Uri uri) {

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(uri, null, null, null, null);
        String displayName = null;
        if (cursor != null && cursor.moveToFirst()) {

            // Note it's called "Display Name".  This is
            // provider-specific, and might not necessarily be the file name.
            displayName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
            cursor.close();
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            displayName = uri.getPath().replaceAll(".*/", "");
        } else displayName = "unknown filename";
        return displayName;
    }

    public static String getPath(Context context, Uri uri) throws URISyntaxException {
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {"_data"};
            Cursor cursor = null;

            try {
                cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor
                        .getColumnIndexOrThrow("_data");
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
                // Eat it
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }


}
