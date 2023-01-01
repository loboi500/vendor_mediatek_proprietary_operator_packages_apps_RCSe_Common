/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 */
/* MediaTek Inc. (C) 2018. All rights reserved.
 *
 * BY OPENING THIS FILE, RECEIVER HEREBY UNEQUIVOCALLY ACKNOWLEDGES AND AGREES
 * THAT THE SOFTWARE/FIRMWARE AND ITS DOCUMENTATIONS ("MEDIATEK SOFTWARE")
 * RECEIVED FROM MEDIATEK AND/OR ITS REPRESENTATIVES ARE PROVIDED TO RECEIVER ON
 * AN "AS-IS" BASIS ONLY. MEDIATEK EXPRESSLY DISCLAIMS ANY AND ALL WARRANTIES,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NONINFRINGEMENT.
 * NEITHER DOES MEDIATEK PROVIDE ANY WARRANTY WHATSOEVER WITH RESPECT TO THE
 * SOFTWARE OF ANY THIRD PARTY WHICH MAY BE USED BY, INCORPORATED IN, OR
 * SUPPLIED WITH THE MEDIATEK SOFTWARE, AND RECEIVER AGREES TO LOOK ONLY TO SUCH
 * THIRD PARTY FOR ANY WARRANTY CLAIM RELATING THERETO. RECEIVER EXPRESSLY ACKNOWLEDGES
 * THAT IT IS RECEIVER'S SOLE RESPONSIBILITY TO OBTAIN FROM ANY THIRD PARTY ALL PROPER LICENSES
 * CONTAINED IN MEDIATEK SOFTWARE. MEDIATEK SHALL ALSO NOT BE RESPONSIBLE FOR ANY MEDIATEK
 * SOFTWARE RELEASES MADE TO RECEIVER'S SPECIFICATION OR TO CONFORM TO A PARTICULAR
 * STANDARD OR OPEN FORUM. RECEIVER'S SOLE AND EXCLUSIVE REMEDY AND MEDIATEK'S ENTIRE AND
 * CUMULATIVE LIABILITY WITH RESPECT TO THE MEDIATEK SOFTWARE RELEASED HEREUNDER WILL BE,
 * AT MEDIATEK'S OPTION, TO REVISE OR REPLACE THE MEDIATEK SOFTWARE AT ISSUE,
 * OR REFUND ANY SOFTWARE LICENSE FEES OR SERVICE CHARGE PAID BY RECEIVER TO
 * MEDIATEK FOR SUCH MEDIATEK SOFTWARE AT ISSUE.
 *
 * The following software/firmware and/or related documentation ("MediaTek Software")
 * have been modified by MediaTek Inc. All revisions are subject to any receiver's
 * applicable license agreements with MediaTek Inc.
 */

package com.mediatek.ims.rcsua.service;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telephony.ims.SipDelegateManager;

import com.mediatek.ims.rcsua.Configuration;
import com.mediatek.ims.rcsua.RegistrationInfo;
import com.mediatek.ims.rcsua.service.utils.Logger;
import com.mediatek.ims.rcsua.service.SipHandler;
import com.mediatek.ims.rcsua.service.SipHandler.Listener;

import java.io.IOException;
import java.lang.*;
import java.util.*;

public class RuaChannel {

    RuaChannel(RuaClient client, ISipEventCallback callback, int mode) {
        this.client = client;
        this.callback = callback;
        this.callbacks.register(callback);
        this.adapter = RuaAdapter.getInstance();
        this.mode = mode;
        this.listener = new SipListener(mode);
        this.adapter.getSipHandler().registerListener(listener);
        this.availabilityListener = new AvailabilityListener();
        adapter.registerForAvailabilityChange(availabilityListener);
        this.isAvailable = isAvailable();
        notifyAvailabilityChanged(isAvailable);
        this.isEnabled = true;
        this.isClosed = false;
        this.hdlrThread = new HandlerThread("CHNLCB-THREAD");
        hdlrThread.start();
        this.callbackHandler = new Handler(hdlrThread.getLooper());
    }

    int sendMessage(byte[] message) throws IOException {

        logger.debug("sendMessage:closed[" + isClosed + "],enabled[" + isEnabled + "]");
        if (isClosed)
            throw new IllegalStateException("Channel closed");

        if (!isEnabled)
            throw new IllegalStateException("Channel disabled");

        if (!isAvailable())
            throw new IOException("send SIP message fail");

        if (!adapter.sendSipMessage(message, listener.hashCode())) {
            throw new IOException("send SIP message fail");
        }

        return 0;
    }


