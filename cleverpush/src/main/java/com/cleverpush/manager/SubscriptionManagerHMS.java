package com.cleverpush.manager;

import static com.cleverpush.Constants.LOG_TAG;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.cleverpush.CleverPushPreferences;
import com.cleverpush.listener.SubscribedListener;
import com.huawei.agconnect.config.AGConnectServicesConfig;
import com.huawei.hms.aaid.HmsInstanceId;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.push.HmsMessaging;

import org.json.JSONObject;

public class SubscriptionManagerHMS extends SubscriptionManagerBase {

    private static final int TOKEN_TIMEOUT_MS = 30_000;
    private static final String THREAD_NAME = "HMS_GET_TOKEN";
    private final String appId;
    private static boolean callbackSuccessful;
    private SubscribedListener subscribedListener;

    public SubscriptionManagerHMS(Context context) {
        super(context, SubscriptionManagerType.HMS);

        appId = AGConnectServicesConfig.fromContext(context).getString("client/app_id");
    }

    @Override
    public void subscribe(JSONObject channelConfig, SubscribedListener subscribedListener) {
        this.subscribedListener = subscribedListener;

        new Thread(() -> {
            try {
                getHMSTokenTask(context, subscribedListener);
            } catch (ApiException e) {
                Log.e(LOG_TAG, "HMS ApiException getting Huawei token", e);
                this.tokenCallback(null, subscribedListener);
            }
        }, THREAD_NAME).start();
    }

    public void tokenCallback(String token) {
        this.tokenCallback(token, this.subscribedListener);
    }

    private void tokenCallback(String token, SubscribedListener subscribedListener) {
        if (subscribedListener == null) {
            return;
        }

        callbackSuccessful = true;

        if (token == null) {
            subscribedListener.subscribed(null);
            return;
        }

        this.syncSubscription(token, subscribedListener, appId);
    }

    @Override
    public void checkChangedPushToken(JSONObject channelConfig, String changedToken) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this.context);
        String existingToken = sharedPreferences.getString(CleverPushPreferences.HMS_TOKEN, null);

        if (existingToken == null) {
            return;
        }

        new Thread(() -> {
            if (appId == null) {
                Log.e(LOG_TAG, "SubscriptionManager: Getting HMS App ID failed");
                return;
            }

            try {
                String newToken = changedToken != null ? changedToken : getToken();
                if (newToken != null && !newToken.equals(existingToken)) {
                    this.syncSubscription(newToken, subscriptionId -> Log.i(LOG_TAG, "Synchronized new HMS token: " + newToken));
                } else {
                    Log.d(LOG_TAG, "HMS token has not changed: " + newToken);
                }
            } catch (Throwable throwable) {
                Log.e(LOG_TAG, "Unknown error getting HMS Token", throwable);
            }
        }, THREAD_NAME).start();
    }

    private String getToken() throws ApiException {
        HmsInstanceId hmsInstanceId = HmsInstanceId.getInstance(context);
        return hmsInstanceId.getToken(appId, HmsMessaging.DEFAULT_TOKEN_SCOPE);
    }

    private synchronized void getHMSTokenTask(@NonNull Context context, SubscribedListener subscribedListener) throws ApiException {
        Log.d(LOG_TAG, "Registering device with HMS App ID: " + appId);
        String pushToken = getToken();

        if (!TextUtils.isEmpty(pushToken)) {
            Log.d(LOG_TAG, "Device registered (HMS), push token: " + pushToken);
            this.tokenCallback(pushToken, subscribedListener);
        } else {
            // Token is always null on Huawei EMUI <= 9. We need to wait for the event.
            waitForOnNewPushTokenEvent(subscribedListener);
        }
    }

    private void waitForOnNewPushTokenEvent(SubscribedListener subscribedListener) {
        try {
            Thread.sleep(TOKEN_TIMEOUT_MS);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "SubscriptionManagerHMS Caught InterruptedException", e);
        }

        if (!callbackSuccessful) {
            Log.e(LOG_TAG, "SubscriptionManagerHMS onNewToken timeout");
            this.tokenCallback(null, subscribedListener);
        }
    }
}
