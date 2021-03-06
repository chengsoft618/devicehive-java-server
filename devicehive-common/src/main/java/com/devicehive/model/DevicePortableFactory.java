package com.devicehive.model;

/*
 * #%L
 * DeviceHive Common Module
 * %%
 * Copyright (C) 2016 - 2017 DataArt
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.devicehive.auth.HivePrincipal;
import com.devicehive.model.eventbus.Filter;
import com.devicehive.model.eventbus.Subscriber;
import com.devicehive.model.eventbus.Subscription;
import com.hazelcast.nio.serialization.Portable;
import com.hazelcast.nio.serialization.PortableFactory;

public class DevicePortableFactory implements PortableFactory {
    @Override
    public Portable create(int classId) {
        if (DeviceNotification.CLASS_ID == classId) {
            return new DeviceNotification();
        } else if (DeviceCommand.CLASS_ID == classId) {
            return new DeviceCommand();
        } else if (HivePrincipal.CLASS_ID == classId) {
            return new HivePrincipal();
        } else if (Filter.CLASS_ID == classId) {
            return new Filter();
        } else if (Subscription.CLASS_ID == classId) {
            return new Subscription();
        } else if (Subscriber.CLASS_ID == classId) {
            return new Subscriber();
        }
        
        return null;
    }
}
