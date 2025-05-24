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

import java.util.ArrayList;
import java.util.List;

public class ObserverUpdater {
    private static ObserverUpdater observerUpdater;
    private final List<TrackerObserver> trackerObservers = new ArrayList<>();
    private final List<ConnectionObserver> connectionObservers = new ArrayList<>();

    public static ObserverUpdater getObserverUpdater() {
        if (observerUpdater == null)
            observerUpdater = new ObserverUpdater();
        return observerUpdater;
    }

    public void addTrackerObserver(TrackerObserver observer) {
        trackerObservers.add(observer);
    }

    public void removeTrackerObserver(TrackerObserver observer) {
        trackerObservers.remove(observer);
    }

    public void addConnectionObserver(ConnectionObserver observer) {
        connectionObservers.add(observer);
    }

    public void removeConnectionObserver(ConnectionObserver observer) {
        connectionObservers.remove(observer);
    }

    public void notifyTrackerObservers(int status, int spO2Value) {
        trackerObservers.forEach(observer -> observer.onTrackerDataChanged(status, spO2Value));
    }

    public void displayError(int errorResourceId) {
        trackerObservers.forEach(observer -> observer.onError(errorResourceId));
    }

    public void notifyConnectionObservers(int stringResourceId) {
        connectionObservers.forEach(observer -> observer.onConnectionResult(stringResourceId));
    }
}
