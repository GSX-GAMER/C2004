package com.gsxgamer.c2004;

import android.app.Application;
import android.os.Build;
import java.security.Security;
import org.conscrypt.Conscrypt;

public class C2004Application extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        // API 16-19 commonly has TLS 1.2 support but does not enable it by default.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            Security.insertProviderAt(Conscrypt.newProvider(), 1);
        }
    }
}
