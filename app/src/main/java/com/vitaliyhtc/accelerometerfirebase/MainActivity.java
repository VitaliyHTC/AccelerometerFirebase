package com.vitaliyhtc.accelerometerfirebase;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;
import com.vitaliyhtc.accelerometerfirebase.interfaces.ActivityToDataFragment;
import com.vitaliyhtc.accelerometerfirebase.interfaces.SessionItemFragment;
import com.vitaliyhtc.accelerometerfirebase.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener,
        ActivityToDataFragment {

    private static final String TAG = "MainActivity";

    private static final String KEY_IS_MAIN_SERVICE_RUNNING = "isMainServiceRunning";

    private static final String KEY_DISPLAYED_FRAGMENT_ID = "displayedFragmentId";
    private static final int VALUE_FRAGMENT_HISTORY = 0x01;
    private static final int VALUE_FRAGMENT_LIST = 0x02;
    private static final int VALUE_FRAGMENT_GRAPH = 0x03;

    private static final String KEY_DISPLAYED_SESSION_ITEM_KEY = "displayedSessionItemKey";
    public static final String VALUE_DISPLAYED_SESSION_ITEM_KEY_OFF = "0x0FF";

    public static final String ANONYMOUS = "anonymous";

    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    //views
    @BindView(R.id.userImageView) CircleImageView mUserImageView;
    @BindView(R.id.tv_userName) TextView mUserNameView;
    @BindView(R.id.btn_start_logging) Button mButtonStart;
    @BindView(R.id.btn_stop_logging) Button mButtonStop;
    @BindView(R.id.btn_data_history) Button mButtonHistory;
    @BindView(R.id.btn_data_list) Button mButtonShowAsList;
    @BindView(R.id.btn_data_graph) Button mButtonShowAsGraph;

    private boolean isMainServiceRunning;

    //fields for fragments
    FragmentManager mFragmentManager;

    private int mLastDisplayedFragment;
    private String mLastDisplayedSessionItemKey;



    // TODO: 12.04.17 Method hierarchy. First public/protected and then private
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        if (savedInstanceState!=null) {
            isMainServiceRunning = savedInstanceState.getBoolean(KEY_IS_MAIN_SERVICE_RUNNING, false);
            mLastDisplayedFragment = savedInstanceState.getInt(KEY_DISPLAYED_FRAGMENT_ID, VALUE_FRAGMENT_HISTORY);
            mLastDisplayedSessionItemKey = savedInstanceState.getString(KEY_DISPLAYED_SESSION_ITEM_KEY, VALUE_DISPLAYED_SESSION_ITEM_KEY_OFF);
        } else {
            mLastDisplayedFragment = VALUE_FRAGMENT_HISTORY;
        }

        doAuth();

        if (isMyServiceRunning(MainService.class)) {
            isMainServiceRunning = true;
        }

        if(isMainServiceRunning){
            mButtonStart.setEnabled(false);
        }

        performBroadcastReceiverRegistration();

        // other go here
        mFragmentManager = getSupportFragmentManager();
        initFragments();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean(KEY_IS_MAIN_SERVICE_RUNNING, isMainServiceRunning);
        outState.putInt(KEY_DISPLAYED_FRAGMENT_ID, mLastDisplayedFragment);
        outState.putString(KEY_DISPLAYED_SESSION_ITEM_KEY, mLastDisplayedSessionItemKey);
    }

    private void doAuth(){
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            // TODO: 13/04/17 better to create separate splash activity and inside if user logged in -> MainActivity else SignInActivity 
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            displayUserNameAndImage(mFirebaseUser);
            enableControlButtons();
        }
    }

    private void performBroadcastReceiverRegistration(){
        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                isMainServiceRunning = intent.getExtras().getBoolean(Config.TAG_SERVICE_RUNNING_STATUS, false);
                if(isMainServiceRunning){
                    mButtonStart.setEnabled(false);
                }else{
                    mButtonStart.setEnabled(true);
                }
            }
        };
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter(Config.TAG_ACTIVITY_BROADCAST_RECEIVER));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings_menu:
                Intent intent = new Intent(this, SettingsActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
            case R.id.sign_out_menu:
                mFirebaseAuth.signOut();
                displayAnonymousUser();
                disableControlButtons();
                startActivity(new Intent(this, SignInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // An unresolvable error has occurred and Google APIs (including Sign-In) will not
        // be available.
        Log.d(TAG, "onConnectionFailed:" + connectionResult);
        Toast.makeText(this, "Google Play Services error.", Toast.LENGTH_SHORT).show();
    }



    /* Helper methods ************************************************************************* **/
    // TODO: 12.04.17 Picasso can load uri
    private void displayUserNameAndImage(FirebaseUser firebaseUser){
        Picasso.with(getApplicationContext()).load(mFirebaseUser.getPhotoUrl().toString())
                .into(mUserImageView);
        mUserNameView.setText(mFirebaseUser.getDisplayName());
    }

    private void displayAnonymousUser(){
        Picasso.with(getApplicationContext()).load(R.drawable.ic_account_circle_black_36dp)
                .into(mUserImageView);
        mUserNameView.setText(ANONYMOUS);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void enableControlButtons(){
        mButtonStart.setEnabled(true);
        mButtonStop.setEnabled(true);
    }

    private void disableControlButtons(){
        mButtonStart.setEnabled(false);
        mButtonStop.setEnabled(false);
    }



    /* Buttons, onClick, etc... *************************************************************** **/
    // TODO: 12.04.17 "mFragmentManager.beginTransaction().replace(R.id.container_view, fragment).commit();" is in each click. DRY.
    @OnClick(R.id.btn_start_logging)
    protected void startDataLogging(){
        if (Utils.isNetworkAvailable(getApplicationContext())) {
            startService(new Intent(this, MainService.class));
        } else {
            // TODO: 12.04.17 hardcode in every toast. Check this
            Toast.makeText(getApplicationContext(), "The Internet is disconnected. Please check the connection.", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_stop_logging)
    protected void stopDataLogging(){
        Intent intent = new Intent(Config.TAG_SERVICE_BROADCAST_RECEIVER);
        intent.putExtra(Config.TAG_SERVICE_RUNNING_STATUS, false);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

    @OnClick(R.id.btn_data_history)
    protected void showAsHistory(){
        if (mLastDisplayedFragment != VALUE_FRAGMENT_HISTORY) {
            mFragmentManager.beginTransaction().replace(R.id.container_view, new DataHistoryFragment()).commit();
            mLastDisplayedFragment = VALUE_FRAGMENT_HISTORY;
        }
    }

    @OnClick(R.id.btn_data_list)
    protected void showAsList(){
        if (mLastDisplayedFragment != VALUE_FRAGMENT_LIST) {
            Fragment fragment = new DataListFragment();
            ((SessionItemFragment) fragment).setSessionItemKey(mLastDisplayedSessionItemKey);
            mFragmentManager.beginTransaction().replace(R.id.container_view, fragment).commit();
            mLastDisplayedFragment = VALUE_FRAGMENT_LIST;
        }
    }

    @OnClick(R.id.btn_data_graph)
    protected void showAsGraph(){
        if (mLastDisplayedFragment != VALUE_FRAGMENT_GRAPH) {
            Fragment fragment = new DataGraphFragment();
            ((SessionItemFragment) fragment).setSessionItemKey(mLastDisplayedSessionItemKey);
            mFragmentManager.beginTransaction().replace(R.id.container_view, fragment).commit();
            mLastDisplayedFragment = VALUE_FRAGMENT_GRAPH;
        }
    }

    /* **************************************************************************************** **/

    // TODO: 12.04.17 same as above. DRY
    private void initFragments(){
        FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
        Fragment fragment;
        if (mLastDisplayedFragment == VALUE_FRAGMENT_LIST) {
            fragment = new DataListFragment();
            ((SessionItemFragment) fragment).setSessionItemKey(mLastDisplayedSessionItemKey);
            fragmentTransaction.replace(R.id.container_view, fragment).commit();
        } else if(mLastDisplayedFragment == VALUE_FRAGMENT_GRAPH) {
            fragment = new DataGraphFragment();
            ((SessionItemFragment) fragment).setSessionItemKey(mLastDisplayedSessionItemKey);
            fragmentTransaction.replace(R.id.container_view, fragment).commit();
        } else {
            fragmentTransaction.replace(R.id.container_view, new DataHistoryFragment()).commit();
        }
    }



    @Override
    public FirebaseUser getFirebaseUser() {
        return mFirebaseUser;
    }

    @Override
    public void displayHistoryItemByKey(String sessionItemKey) {
        mLastDisplayedSessionItemKey = sessionItemKey;
        showAsList();
    }
}
