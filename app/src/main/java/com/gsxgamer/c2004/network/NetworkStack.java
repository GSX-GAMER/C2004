package com.gsxgamer.c2004.network;

import android.content.Context;
import android.os.Build;
import java.security.Security;
import okhttp3.OkHttpClient;
import org.conscrypt.Conscrypt;

public final class NetworkStack {
    private NetworkStack(){}
    public static void installTls(){
        if(Build.VERSION.SDK_INT < 21){
            try { Security.insertProviderAt(Conscrypt.newProvider(),1); } catch(Throwable ignored) {}
        }
    }
    public static OkHttpClient client(){
        return new OkHttpClient.Builder().connectTimeout(20,java.util.concurrent.TimeUnit.SECONDS).readTimeout(60,java.util.concurrent.TimeUnit.SECONDS).writeTimeout(60,java.util.concurrent.TimeUnit.SECONDS).build();
    }
}
