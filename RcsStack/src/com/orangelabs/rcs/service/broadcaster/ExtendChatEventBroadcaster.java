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

import com.orangelabs.rcs.platform.AndroidFactory;
import com.orangelabs.rcs.utils.IntentUtils;
import com.orangelabs.rcs.utils.logger.Logger;
import com.gsma.services.rcs.chat.ChatLog.Message.Content.ReasonCode;
import com.gsma.services.rcs.chat.ChatLog.Message.Content.Status;
import com.gsma.services.rcs.chat.IExtendChatListener;
import com.gsma.services.rcs.chat.OneToOneChatIntent;
import com.gsma.services.rcs.contact.ContactId;

import android.content.Intent;
import android.os.RemoteCallbackList;
import android.os.RemoteException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * OneToOneChatEventBroadcaster maintains the registering and unregistering of
 * IOneToOneChatListeners and also performs broadcast events on these listeners upon the trigger of
 * corresponding callbacks.
 */
public class ExtendChatEventBroadcaster implements IExtendChatEventBroadcaster {

    private final RemoteCallbackList<IExtendChatListener> mExtendChatListeners = new RemoteCallbackList<>();

    private final Logger logger = Logger.getLogger(getClass().getName());

    public ExtendChatEventBroadcaster() {
    }

    public void addOneToOneChatEventListener(IExtendChatListener listener) {
        mExtendChatListeners.register(listener);
    }

    public void removeOneToOneChatEventListener(IExtendChatListener listener) {
        mExtendChatListeners.unregister(listener);
    }

    @Override
    public void broadcastMessageStatusChanged(String chatId,ContactId contact, String mimeType, String msgId,
            Status status, ReasonCode reasonCode) {
        final int N = mExtendChatListeners.beginBroadcast();
        if (logger.isActivated()) {
            logger.info("broadcastMessageStatusChanged contact: " + contact.toString() + ", msgId: " + msgId + ", status: " + status.toInt() + ", count: " + N);
        }
        int rcsStatus = status.toInt();
        int rcsReasonCode = reasonCode.toInt();
        for (int i = 0; i < N; i++) {
            try {
                mExtendChatListeners.getBroadcastItem(i).onMessageStatusChanged(chatId,contact,
                        mimeType, msgId, rcsStatus, rcsReasonCode);
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener.", e);
                }
            }
        }
        mExtendChatListeners.finishBroadcast();
    }

    @Override
    public void broadcastComposingEvent(ContactId contact, boolean status) {
        final int N = mExtendChatListeners.beginBroadcast();
        if (logger.isActivated()) {
            logger.info("broadcastComposingEvent contact: " + contact.toString() + ", status: " + status + ", count: " + N);
        }
        for (int i = 0; i < N; i++) {
            try {
                mExtendChatListeners.getBroadcastItem(i).onComposingEvent(contact, status);
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener", e);
                }
            }
        }
        mExtendChatListeners.finishBroadcast();
    }

    @Override
    public void broadcastMessageReceived(String mimeType, String msgId) {
        Intent newOneToOneMessage = new Intent(
                OneToOneChatIntent.ACTION_NEW_ONE_TO_ONE_STANDALONE_CHAT_MESSAGE);
        if (logger.isActivated()) {
            logger.info("broadcastMessageReceived: " + mimeType + ", msgId: " + msgId);
        }
        newOneToOneMessage.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
        IntentUtils.tryToSetReceiverForegroundFlag(newOneToOneMessage);
        newOneToOneMessage.putExtra(OneToOneChatIntent.EXTRA_MIME_TYPE, mimeType);
        newOneToOneMessage.putExtra(OneToOneChatIntent.EXTRA_MESSAGE_ID, msgId);
        AndroidFactory.getApplicationContext().sendBroadcast(newOneToOneMessage, "com.gsma.services.permission.RCS");
    }

    public void broadcastExtendMessageReceived(String mimeType, String msgId, String displayName) {
        Intent newOneToOneMessage = new Intent(
                OneToOneChatIntent.ACTION_NEW_ONE_TO_ONE_STANDALONE_CHAT_MESSAGE);
        if (logger.isActivated()) {
            logger.info("broadcastExtendMessageReceived: " + mimeType + ", msgId: " + msgId + ",displayName: " + displayName);
        }
        newOneToOneMessage.addFlags(Intent.FLAG_EXCLUDE_STOPPED_PACKAGES);
        IntentUtils.tryToSetReceiverForegroundFlag(newOneToOneMessage);
        newOneToOneMessage.putExtra(OneToOneChatIntent.EXTRA_MIME_TYPE, mimeType);
        newOneToOneMessage.putExtra(OneToOneChatIntent.EXTRA_MESSAGE_ID, msgId);
        newOneToOneMessage.putExtra(OneToOneChatIntent.EXTRA_DISPLAY_NAME, displayName);
        AndroidFactory.getApplicationContext().sendBroadcast(newOneToOneMessage, "com.gsma.services.permission.RCS");
    }

    @Override
    public void broadcastMessagesDeleted(ContactId contact, Set<String> msgIds) {
        List<String> ids = new ArrayList<>(msgIds);
        final int N = mExtendChatListeners.beginBroadcast();
        if (logger.isActivated()) {
            logger.info("broadcastMessagesDeleted contact: " + contact.toString() + ", msgIds: " + msgIds.size());
        }
        for (int i = 0; i < N; i++) {
            try {
                mExtendChatListeners.getBroadcastItem(i).onMessagesDeleted(contact, ids);
            } catch (RemoteException e) {
                if (logger.isActivated()) {
                    logger.error("Can't notify listener.", e);
                }
            }
        }
        mExtendChatListeners.finishBroadcast();
    }
}
