/*
 * Copyright (c) 2017 ThanksMister LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package com.thanksmister.iot.mqtt.alarmpanel.ui.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.thanksmister.iot.mqtt.alarmpanel.BaseFragment;
import com.thanksmister.iot.mqtt.alarmpanel.R;

import butterknife.ButterKnife;
import timber.log.Timber;

public class AboutFragment extends BaseFragment {

    public static final String GOOGLE_PLAY_RATING = "com.thanksmister.iot.mqtt.alarmpanel";
    public static final String GITHUB_URL = "https://github.com/thanksmister/android-mqtt-alarm-panel";
    public static final String EMAIL_ADDRESS = "mister@thanksmister.com";
    
    private String versionNumber;

    public static AboutFragment newInstance() {
        return new AboutFragment();
    }

    public AboutFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_about, container, false);
    }

    @Override
    public void onViewCreated(View fragmentView, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(fragmentView, savedInstanceState);
        try {
            PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            TextView versionName = getActivity().findViewById(R.id.versionName);
            versionNumber = " v" + packageInfo.versionName;
            versionName.setText(versionNumber);
        } catch (PackageManager.NameNotFoundException e) {
            Timber.e(e.getMessage());
        }
    }
    
    @Override
    public void onDetach() {
        super.onDetach();
        ButterKnife.unbind(this);
    }
}