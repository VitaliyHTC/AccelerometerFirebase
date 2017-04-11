package com.vitaliyhtc.accelerometerfirebase.interfaces;

import com.google.firebase.auth.FirebaseUser;

public interface ActivityToDataFragment {

    FirebaseUser getFirebaseUser();
    void displayHistoryItemByKey(String sessionItemKey);
}
