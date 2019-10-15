package com.dds.webrtclib.utils;

import android.util.Log;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

/**
 * author xiayanlei
 * date 2019/10/9
 */
public class SurfaceViewRenderHelper {

    private static final String TAG = "SurfaceViewRenderHelper";

    public static void init(EglBase.Context sharedContext, SurfaceViewRenderer surfaceViewRenderer) {
        surfaceViewRenderer.init(sharedContext, new RendererCommon.RendererEvents() {
            @Override
            public void onFirstFrameRendered() {

            }

            @Override
            public void onFrameResolutionChanged(int videoWidth, int videoHeight, int rotation) {
                Log.i(TAG, "onFrameResolutionChanged: " + rotation);
                surfaceViewRenderer.setMirror(rotation == 270);
            }
        });
        surfaceViewRenderer.addFrameListener(frame -> {
            Log.i(TAG, "onFrame: ");
        }, 0);
    }
}
