package snw.srs.nms.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_20_R1.profile.CraftPlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.profile.PlayerProfile;
import snw.srs.nms.NMSUtilitiesAdapter;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("deprecation")
public class V1_20_R1 implements NMSUtilitiesAdapter {
    private static final Cache<UUID, PlayerProfile> SKIN_CACHE;

    static {
        SKIN_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();
    }

    private ServerPlayer toNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private void sendPacket(Player player, Packet<ClientGamePacketListener> packet) {
        ServerPlayer handle = toNMSPlayer(player);
        ServerGamePacketListenerImpl connection = handle.connection;
        if (connection != null) {
            connection.send(packet);
        }
    }

    @Override
    public void showCreditScreen(Player player) {
        ClientboundGameEventPacket packet;
        packet = new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 1);
        sendPacket(player, packet);
    }

    @Override
    public CompletableFuture<ItemStack> getPlayerHead(Player player, Plugin requester) {
        CompletableFuture<ItemStack> future = new CompletableFuture<>();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        UUID uuid = player.getUniqueId();
        PlayerProfile profile;
        PlayerProfile profileFromCache = SKIN_CACHE.getIfPresent(uuid);
        profile = Objects.requireNonNullElseGet(profileFromCache, () -> getPlayerProfile(player));
        if (profile.getTextures().isEmpty()) {
            profile.update().thenAcceptAsync((updated) -> {
                completeSkull(itemStack, updated, future, uuid, false);
            }, runnable -> requester.getServer().getScheduler().runTask(requester, runnable));
        } else {
            completeSkull(itemStack, profile, future, uuid, true);
        }
        return future;
    }

    private void completeSkull(
            ItemStack itemStack,
            PlayerProfile profile,
            CompletableFuture<ItemStack> future,
            UUID ownerUUID,
            boolean upToDate
    ) {
        if (!upToDate) {
            SKIN_CACHE.put(ownerUUID, profile);
        }
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        meta.setOwnerProfile(profile);
        itemStack.setItemMeta(meta);
        future.complete(itemStack);
    }

    // We'll have to use this because we're compiled on top of Paper API.
    // Paper returns their version of PlayerProfile which could break our code
    // on server implementations that not implement Paper API.
    private PlayerProfile getPlayerProfile(Player player) {
        return new CraftPlayerProfile(((CraftPlayer) player).getProfile()); // CraftBukkit version, not Paper
    }
}
