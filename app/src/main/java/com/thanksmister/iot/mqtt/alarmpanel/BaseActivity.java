/*
 * <!--
 *   ~ Copyright (c) 2017. ThanksMister LLC
 *   ~
 *   ~ Licensed under the Apache License, Version 2.0 (the "License");
 *   ~ you may not use this file except in compliance with the License. 
 *   ~ You may obtain a copy of the License at
 *   ~
 *   ~ http://www.apache.org/licenses/LICENSE-2.0
 *   ~
 *   ~ Unless required by applicable law or agreed to in writing, software distributed 
 *   ~ under the License is distributed on an "AS IS" BASIS, 
 *   ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   ~ See the License for the specific language governing permissions and 
 *   ~ limitations under the License.
 *   -->
 */

package com.thanksmister.iot.mqtt.alarmpanel;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ExtendedForecastView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView;

import butterknife.ButterKnife;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

abstract public class BaseActivity extends AppCompatActivity {
    
    private StoreManager storeManager;
    private Configuration configuration;
    private AlertDialog alertDialog;
    private AlertDialog dialog;
    private AlertDialog disableDialog;
    private AlertDialog screenSaverDialog;
    private Handler inactivityHandler = new Handler();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void onDestroy() {

        super.onDestroy();

        ButterKnife.unbind(this);

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            dialog = null;
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        if(inactivityHandler != null) {
            inactivityHandler.removeCallbacks(inactivityCallback);
            inactivityHandler = null;
        }
    }

    private Runnable inactivityCallback = new Runnable() {
        @Override
        public void run() {
            hideDialog();
            showScreenSaver();
        }
    };

    public void resetInactivityTimer() {
        closeScreenSaver();
        inactivityHandler.removeCallbacks(inactivityCallback);
        inactivityHandler.postDelayed(inactivityCallback, getConfiguration().getInactivityTime());
    }

    public void stopDisconnectTimer(){
        hideDialog();
        inactivityHandler.removeCallbacks(inactivityCallback);
    }

    @Override
    public void onUserInteraction(){
        resetInactivityTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }
    
    public StoreManager getStoreManager() {
        if (storeManager == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            storeManager = new StoreManager(getApplicationContext(), getContentResolver(), baseApplication.getAppSharedPreferences());
        }
        return storeManager;
    }
    
    public Configuration getConfiguration() {
        if (configuration == null) {
            BaseApplication baseApplication = BaseApplication.getInstance();
            configuration = new Configuration(getApplicationContext(), baseApplication.getAppSharedPreferences());
        }
        return configuration;
    }
    
