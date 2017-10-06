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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.data.stores.StoreManager;
import com.thanksmister.iot.mqtt.alarmpanel.network.model.Daily;
import com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration;
import com.thanksmister.iot.mqtt.alarmpanel.ui.activities.SettingsActivity;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.AlarmDisableView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ArmOptionsView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.ScreenSaverView;
import com.thanksmister.iot.mqtt.alarmpanel.ui.views.SettingsCodeView;
import com.thanksmister.iot.mqtt.alarmpanel.utils.DialogUtils;

import butterknife.ButterKnife;

import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED;
import static com.thanksmister.iot.mqtt.alarmpanel.ui.Configuration.PREF_TRIGGERED_PENDING;

abstract public class BaseActivity extends AppCompatActivity {
    
    private StoreManager storeManager;
    private Configuration configuration;
    private AlertDialog alertDialog;
    private Dialog dialog;
    private Dialog disableDialog;
    private Dialog screenSaverDialog;
    private Dialog progressDialog;
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
        if (disableDialog != null && disableDialog.isShowing()) {
            disableDialog.dismiss();
            disableDialog = null;
        }
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
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

    public void hideProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void showProgressDialog(String message, boolean modal) {
        if (progressDialog != null) {
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_progress, null, false);
        TextView progressDialogMessage = (TextView) dialogView.findViewById(R.id.progressDialogMessage);
        progressDialogMessage.setText(message);

        progressDialog = new AlertDialog.Builder(this)
                .setCancelable(modal)
                .setView(dialogView)
                .show();
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
        dialog = DialogUtils.showArmOptionsDialog(BaseActivity.this, armListener);
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
        disableDialog = DialogUtils.showAlarmDisableDialog(BaseActivity.this, alarmCodeListener, code, beep, timeRemaining);
    }
    
    public void showExtendedForecastDialog(Daily daily) {
        hideDialog();
        dialog = DialogUtils.showExtendedForecastDialog(BaseActivity.this, daily, getConfiguration().getWeatherUnits());
    }
    
    public void closeScreenSaver() {
        if(screenSaverDialog != null) {
            screenSaverDialog.dismiss();
            screenSaverDialog = null;
        }
    }
    
    /**
     * Show the screen saver only if the alarm isn't triggered. This shouldn't be an issue
     * with the alarm disabled because the disable time will be longer than this. 
     */
    public void showScreenSaver() {
        if(getConfiguration().getAlarmMode().equals(PREF_TRIGGERED)
                || getConfiguration().getAlarmMode().equals(PREF_TRIGGERED_PENDING)) {
            return;
        }
        if (screenSaverDialog != null && screenSaverDialog.isShowing()) {
            return;
        }
        inactivityHandler.removeCallbacks(inactivityCallback);
        screenSaverDialog = DialogUtils.showScreenSaver(BaseActivity.this, getConfiguration().showPhotoScreenSaver(),
                getConfiguration().getImageSource(), getConfiguration().getImageFitScreen(),
                getConfiguration().getImageRotation(), new ScreenSaverView.ViewListener() {
                    @Override
                    public void onMotion() {
                        if (screenSaverDialog != null) {
                            screenSaverDialog.dismiss();
                            screenSaverDialog = null;
                            resetInactivityTimer();
                        }
                    }
                }, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (screenSaverDialog != null) {
                            screenSaverDialog.dismiss();
                            screenSaverDialog = null;
                            resetInactivityTimer();
                        }
                    }
                });
    }

    public void showSettingsCodeDialog(final int code, final SettingsCodeView.ViewListener listener) {
        hideDialog();
        if(getConfiguration().isFirstTime()) {
            Intent intent = SettingsActivity.createStartIntent(BaseActivity.this);
            startActivity(intent);
        } else {
            dialog = DialogUtils.showSettingsCodeDialog(BaseActivity.this, code, listener);
        }
    }
}