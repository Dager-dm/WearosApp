/*
 * Copyright 2022 Samsung Electronics Co., Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.maternapp.data.spo2;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.samsung.android.service.health.tracking.ConnectionListener;
import com.samsung.android.service.health.tracking.HealthTrackerCapability;
import com.samsung.android.service.health.tracking.HealthTrackerException;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;
import com.example.maternapp.R;

import java.util.List;

public class ConnectionManager {
    private final static String TAG = "Connection Manager";
    Activity callingActivity = null;
    private HealthTrackingService healthTrackingService = null;
    private final ConnectionListener connectionListener = new ConnectionListener() {
        @Override
        public void onConnectionSuccess() {
            Log.i(TAG, "Connected");
            ObserverUpdater.getObserverUpdater().notifyConnectionObservers(R.string.ConnectedToHS);
            if (!isSpO2Available(healthTrackingService)) {
                Log.i(TAG, "Device does not support Blood Oxygen Level tracking");
                ObserverUpdater.getObserverUpdater().notifyConnectionObservers(R.string.NoSPo2Support);
            }
        }

        @Override
        public void onConnectionEnded() {
            Log.i(TAG, "Disconnected");
        }

        @Override
        public void onConnectionFailed(HealthTrackerException e) {
            processTrackerException(e);
        }
    };

    public void processTrackerException(HealthTrackerException e) {
        @SuppressWarnings("UnusedAssignment") boolean hasResolution = false;
        hasResolution = e.hasResolution();
        if (hasResolution)
            e.resolve(callingActivity);
        if (e.getErrorCode() == HealthTrackerException.OLD_PLATFORM_VERSION || e.getErrorCode() == HealthTrackerException.PACKAGE_NOT_INSTALLED)
            ObserverUpdater.getObserverUpdater().notifyConnectionObservers(R.string.NoValidHealthPlatform);
        else
            ObserverUpdater.getObserverUpdater().notifyConnectionObservers(R.string.ConnectionError);
        Log.e(TAG, "Could not connect to Health Tracking Service: " + e.getMessage());
    }

    public void connect(Activity activity, Context context) {
        callingActivity = activity;
        healthTrackingService = new HealthTrackingService(connectionListener, context);
        healthTrackingService.connectService();
    }

    public void disconnect() {
        if (healthTrackingService != null)
            healthTrackingService.disconnectService();
    }

    public void initSpO2(SpO2Listener spO2Listener) {
        spO2Listener.init(healthTrackingService);
    }

    public boolean isSpO2Available(HealthTrackingService healthTrackingService) {
        if (healthTrackingService == null)
            return false;
        @SuppressWarnings("UnusedAssignment") List<HealthTrackerType> availableTrackers = null;
        availableTrackers = checkAvailableTrackers(healthTrackingService.getTrackingCapability());
        if (availableTrackers == null)
            return false;
        else
            return availableTrackers.contains(HealthTrackerType.SPO2_ON_DEMAND);
    }

    public List<HealthTrackerType> checkAvailableTrackers(HealthTrackerCapability healthTrackerCapability) {
        if (healthTrackerCapability == null)
            return null;
        return healthTrackerCapability.getSupportHealthTrackerTypes();
    }
}
