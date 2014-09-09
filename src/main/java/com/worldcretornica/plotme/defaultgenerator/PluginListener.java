package com.worldcretornica.plotme.defaultgenerator;

import me.flungo.bukkit.plotme.abstractgenerator.AbstractGenerator;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginEnableEvent;

/**
 *
 * @author Fabrizio Lungo <fab@lungo.co.uk>
 */
public class PluginListener implements Listener {

    private final DefaultGenerator plugin;

    public PluginListener(DefaultGenerator plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPluginEnable(final PluginEnableEvent event) {
        if (event.getPlugin().getName().equals(AbstractGenerator.CORE_PLUGIN_NAME)) {
            plugin.importOldConfigs();
        }
    }
}
