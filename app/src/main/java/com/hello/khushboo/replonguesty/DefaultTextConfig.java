package com.hello.khushboo.replonguesty;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import static android.content.Context.WINDOW_SERVICE;

public class DefaultTextConfig {




    public void adjustFontScale(Configuration configuration, Context mContext)
    {
        configuration.fontScale = (float) 1; //0.85 small size, 1 normal size, 1,15 big etc
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager)(mContext).getSystemService(WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        metrics.scaledDensity = configuration.fontScale * metrics.density;
        configuration.densityDpi = (int) mContext.getResources().getDisplayMetrics().xdpi;
        ((Activity)mContext).getBaseContext().getResources().updateConfiguration(configuration, metrics);
    }
}
