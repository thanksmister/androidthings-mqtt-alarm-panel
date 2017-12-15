/*
 * Copyright (c) 2017. ThanksMister LLC
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

package com.thanksmister.iot.mqtt.alarmpanel.viewmodel

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import com.thanksmister.iot.mqtt.alarmpanel.persistence.Message
import com.thanksmister.iot.mqtt.alarmpanel.persistence.MessageDao
import io.reactivex.Flowable
import javax.inject.Inject

class MainFragmentViewModel @Inject
constructor(application: Application, val dataSource: MessageDao) : AndroidViewModel(application) {
    /**
     * Get the messages.
     * @return a [Flowable] that will emit every time the messages have been updated.
     */
    fun getMessages(): Flowable<List<Message>> {
        return dataSource.getMessages()
    }
}