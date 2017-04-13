package com.vitaliyhtc.accelerometerfirebase;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import butterknife.ButterKnife;

/**
 * окей, тоді firebase, треба трохи переробити.
 * +++ Після логінізації екран з двома кнопками "Accelerometer" i "FileStore",
 * +++ При кліку на першу кнопку переход на твоє завдання фактично,
 *
 * TODO: 2017.04.12
 * при кліку на другу кнопку відкривається новий скрін де вибираєш файл(один або декілька) і завантажуєш їх на GoogleCloudStore
 * під час завантаження показується прогрес, завантаження автоматично на паузу і продовжується в залежності від onPause, onResume
 * завантажені файли користувача записуються в окрему вітку бази і є можливість їх переглянути і скачати
 */
public class FileStoreActivity extends AppCompatActivity {


    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    private FirebaseStorage mStorage;
    private StorageReference mStorageReference;

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



    private void init(){
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();

        mStorage = FirebaseStorage.getInstance();
        mStorageReference = mStorage.getReference();
    }
}