    void sendMessageAsync(byte[] message, String transactionId) {

        logger.debug("sendSipMessageAsync >> "
                + "transactionId[" + transactionId + "],"
                + "closed[" + isClosed + "],enabled[" + isEnabled + "]");
        if (isClosed)
            callbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifySipMessageSendFailure(transactionId, SipDelegateManager.MESSAGE_FAILURE_REASON_DELEGATE_CLOSED);
                }
            });

        if (!isEnabled)
            callbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifySipMessageSendFailure(transactionId, SipDelegateManager.MESSAGE_FAILURE_REASON_TAG_NOT_ENABLED_FOR_DELEGATE);
                }
            });

        if (!isAvailable())
            callbackHandler.post(new Runnable() {
                @Override
                public void run() {
                    notifySipMessageSendFailure(transactionId, SipDelegateManager.MESSAGE_FAILURE_REASON_NOT_REGISTERED);
                }
            });

        adapter.sendSipMessage(message, transactionId, listener.hashCode());
    }



    boolean isAvailable() {
        logger.debug("isAvailable: isEnabled[" + isEnabled + "]");

        return isEnabled && adapter.isAvailable()
            && adapter.getRegistrationInfo().isRegistered();
    }

    Configuration readConfiguration() {
        if (!adapter.isAvailable())
            return new Configuration();
        RegistrationInfo info = adapter.getRegistrationInfo();

        return info.readImsConfiguration();
    }


    public synchronized void close() {
        if (isClosed)
            return;

        isClosed = true;
        adapter.getSipHandler().unregisterListener(listener);
        adapter.unregisterForAvailabilityChange(availabilityListener);
        callbacks.unregister(callback);
        client.channelClosed(this);
    }

    public synchronized void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        if (!isEnabled) {
            adapter.getSipHandler().unregisterListener(listener);
            adapter.unregisterForAvailabilityChange(availabilityListener);
        } else {
            adapter.getSipHandler().registerListener(listener);
            adapter.registerForAvailabilityChange(availabilityListener);
        }
    }

    public void notifyMessageReceived(int transport, byte[] message) {
        logger.debug("notifyMessageReceived with transport[" + transport + "]");
        if (callbacks != null) {
            synchronized (callbacks) {
                final int N = callbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        callbacks.getBroadcastItem(i).messageReceived(transport, message);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                callbacks.finishBroadcast();
            }
        }
    }

    public void notifySipMessageSent(String transactionId) {
        logger.debug("notifySipMessageSent >> transactionId[" + transactionId + "]");
        if (callbacks != null) {
            synchronized (callbacks) {
                final int N = callbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        callbacks.getBroadcastItem(i).messageSent(transactionId);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                callbacks.finishBroadcast();
            }
        }
    }

    public void notifySipMessageSendFailure(String transactionId, int reason) {
        logger.debug("notifySipMessageSendFailure >> transactionId[" + transactionId + "], reason[" + reason + "]");
        if (callbacks != null) {
            synchronized (callbacks) {
                final int N = callbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        callbacks.getBroadcastItem(i).messageSendFail(transactionId, reason);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                callbacks.finishBroadcast();
            }
        }
    }

    public SipChannelImpl getBinder() {
        return new SipChannelImpl();
    }

    private class SipListener extends SipHandler.Listener {
        SipListener(int mode) {
            this.standalonePresence = (mode != 0);
        }

        @Override
        public void messageReceived(int transport, byte[] sipMessage) {
            notifyMessageReceived(transport, sipMessage);
        }

        @Override
        public void messageSent(String transactionId) {
            notifySipMessageSent(transactionId);
        }

        @Override
        public void messageSendFailure(String transactionId, int reason) {
            notifySipMessageSendFailure(transactionId, reason);
        }

    }

    void notifyAvailabilityChanged(boolean isAvailable) {
        logger.debug("notifyAvailabilityChanged with isAvailable[" + isAvailable + "]");
        if (callbacks != null) {
            synchronized (callbacks) {
                final int N = callbacks.beginBroadcast();
                for (int i = 0; i < N; i++) {
                    try {
                        callbacks.getBroadcastItem(i).availabilityChanged(isAvailable);
                    } catch (RemoteException e) {
                        // The RemoteCallbackList will take care of removing
                        // the dead object for us.
                    }
                }
                callbacks.finishBroadcast();
            }
        }
    }

    private class AvailabilityListener implements RuaAdapter.AvailabilityListener {
        @Override
        public void availabilityChanged() {
            if (isAvailable != isAvailable()) {
                isAvailable = isAvailable();
                notifyAvailabilityChanged(isAvailable);
            }
        }
    }

    private class SipChannelImpl extends ISipChannel.Stub {

        @Override
        public int sendMessage(byte[] message, RcsUaException ex) throws RemoteException {
            try {
                return RuaChannel.this.sendMessage(message);
            } catch (Exception e) {
                ex.set(e);
            }
            return 0;
        }

        @Override
        public void sendMessageAsync(byte[] message, String transactionId) throws RemoteException {
            try {
                RuaChannel.this.sendMessageAsync(message, transactionId);
            } catch (Exception e) {
            }
        }

        @Override
        public void close(RcsUaException e) throws RemoteException {
            RuaChannel.this.close();
        }

        @Override
        public boolean isAvailable() throws RemoteException {
            return RuaChannel.this.isAvailable();
        }

        @Override
        public Configuration readConfiguration() throws RemoteException {
            return RuaChannel.this.readConfiguration();
        }
    }

    private final RemoteCallbackList<ISipEventCallback> callbacks = new RemoteCallbackList<ISipEventCallback>() {
        @Override
        public void onCallbackDied(ISipEventCallback callback) {

        }
    };

    private RuaAdapter adapter;
    private RuaClient client;
    private ISipEventCallback callback;
    private SipListener listener;
    private AvailabilityListener availabilityListener;
    private int mode;
    private boolean isClosed;
    private boolean isEnabled;
    private boolean isAvailable;
    private HandlerThread hdlrThread;
    private Handler callbackHandler;
    private Logger logger = Logger.getLogger(RuaChannel.class.getName());
}
