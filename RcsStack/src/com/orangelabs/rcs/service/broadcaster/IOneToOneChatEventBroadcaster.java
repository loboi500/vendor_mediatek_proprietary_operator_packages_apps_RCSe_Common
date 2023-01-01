/*
 * Copyright (C) 2014 Sony Mobile Communications Inc.
 * Copyright (C) 2010-2016 Orange.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.orangelabs.rcs.service.broadcaster;

import com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode;
import com.gsma.services.rcs.chat.ChatLog.Message.Content.Status;
import com.gsma.services.rcs.contact.ContactId;

import java.util.Set;

/**
 * Interface to perform broadcast events on ChatListeners
 */
public interface IOneToOneChatEventBroadcaster {

    void broadcastMessageStatusChanged(ContactId contact, String mimeType, String msgId,
            Status status, ReasonCode reasonCode);

    void broadcastComposingEvent(ContactId contact, boolean status);

    void broadcastMessageReceived(String mimeType, String msgId);

    void broadcastMessagesDeleted(ContactId contact, Set<String> msgIds);

}
