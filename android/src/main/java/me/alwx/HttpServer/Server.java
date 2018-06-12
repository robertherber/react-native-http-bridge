package me.alwx.HttpServer;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response;
import fi.iki.elonen.NanoHTTPD.Response.Status;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.UUID;

import android.support.annotation.Nullable;
import android.util.Log;

public class Server extends NanoHTTPD {
    private static final String TAG = "HttpServer";
    private static final String SERVER_EVENT_ID = "httpServerResponseReceived";

    private Map<String, ReadableMap> response;
    private Map<String, Response> responses = new HashMap<>();
    private ReactContext reactContext;

    public Server(ReactContext context, int port) {
        super(port);
        reactContext = context;
        response = new HashMap<>();

        Log.d(TAG, "Server started");
    }

    @Override
    public Response serve(IHTTPSession session) {
        Log.d(TAG, "Request received!");

        Response requestResponse = null;

        String requestId = UUID.randomUUID().toString();

        WritableMap request;
        try {
            request = fillRequestMap(session, requestId);
        } catch (Exception e) {
            return newFixedLengthResponse(
                    Response.Status.INTERNAL_ERROR, MIME_PLAINTEXT, e.getMessage()
            );
        }

        this.sendEvent(reactContext, SERVER_EVENT_ID, request);

        requestResponse = responses.get(requestId);
        while (requestResponse == null) {
            try {
                Thread.sleep(20);
                requestResponse = responses.get(requestId);
            } catch (Exception e) {
                Log.d(TAG, "Exception while waiting: " + e);
            }
        }

        responses.remove(requestId);

        return requestResponse;
    }

    public void respond(int code, String type, String body, String requestId) {
        Response response = newFixedLengthResponse(Status.lookup(code), type, body);
        responses.put(requestId, response);
    }

    private WritableMap fillRequestMap(IHTTPSession session, String requestId) throws Exception {
        Method method = session.getMethod();
        WritableMap request = Arguments.createMap();
        request.putString("url", session.getUri());
        request.putString("requestId", requestId);
        request.putString("method", method.name());
        String query = session.getQueryParameterString();
        request.putString("query", query);

        Map<String, String> files = new HashMap<>();
        if (Method.POST.equals(method)) {
            session.parseBody(files);
            if (files.size() > 0) {
                request.putString("postData", files.get("postData"));
            }
        }
        return request;
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, params);
    }
}
