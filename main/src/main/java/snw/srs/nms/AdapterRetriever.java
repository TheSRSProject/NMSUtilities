package snw.srs.nms;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
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
        // TODO [DO NOT REMOVE] Check for every commit we made for supporting a certain MC version
        // For forks:
        // Don't forget to add the MC version -> CB package mapping there
        //  if you will support more version
        final Map<String, String> supportedVersions = Map.of(
                "1.21.4", "V1_21_R3",
                "1.20.1", "V1_20_R1",
                "1.16.5", "V1_16_R3"
        );
        final String versionWeAreOn = supportedVersions.get(MINECRAFT_VERSION);
        if (versionWeAreOn == null) {
            throw new IllegalStateException("Unknown Minecraft version: " + MINECRAFT_VERSION);
        }
        final String packageName = AdapterRetriever.class.getPackageName();
        final String adapterClassName = packageName + ".impl." + versionWeAreOn;
        try {
            ADAPTER = (NMSUtilitiesAdapter) Class.forName(adapterClassName).getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new IllegalStateException("Unsupported environment", e);
        }
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
