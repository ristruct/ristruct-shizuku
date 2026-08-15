package com.sketchgdx.gamelib;

import com.badlogic.gdx.Game;

/**
 * Real entry point for a code-only game. Instead of one ApplicationAdapter
 * doing everything, libGDX's Game class lets you swap between Screens
 * (menu, level, game-over, ...), each its own class with its own
 * create/render/dispose lifecycle. This is what most real libGDX games use.
 *
 * Add your own screens next to MenuScreen / PlayScreen2D / PlayScreen3D and
 * switch between them with setScreen(new WhateverScreen(this)).
 */
public class GdxGame extends Game {

    /** Set to false once you've confirmed the pipeline works, so real
     *  players go straight to MenuScreen instead of the diagnostic screen. */
    public static boolean SHOW_SMOKE_TEST = true;

    @Override
    public void create() {
        if (SHOW_SMOKE_TEST) {
            setScreen(new SmokeTestScreen(this));
        } else {
            setScreen(new MenuScreen(this));
        }
    }
}
