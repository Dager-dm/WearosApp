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

//import static com.samsung.health.spo2tracking.Status.MEASUREMENT_COMPLETED;
import static com.example.maternapp.data.spo2.Status.MEASUREMENT_COMPLETED;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.samsung.android.service.health.tracking.HealthTracker;
import com.samsung.android.service.health.tracking.HealthTrackingService;
import com.samsung.android.service.health.tracking.data.DataPoint;
import com.samsung.android.service.health.tracking.data.HealthTrackerType;
import com.samsung.android.service.health.tracking.data.ValueKey;
import com.example.maternapp.R;
import java.util.List;

public class SpO2Listener {
    private final static String TAG = "SpO2 Listener";
    private final Handler spo2Handler = new Handler(Looper.myLooper());
    private final HealthTracker.TrackerEventListener spo2Listener = new HealthTracker.TrackerEventListener() {
        @Override
        public void onDataReceived(@NonNull List<DataPoint> list) {
            for (DataPoint data : list) {
                updateSpo2(data);
            }
        }

        @Override
        public void onFlushCompleted() {
            Log.i(TAG, "Flush completed");
        }

        @Override
        public void onError(HealthTracker.TrackerError trackerError) {
            Log.i(TAG, "SpO2 Tracker error: " + trackerError.toString());
            if (trackerError == HealthTracker.TrackerError.PERMISSION_ERROR) {
               ObserverUpdater.getObserverUpdater().displayError(R.string.NoPermission);
            }
            if (trackerError == HealthTracker.TrackerError.SDK_POLICY_ERROR) {
               ObserverUpdater.getObserverUpdater().displayError(R.string.SDKPolicyError);
            }
        }
    };
    private boolean isHandlerRunning = false;
    private HealthTracker spo2Tracker;

    void init(HealthTrackingService healthTrackingService) {
        spo2Tracker = healthTrackingService.getHealthTracker(HealthTrackerType.SPO2_ON_DEMAND);
    }

    public void startTracker() {
        if (!isHandlerRunning) {
            spo2Handler.post(() -> spo2Tracker.setEventListener(spo2Listener));
            isHandlerRunning = true;
        }
    }

    public void stopTracker() {
        if (spo2Tracker != null)
            spo2Tracker.unsetEventListener();
        spo2Handler.removeCallbacksAndMessages(null);
        isHandlerRunning = false;
    }

    private void updateSpo2(DataPoint data) {
        final int status = data.getValue(ValueKey.SpO2Set.STATUS);
        int spo2Value = 0;
        if (status == MEASUREMENT_COMPLETED)
            spo2Value = data.getValue(ValueKey.SpO2Set.SPO2);
        ObserverUpdater.getObserverUpdater().notifyTrackerObservers(status, spo2Value);
    }
}