    public void hideDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
        if (disableDialog != null) {
            disableDialog.dismiss();
            disableDialog = null;
        }
    }
    
    public void showAlertDialog(String message, DialogInterface.OnClickListener onClickListener) {
        alertDialog = new AlertDialog.Builder(this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, onClickListener)
                .show();
    }

    public void showAlertDialog(String message) {
        alertDialog = new AlertDialog.Builder(BaseActivity.this)
                .setMessage(Html.fromHtml(message))
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return item.getItemId() == android.R.id.home;
    }

    public void showArmOptionsDialog(ArmOptionsView.ViewListener armListener) {
        hideDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_options, null, false);
        Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        int density= getResources().getDisplayMetrics().densityDpi;
        if(density == DisplayMetrics.DENSITY_TV ) {
            view.setMinimumWidth((int) (displayRectangle.width() * 0.6f));
            view.setMinimumHeight((int) (displayRectangle.height() * 0.7f));
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.setMinimumWidth((int) (displayRectangle.width() * 0.5f));
            view.setMinimumHeight((int) (displayRectangle.height() * 0.6f));
        } else {
            view.setMinimumWidth((int)(displayRectangle.width() * 0.7f));
            view.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        }
        final ArmOptionsView optionsView = view.findViewById(R.id.armOptionsView);
        optionsView.setListener(armListener);
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }
    
    /**
     * Shows the disable alarm dialog with countdown. It is important that this 
     * dialog only be shown once and not relaunched when already displayed as
     * it resets the timer.
     */
    public void showAlarmDisableDialog(AlarmDisableView.ViewListener alarmCodeListener,
                                       int code, boolean beep, int timeRemaining) {
        if(disableDialog != null && disableDialog.isShowing()) {
            return;
        }
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_disable, null, false);
        final AlarmDisableView disableView = view.findViewById(R.id.alarmDisableView);
        disableView.setListener(alarmCodeListener);
        disableView.setCode(code);
        disableView.startCountDown(timeRemaining);
        if(beep) {
            disableView.playContinuousBeep();
        }
        disableDialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
        disableDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                disableView.destroySoundUtils();
            }
        });
    }
    
   /* public void showAlarmDisableDialog(AlarmDisableView.ViewListener alarmCodeListener, int code, boolean beep) {
        hideDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_alarm_disable, null, false);
        final AlarmDisableView alarmCodeView = view.findViewById(R.id.alarmDisableView);
        alarmCodeView.setListener(alarmCodeListener);
        alarmCodeView.setCode(code);
        alarmCodeView.startCountDown(configuration.getPendingTime());
        if(beep) {
            alarmCodeView.playContinuousBeep();
        }
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }*/

    public void showExtendedForecastDialog(Daily daily) {
        hideDialog();
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_extended_forecast, null, false);
        Rect displayRectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        view.setMinimumWidth((int)(displayRectangle.width() * 0.7f));

        int density= getResources().getDisplayMetrics().densityDpi;
        if(density == DisplayMetrics.DENSITY_TV ) {
        } else if (density == DisplayMetrics.DENSITY_MEDIUM) {
            view.setMinimumHeight((int) (displayRectangle.height() * 0.6f));
        } else {
            view.setMinimumHeight((int)(displayRectangle.height() * 0.8f));
        }
        final ExtendedForecastView  extendedForecastView = view.findViewById(R.id.extendedForecastView);
        extendedForecastView.setExtendedForecast(daily, getConfiguration().getWeatherUnits());
        dialog = new AlertDialog.Builder(BaseActivity.this)
                .setCancelable(true)
                .setView(view)
                .show();
    }

    public void closeScreenSaver() {
        if(screenSaverDialog != null) {
            screenSaverDialog.dismiss();
            screenSaverDialog = null;
        }
    }
    public void showScreenSaver() {
        
        if(getConfiguration().getAlarmMode().equals(PREF_TRIGGERED)
                || getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)) {
            return;
        } else if (screenSaverDialog != null && screenSaverDialog.isShowing()) {
            return;
        }
        
        inactivityHandler.removeCallbacks(inactivityCallback);
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.dialog_screen_saver, null, false);
        final ScreenSaverView screenSaverView = view.findViewById(R.id.screenSaverView);
        screenSaverView.setScreenSaver(BaseActivity.this, getConfiguration().showPhotoScreenSaver(),
                getConfiguration().getImageSource(), getConfiguration().getImageFitScreen(),
                getConfiguration().getImageRotation());
        screenSaverView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(screenSaverDialog != null) {
                    screenSaverDialog.dismiss();
                    screenSaverDialog = null;
                    resetInactivityTimer();
                }
            }
        });
        screenSaverDialog = new AlertDialog.Builder(BaseActivity.this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
                .setCancelable(true)
                .setView(view)
                .show();
    }

    public void showSettingsCodeDialog(final int code, final SettingsCodeView.ViewListener listener) {
        if(getConfiguration().isFirstTime()) {
            Intent intent = SettingsActivity.createStartIntent(BaseActivity.this);
            startActivity(intent);
        } else {
            hideDialog();
            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.dialog_settings_code, null, false);
            final SettingsCodeView settingsCodeView = view.findViewById(R.id.settingsCodeView);
            settingsCodeView.setCode(code);
            settingsCodeView.setListener(listener);
            dialog = new AlertDialog.Builder(BaseActivity.this)
                    .setCancelable(true)
                    .setView(view)
                    .show();
        }
    }
}