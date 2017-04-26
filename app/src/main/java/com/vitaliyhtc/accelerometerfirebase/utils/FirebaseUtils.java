package com.vitaliyhtc.accelerometerfirebase.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public abstract class FirebaseUtils {
    private static final String STORAGE_REFERENCE_FILES = "files";
    private static final String DATABASE_REFERENCE_FILES = "files";

    private FirebaseUtils() {
        throw new AssertionError();
    }


    public static String getCurrentUserUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public static StorageReference getCurrentUserStoreRef() {
        return FirebaseStorage.getInstance().getReference()
                .child(STORAGE_REFERENCE_FILES)
                .child(getCurrentUserUid());
    }

    public static DatabaseReference getCurrentUserDatabaseRef() {
        return FirebaseDatabase.getInstance().getReference()
                .child(DATABASE_REFERENCE_FILES)
                .child(getCurrentUserUid());
    }
}
