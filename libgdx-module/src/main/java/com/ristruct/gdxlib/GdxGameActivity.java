package com.ristruct.gdxlib;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * Drop-in Activity for Sketchware Pro projects.
 *
 * Sketchware Pro's visual MainActivity cannot extend AndroidApplication
 * directly, so this class is meant to be registered as an EXTRA Activity
 * (Project > Manage AndroidManifest.xml, or the "Add Activity" feature in
 * recent Sketchware Pro builds) and launched from MainActivity's blocks
 * with an Intent, e.g.:
 *
 *   Intent i = new Intent(MainActivity.this, GdxGameActivity.class);
 *   i.putExtra("gdxListenerClass", "com.example.mygame.MyGameListener");
 *   startActivity(i);
 *
 * "MyGameListener" is any class YOU write inside your Sketchware Pro
 * project (as an extra .java source / custom block) that implements
 * com.badlogic.gdx.ApplicationListener. This keeps the local_libs
 * package generic and reusable across every game you build, instead of
 * baking one specific game into the compiled classes.dex.
 */
public class GdxGameActivity extends AndroidApplication {

    public static final String EXTRA_LISTENER_CLASS = "gdxListenerClass";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String listenerClassName = intent != null ? intent.getStringExtra(EXTRA_LISTENER_CLASS) : null;

        ApplicationListener listener;
        if (listenerClassName != null) {
            listener = instantiateListener(listenerClassName);
        } else {
            // Fallback demo listener so the local lib "just works" out of
            // the box even before you wire up your own game class.
            listener = new DefaultGdxListener();
        }

        AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
        config.useAccelerometer = false;
        config.useCompass = false;
        config.useGyroscope = false;

        initialize(listener, config);
    }

    private ApplicationListener instantiateListener(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            return (ApplicationListener) clazz.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException(
                "GdxGameActivity: could not instantiate ApplicationListener '" + className +
                "'. Make sure the class exists in your Sketchware Pro project, is public, " +
                "has a public no-arg constructor, and implements com.badlogic.gdx.ApplicationListener.",
                e
            );
        }
    }
}
