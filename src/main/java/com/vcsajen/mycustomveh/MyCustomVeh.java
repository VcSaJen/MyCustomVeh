package com.vcsajen.mycustomveh;

import com.google.inject.Inject;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.slf4j.Logger;

/**
 * Main plugin class
 * Created by VcSaJen on 02.09.2016 18:44.
 */
@Plugin(id = "mycustomveh", name = "MyCustomVeh", version = "1.0", authors = {"VcSaJen"},
        description = "Sponge plugin for making custom dynamic vehicles", dependencies = {})
public class MyCustomVeh {
    @Inject
    private Logger logger;

    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        logger.debug("*************************");
        logger.debug("HI! MY PLUGIN IS WORKING!");
        logger.debug("*************************");
    }

}
