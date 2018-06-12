package me.alwx.HttpServer;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactMethod;

import java.io.IOException;

import android.util.Log;

public class HttpServerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    ReactApplicationContext reactContext;

    private static final String MODULE_NAME = "HttpServer";

    private static int port;
    private static String serviceName;
    private static Server server = null;

    private NsdManager.RegistrationListener regListener = new NsdManager.RegistrationListener() {
        @Override
        public void onRegistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(MODULE_NAME, "NDS onRegistrationFailed: " + errorCode);
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo serviceInfo, int errorCode) {
            Log.d(MODULE_NAME, "NDS onUnregistrationFailed: " + errorCode);
        }

        @Override
        public void onServiceRegistered(NsdServiceInfo serviceInfo) {
            Log.d(MODULE_NAME, "NDS onServiceRegistered: " + serviceInfo);
            serviceName = serviceInfo.getServiceName();
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo serviceInfo) {
            Log.d(MODULE_NAME, "NDS onServiceUnregistered: " + serviceInfo);
        }
    };

    public HttpServerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return MODULE_NAME;
    }

    @ReactMethod
    public void start(int port, String serviceName) {
        Log.d(MODULE_NAME, "Initializing server...");
        this.port = port;
        this.serviceName = serviceName;

        startServer();
    }

    @ReactMethod
    public void stop() {
        Log.d(MODULE_NAME, "Stopping server...");

        stopServer();
    }

    @ReactMethod
    public void respond(int code, String type, String body, String requestId) {
        if (server != null) {
            server.respond(code, type, body, requestId);
        }
    }

    @Override
    public void onHostResume() {
    }

    @Override
    public void onHostPause() {
    }

    @Override
    public void onHostDestroy() {
        stopServer();
    }

    private void startServer() {
        if (this.port == 0 || this.serviceName == null) {
            return;
        }

        if (server == null) {
            server = new Server(reactContext, port);
        }
        try {
            server.start();
            Log.d(MODULE_NAME, "RegisterNDS with " + port + " " + serviceName);
            registerService();
        } catch (IOException e) {
            Log.e(MODULE_NAME, e.getMessage());
        }
    }

    private void stopServer() {
        if (server != null) {
            server.stop();
            unregisterService();

            server = null;
            port = 0;
            serviceName = null;
        }
    }

    private void registerService() {
        NsdServiceInfo serviceInfo  = new NsdServiceInfo();
        serviceInfo.setServiceName(serviceName);
        serviceInfo.setServiceType("_http._tcp.");
        serviceInfo.setPort(port);

        NsdManager manager = (NsdManager) reactContext.getSystemService(Context.NSD_SERVICE);

        Log.d(MODULE_NAME, "Registering service \"" + serviceName + "\" (port " + port + ")");
        manager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, regListener);
    }

    private void unregisterService() {
        NsdManager manager = (NsdManager) reactContext.getSystemService(Context.NSD_SERVICE);
        manager.unregisterService(regListener);
    }
}
