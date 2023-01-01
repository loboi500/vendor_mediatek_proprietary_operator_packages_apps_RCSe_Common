package com.mediatek.ims.rcsua;

import android.util.Log;

/**
 * Callback indicate ACS status changed
 */
public abstract class AcsEventCallback extends AppCallback {

    /**
     * Used to notify Configuration status changed.
     *
     * @param valid Configuration is valid or not.
     * @param version Configuration version, only positive value is meaningful.
     */
    public void onConfigurationStatusChanged(boolean valid, int version) {}
  
    /* * Used to notify Configuration error received
     *
     * @param errorCode HTTP error received during connection setup
     * @param errorString reason phrase received with the error
     *
     */
    public void onConfigurationErrorReceived(int errorCode, String errorString) {}

    /**
    /**
     * Indicate ACS connection exist. Used for UI indicator.
     */
    public void onAcsConnected() {}

    /**
     * Indicate ACS connection not exist. Used fo UI indicator.
     */
    public void onAcsDisconnected() {}

    class Runner extends BaseRunner<Object> {

        Runner(Object... params) {
            super(params);
        }

        @Override
        void exec(Object... params) {
            int type = (params != null && params[0] instanceof Integer) ? (int)params[0] : -1;
            Log.d("AcsEventCallback",  " type[" + type + "]");

            switch (type) {
                case 0:
                    boolean valid = (int)params[1] == 1;
                    int version = (int)params[2];
                    Log.d("AcsEventCallback",  " valid[" + valid + "],version[" + version + "]");
                    onConfigurationStatusChanged(valid, version);
                    break;

                case 1:
                    onAcsConnected();
                    break;

                case 2:
                    onAcsDisconnected();
                    break;

                case 3:
                    int errorCode = (int)params[1];
                    String errorString = (String)params[2];
                    Log.d("AcsEventCallback",  " errorCode[" + errorCode + "],errorString[" + errorString + "]");
                    onConfigurationErrorReceived(errorCode, errorString);
                    break;

                default:
                    /* Nothing to do */
                    break;
            }
        }
    }

}

