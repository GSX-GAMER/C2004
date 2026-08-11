package com.gsxgamer.c2004;

import android.app.Application;
import com.gsxgamer.c2004.network.NetworkStack;

public class C2004Application extends Application {
    @Override public void onCreate(){super.onCreate();NetworkStack.installTls();}
}
