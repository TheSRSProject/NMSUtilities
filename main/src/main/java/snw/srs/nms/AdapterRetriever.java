package snw.srs.nms;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import snw.srs.nms.impl.V1_20_R1;

@SuppressWarnings("unused")
public final class AdapterRetriever {
    private AdapterRetriever() {
    }

    public static final String SERVER_VERSION;
    public static final NMSUtilitiesAdapter ADAPTER;

    static {
        final Server craftServer = Bukkit.getServer();
        final Class<? extends Server> serverClass = craftServer.getClass();
        final String packageName = serverClass.getPackageName();
        final int lastIndexOf = packageName.lastIndexOf(".");
        SERVER_VERSION = packageName.substring(lastIndexOf + 1);

        // we may add support of other Minecraft version in the future?
        // noinspection SwitchStatementWithTooFewBranches
        switch (SERVER_VERSION) {
            case "v1_20_R1" -> {
                ADAPTER = new V1_20_R1();
            }
            default -> throw new IllegalArgumentException("Unsupported server version " + SERVER_VERSION);
        }
    }

    // Note for API users: You should catch ExceptionInInitializerError
    public static void init() {
    }
}
