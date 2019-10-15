package com.dds.webrtclib;

import android.os.Handler;
import android.text.TextUtils;

import org.webrtc.Logging;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoFrame;
import org.webrtc.VideoSink;

/**
 * Created by dds on 2019/4/4.
 * android_shuai@163.com
 */
public class ProxyVideoSink implements VideoSink {
    private static final String TAG = "dds_ProxyVideoSink";
    private VideoSink target;
    private final static int CAMERA_OBSERVER_PERIOD_MS = 2000;
    private static final int UPPER_QUALITY_LIMIT = 12;
    private static final int LOWER_QUALITY_LIMIT = 2;
    private int frameCount;
    private int freezePeriodCount;
    private int flexPeriodCount;
    private Handler handler;
    private VideoListener videoListener;

    @PeerConnectionHelper.Quality
    private String quality;
    private boolean received;

    private final Runnable cameraObserver = new Runnable() {
        @Override
        public void run() {
            final int cameraFps = Math.round(frameCount * 1000.0f / CAMERA_OBSERVER_PERIOD_MS);
            Logging.d(TAG, "Camera fps: " + cameraFps + "---");
            if (frameCount == 0) {
                ++freezePeriodCount;
            } else {
                freezePeriodCount = 0;
                ++flexPeriodCount;
            }
            String tmpQ = quality;
            if (cameraFps < 15 && freezePeriodCount >= LOWER_QUALITY_LIMIT) {
                Logging.e(TAG, "Camera freezed");
                flexPeriodCount = 0;
                tmpQ = PeerConnectionHelper.HVGA;
            } else if (flexPeriodCount >= UPPER_QUALITY_LIMIT) {
                if (cameraFps >= 24) {
                    tmpQ = PeerConnectionHelper.HD;
                } else if (cameraFps >= 15) {
                    tmpQ = PeerConnectionHelper.VGA;
                }
                flexPeriodCount = 0;
            }
            if (!TextUtils.equals(tmpQ, quality) && videoListener != null && target != null) {
                quality = tmpQ;
                videoListener.onCameraQualityChanged(target, quality);
                Logging.e(TAG, "Camera quality changed " + quality);
            }
            frameCount = 0;
            handler.postDelayed(this, CAMERA_OBSERVER_PERIOD_MS);
        }
    };

    public ProxyVideoSink() {
        handler = new Handler();
        this.frameCount = 0;
        this.freezePeriodCount = 0;
        flexPeriodCount = 0;
        this.quality = PeerConnectionHelper.VGA;
    }

    @Override
    synchronized public void onFrame(VideoFrame frame) {
        if (target == null) {
            Logging.d(TAG, "Dropping frame in proxy because target is null.");
            return;
        }
        if (!received) {
            received = true;
            handler.postDelayed(cameraObserver, CAMERA_OBSERVER_PERIOD_MS);
        }
        ++frameCount;
        target.onFrame(frame);
    }

    synchronized public void setTarget(VideoSink target) {
        this.target = target;
        if (target == null) {
            handler.removeCallbacks(cameraObserver);
            videoListener = null;
        } else if (target instanceof SurfaceViewRenderer) {
            this.quality = (String) ((SurfaceViewRenderer) target).getTag();
        }
        Logging.d(TAG, "target quality: " + target + "." + quality);
    }

    public String getQuality() {
        return quality;
    }

    synchronized public void setVideoListener(VideoListener videoListener) {
        this.videoListener = videoListener;
    }

    public interface VideoListener {
        void onCameraQualityChanged(VideoSink target, @PeerConnectionHelper.Quality String quality);
    }

}