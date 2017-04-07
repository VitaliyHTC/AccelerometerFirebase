package com.vitaliyhtc.accelerometerfirebase;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
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
import com.vitaliyhtc.accelerometerfirebase.Utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "MainActivity";

    public static final String ANONYMOUS = "anonymous";

    private String mUsername;
    private String mPhotoUrl;
    private SharedPreferences mSharedPreferences;
    private GoogleApiClient mGoogleApiClient;

    // Firebase instance variables
    private FirebaseAuth mFirebaseAuth;
    private FirebaseUser mFirebaseUser;

    //views
    @BindView(R.id.userImageView) CircleImageView mUserImageView;
    @BindView(R.id.tv_userName) TextView mUserNameView;
    @BindView(R.id.btn_start_logging) Button mButtonStart;
    @BindView(R.id.btn_stop_logging) Button mButtonStop;
    @BindView(R.id.btn_data_list) Button mButtonShowAsList;
    @BindView(R.id.btn_data_graph) Button mButtonShowAsGraph;

    private boolean isMainServiceRunning;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        doAuth();

        performBroadcastReceiverRegistration();

        // other go here
    }

    private void doAuth(){
        mUsername = ANONYMOUS;

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this /* FragmentActivity */, this /* OnConnectionFailedListener */)
                .addApi(Auth.GOOGLE_SIGN_IN_API)
                .build();

        // Initialize Firebase Auth
        mFirebaseAuth = FirebaseAuth.getInstance();
        mFirebaseUser = mFirebaseAuth.getCurrentUser();
        if (mFirebaseUser == null) {
            // Not signed in, launch the Sign In activity
            startActivity(new Intent(this, SignInActivity.class));
            finish();
            return;
        } else {
            mUsername = mFirebaseUser.getDisplayName();
            if (mFirebaseUser.getPhotoUrl() != null) {
                mPhotoUrl = mFirebaseUser.getPhotoUrl().toString();
            }
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
                mUsername = ANONYMOUS;
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

    private void enableControlButtons(){
        mButtonStart.setEnabled(true);
        mButtonStop.setEnabled(true);
    }

    private void disableControlButtons(){
        mButtonStart.setEnabled(false);
        mButtonStop.setEnabled(false);
    }



    /* **************************************************************************************** **/

    @OnClick(R.id.btn_start_logging)
    protected void startDataLogging(){

        if (Utils.isNetworkAvailable(getApplicationContext())) {
            startService(new Intent(this, MainService.class));
        } else {
            Toast.makeText(getApplicationContext(), "The Internet is disconnected. Please check the connection.", Toast.LENGTH_LONG).show();
        }
    }

    @OnClick(R.id.btn_stop_logging)
    protected void stopDataLogging(){

        Intent intent = new Intent(Config.TAG_SERVICE_BROADCAST_RECEIVER);
        intent.putExtra(Config.TAG_SERVICE_RUNNING_STATUS, false);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }

}
