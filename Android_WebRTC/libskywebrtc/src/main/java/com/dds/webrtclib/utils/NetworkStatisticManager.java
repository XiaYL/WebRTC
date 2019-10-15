package com.dds.webrtclib.utils;

import android.net.TrafficStats;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

/**
 * author xiayanlei
 * date 2019/10/12
 */
public class NetworkStatisticManager {

    private static final String TAG = "NetworkStatisticManager";
    private boolean isListening;
    private long uidR;
    private long uidT;
    private long totalR;//手机下行速率
    private long totalT;//手机上行速率
    private Handler mainHandler;
    private Timer statsTimer;
    private OnNetWorkStatusListener statusListener;

    private NetworkStatisticManager() {
        mainHandler = new Handler(Looper.getMainLooper());
        statsTimer = new Timer();
        statsTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                analysis();
            }
        }, 0, 1000);
    }

    public static NetworkStatisticManager getInstance() {
        return SingletonHolder.instance;
    }

    public NetworkStatisticManager register(OnNetWorkStatusListener statusListener) {
        this.statusListener = statusListener;
        return this;
    }

    public void start() {
        totalR = TrafficStats.getTotalRxBytes();
        totalT = TrafficStats.getTotalTxBytes();
        uidR = TrafficStats.getUidRxBytes(getCallingUid());
        uidT = TrafficStats.getUidTxBytes(getCallingUid());
        isListening = true;
    }

    public void stop() {
        isListening = false;
        statusListener = null;
    }

    private void analysis() {
        if (!isListening) {
            return;
        }
        long tmpUidR = TrafficStats.getUidRxBytes(getCallingUid());
        long tmpUidT = TrafficStats.getUidTxBytes(getCallingUid());
        long tmpTotalR = TrafficStats.getTotalRxBytes();
        long tmpTotalT = TrafficStats.getTotalTxBytes();
        float speedR = calculateSpeed(tmpTotalR - totalR);
        float speedT = calculateSpeed(tmpTotalT - totalT);
        float speedUidR = calculateSpeed(tmpUidR - uidR);
        float speedUidT = calculateSpeed(tmpUidT - uidT);
        Log.i(TAG, "analysis: " + Arrays.asList(new Object[]{speedR, speedT, speedUidR, speedUidT}));
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (statusListener != null) {
                    statusListener.onSpeedInfo(speedR, speedT);
                }
            }
        });
        uidR = tmpUidR;
        uidT = tmpUidT;
        totalR = tmpTotalR;
        totalT = tmpTotalT;
    }

    private static int getCallingUid() {
        return android.os.Process.myUid();
    }

    private float calculateSpeed(long delta) {
        return delta * 1.0f / 1024;
    }

    private static class SingletonHolder {
        private static final NetworkStatisticManager instance = new NetworkStatisticManager();
    }

    public interface OnNetWorkStatusListener {

        void onSpeedInfo(float speedR, float speedT);
    }
}
