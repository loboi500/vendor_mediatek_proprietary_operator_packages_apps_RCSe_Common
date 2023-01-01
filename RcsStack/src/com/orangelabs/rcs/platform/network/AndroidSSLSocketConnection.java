/* Copyright Statement:
 *
 * This software/firmware and related documentation ("MediaTek Software") are
 * protected under relevant copyright laws. The information contained herein
 * is confidential and proprietary to MediaTek Inc. and/or its licensors.
 * Without the prior written permission of MediaTek inc. and/or its licensors,
 * any reproduction, modification, use or disclosure of MediaTek Software,
 * and information contained herein, in whole or in part, shall be strictly prohibited.
 *
 * MediaTek Inc. (C) 2012. All rights reserved.
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

package com.orangelabs.rcs.platform.network;

import android.net.TrafficStats;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import javax.net.ssl.SSLSocket;

import com.orangelabs.rcs.utils.logger.Logger;

public class AndroidSSLSocketConnection implements SocketConnection {
    /**
     * SSLSocket connection
     */
    private SSLSocket mSocket = null;
    private MsrpSSLSocketFactory mFactory = new MsrpSSLSocketFactory();
    private static Logger sLogger = Logger.getLogger(AndroidSSLSocketConnection.class.getName());

    /**
     * Constructor
     */
    public AndroidSSLSocketConnection() {
    }

    public void open1(String remoteAddr, int remotePort, String localhost, int port) throws IOException {
        final int oldTag = TrafficStats.getAndSetThreadStatsTag(TrafficStats.TAG_SYSTEM_APP);
        new Socket(remoteAddr, remotePort);
        TrafficStats.setThreadStatsTag(oldTag);
    }
    /**
     * Constructor
     *
     * @param socket Socket
     */
    public AndroidSSLSocketConnection(SSLSocket socket) {
        mSocket = socket;
    }

    /**
     * Open the socket
     *
     * @param remoteAddr Remote address
     * @param remotePort Remote port
     * @throws IOException
     */
    public void open(String remoteAddr, int remotePort) throws IOException {
        if (sLogger.isActivated()) {
            sLogger.debug("open(), remoteAddr = " + remoteAddr + ", remotePort = " + remotePort);
        }
        mSocket = (SSLSocket) mFactory.createSocket(remoteAddr, remotePort);
        if (sLogger.isActivated()) {
            sLogger.debug("open() exit");
        }
    }

    /**
     * Close the socket
     *
     * @throws IOException
     */
    public void close() throws IOException {
        if (mSocket != null) {
            mSocket.close();
            mSocket = null;
        }
    }

    /**
     * Returns the socket input stream
     *
     * @return Input stream
     * @throws IOException
     */
    public InputStream getInputStream() throws IOException {
        if (mSocket != null) {
            return mSocket.getInputStream();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Returns the socket output stream
     *
     * @return Output stream
     * @throws IOException
     */
    public OutputStream getOutputStream() throws IOException {
        if (mSocket != null) {
            return mSocket.getOutputStream();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Returns the remote address of the connection
     *
     * @return Address
     * @throws IOException
     */
    public String getRemoteAddress() throws IOException {
        if (mSocket != null) {
            return mSocket.getInetAddress().getHostAddress();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Returns the remote port of the connection
     *
     * @return Port
     * @throws IOException
     */
    public int getRemotePort() throws IOException {
        if (mSocket != null) {
            return mSocket.getPort();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Returns the local address of the connection
     *
     * @return Address
     * @throws IOException
     */
    public String getLocalAddress() throws IOException {
        if (mSocket != null) {
            return mSocket.getLocalAddress().getHostAddress();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Returns the local port of the connection
     *
     * @return Port
     * @throws IOException
     */
    public int getLocalPort() throws IOException {
        if (mSocket != null) {
            return mSocket.getLocalPort();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Get the timeout for this socket during which a reading operation shall
     * block while waiting for data
     *
     * @return Timeout in milliseconds
     * @throws IOException
     */
    public int getSoTimeout() throws IOException {
        if (mSocket != null) {
            return mSocket.getSoTimeout();
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Set the timeout for this socket during which a reading operation shall
     * block while waiting for data
     *
     * @param timeout Timeout in milliseconds
     * @throws IOException
     */
    public void setSoTimeout(int timeout) throws IOException {
        if (mSocket != null) {
            mSocket.setSoTimeout(timeout);
        } else {
            throw new IOException("Connection not openned");
        }
    }

    /**
     * Sets the {@link SocketOptions#SO_SNDBUF SO_SNDBUF} option to the
     * specified value for this {@code Socket}.
     * The {@link SocketOptions#SO_SNDBUF SO_SNDBUF} option is used by the
     * platform's networking code as a hint for the size to set the underlying
     * network I/O buffers.
     *
     * <p>Because {@link SocketOptions#SO_SNDBUF SO_SNDBUF} is a hint,
     * applications that want to verify what size the buffers were set to
     * should call {@link #getSendBufferSize()}.
     *
     * @exception SocketException if there is an error
     * in the underlying protocol, such as a TCP error.
     *
     * @param size the size to which to set the send buffer
     * size. This value must be greater than 0.
     *
     * @exception IllegalArgumentException if the
     * value is 0 or is negative.
     *
     * @see #getSendBufferSize()
     * @since 1.2
     */
    public void setSendBufferSize(int size) throws IOException {
        if (mSocket != null) {
            mSocket.setSendBufferSize(size);
        } else {
            throw new IOException("Connection not openned");
        }
    }

  /**
     * Sets the {@link SocketOptions#SO_RCVBUF SO_RCVBUF} option to the
     * specified value for this {@code Socket}. The
     * {@link SocketOptions#SO_RCVBUF SO_RCVBUF} option is
     * used by the platform's networking code as a hint for the size to set
     * the underlying network I/O buffers.
     *
     * <p>Increasing the receive buffer size can increase the performance of
     * network I/O for high-volume connection, while decreasing it can
     * help reduce the backlog of incoming data.
     *
     * <p>Because {@link SocketOptions#SO_RCVBUF SO_RCVBUF} is a hint,
     * applications that want to verify what size the buffers were set to
     * should call {@link #getReceiveBufferSize()}.
     *
     * <p>The value of {@link SocketOptions#SO_RCVBUF SO_RCVBUF} is also used
     * to set the TCP receive window that is advertized to the remote peer.
     * Generally, the window size can be modified at any time when a socket is
     * connected. However, if a receive window larger than 64K is required then
     * this must be requested <B>before</B> the socket is connected to the
     * remote peer. There are two cases to be aware of:
     * <ol>
     * <li>For sockets accepted from a ServerSocket, this must be done by calling
     * {@link ServerSocket#setReceiveBufferSize(int)} before the ServerSocket
     * is bound to a local address.<p></li>
     * <li>For client sockets, setReceiveBufferSize() must be called before
     * connecting the socket to its remote peer.</li></ol>
     * @param size the size to which to set the receive buffer
     * size. This value must be greater than 0.
     *
     * @exception IllegalArgumentException if the value is 0 or is
     * negative.
     *
     * @exception SocketException if there is an error
     * in the underlying protocol, such as a TCP error.
     *
     * @see #getReceiveBufferSize()
     * @see ServerSocket#setReceiveBufferSize(int)
     * @since 1.2
     */
    public void setReceiveBufferSize(int size) throws IOException {
        if (mSocket != null) {
            mSocket.setReceiveBufferSize(size);
        } else {
            throw new IOException("Connection not openned");
        }
    }

}
