package snw.srs.nms;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import snw.srs.nms.impl.V1_20_R1;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings("unused")
public final class AdapterRetriever {

    public static final String MINECRAFT_VERSION;
    public static final NMSUtilitiesAdapter ADAPTER;

    private static final Pattern VERSION_PATTERN = Pattern.compile(".*\\(.*MC.\\s*([a-zA-z0-9\\-.]+).*");

    private AdapterRetriever() {
    }

    static {
        Server server = Bukkit.getServer();
        String version = server.getVersion();
        MINECRAFT_VERSION = extractVersion(version);
        // The switch statement must have at least 1 result
        // Don't forget to add the adapter NEW statements there if you supported more version
        // Your plugin may support multiple Minecraft version
        // noinspection SwitchStatementWithTooFewBranches
        ADAPTER = switch (MINECRAFT_VERSION) {
            case "1.20.1" -> new V1_20_R1();
            default -> throw new IllegalStateException("Unsupported Minecraft version: " + MINECRAFT_VERSION);
        };
    }

    private static String extractVersion(String text) {
        Matcher version = VERSION_PATTERN.matcher(text);

        if (version.matches() && version.group(1) != null) {
            return version.group(1);
        } else {
            throw new IllegalStateException("Cannot parse version String '%s'".formatted(text));
        }
    }
}
