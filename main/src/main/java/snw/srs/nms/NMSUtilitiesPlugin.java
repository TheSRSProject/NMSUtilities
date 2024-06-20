package snw.srs.nms;

import com.google.common.base.Preconditions;
import org.bukkit.plugin.java.JavaPlugin;

public final class NMSUtilitiesPlugin extends JavaPlugin {
    private static NMSUtilitiesPlugin instance;

    public static NMSUtilitiesPlugin getInstance() {
        Preconditions.checkNotNull(instance, "Plugin not enabled yet");
        return instance;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        instance = this;

        AdapterRetriever.init();
        getLogger().info("Initialized NMS adapter");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        instance = null;
    }
}
