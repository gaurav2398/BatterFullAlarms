package com.gaurav.project.batterfullalarm;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String PREFERENCES_NAME = "com.gaurav.project.batterfullalarm";
    public static final String PREFERENCE_KEY_ENABLED = "enabled";
    public static final String PREFERENCE_KEY_VIBRATE = "vibrate";
    public static final String PREFERENCE_KEY_SOUND = "sound";

    private SharedPreferences mPreferences;
    private Switch mEnabledSwitch;

    private InterstitialAd mInterstitialAd;

    public static Button stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportActionBar().setIcon(R.drawable.ic_battery);
        getSupportActionBar().setTitle("   Battery Full Alarm");
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        mPreferences = getSharedPreferences(PREFERENCES_NAME, 0);

        setContentView(R.layout.activity_main);

        ((TextView) findViewById(R.id.version_text)).setText("VERSION_NAME");

        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        MobileAds.initialize(this, "ca-app-pub-4250344724353850~7015455327");
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-4250344724353850/7270320785");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                mInterstitialAd.loadAd(new AdRequest.Builder().build());
            }

        });


        AdView adView = new AdView(this);
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId("ca-app-pub-4250344724353850/3076210317");       //test add
        AdView mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        AdView adView1 = new AdView(this);              //real add
        adView1.setAdSize(AdSize.BANNER);
        adView1.setAdUnitId("ca-app-pub-4250344724353850/6809433588");
        AdView mAdView1 = findViewById(R.id.adView1);
        AdRequest adRequest1 = new AdRequest.Builder().build();
        mAdView1.loadAd(adRequest1);

        AdView adView2 = new AdView(this);              //real add
        adView2.setAdSize(AdSize.BANNER);
        adView2.setAdUnitId("ca-app-pub-4250344724353850/5378654615");
        AdView mAdView2 = findViewById(R.id.adView2);
        AdRequest adRequest2 = new AdRequest.Builder().build();
        mAdView2.loadAd(adRequest2);


        stop = findViewById(R.id.btnstop);

        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        final Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(),uri);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    MainService.ringtone.stop();
                    MainService.ringtone.stop();
                    MainService.ringtone.stop();

                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                mInterstitialAd.setAdListener(new AdListener() {
                    @Override
                    public void onAdLoaded() {
                        // Code to be executed when an ad finishes loading.
                    }

                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        // Code to be executed when an ad request fails.
                    }

                    @Override
                    public void onAdOpened() {
                        // Code to be executed when the ad is displayed.
                    }

                    @Override
                    public void onAdClicked() {
                        // Code to be executed when the user clicks on an ad.
                    }

                    @Override
                    public void onAdLeftApplication() {
                        // Code to be executed when the user has left the app.
                    }

                    @Override
                    public void onAdClosed() {
                        // Code to be executed when the interstitial ad is closed.
                    }
                });


            }
        });

        boolean isEnabled = mPreferences.getBoolean(PREFERENCE_KEY_ENABLED, false);
        mEnabledSwitch = (Switch) findViewById(R.id.enabled_switch);
        mEnabledSwitch.setChecked(isEnabled);
        mEnabledSwitch.setOnCheckedChangeListener(this);

        Switch vibrateSwitch = (Switch) findViewById(R.id.vibrate_switch);
        vibrateSwitch.setChecked(mPreferences.getBoolean(PREFERENCE_KEY_VIBRATE, false));
        vibrateSwitch.setOnCheckedChangeListener(this);

        Switch soundSwitch = (Switch) findViewById(R.id.sound_switch);
        soundSwitch.setChecked(mPreferences.getBoolean(PREFERENCE_KEY_SOUND, false));
        soundSwitch.setOnCheckedChangeListener(this);

        MainService.startIfEnabled(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.feedback) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(String.format("mailto:%s?subject=%s", getString(R.string.email), getString(R.string.app_name))));
//            intent.setType("message/rfc822");
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(this, R.string.no_email, Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        int id = buttonView.getId();

        switch (id) {
            case R.id.enabled_switch:
                mPreferences.edit().putBoolean(PREFERENCE_KEY_ENABLED, isChecked).apply();
                MainService.startIfEnabled(this);
                break;
            case R.id.vibrate_switch:
                mPreferences.edit().putBoolean(PREFERENCE_KEY_VIBRATE, isChecked).apply();
                break;
            case R.id.sound_switch:
                mPreferences.edit().putBoolean(PREFERENCE_KEY_SOUND, isChecked).apply();
                break;
        }
    }
    public static boolean isPlugged(Context context) {
        boolean isPlugged= false;
        Intent intent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int plugged = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
        isPlugged = plugged == BatteryManager.BATTERY_PLUGGED_AC || plugged == BatteryManager.BATTERY_PLUGGED_USB;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN) {
            isPlugged = isPlugged || plugged == BatteryManager.BATTERY_PLUGGED_WIRELESS;
        }
        return isPlugged;
    }
}