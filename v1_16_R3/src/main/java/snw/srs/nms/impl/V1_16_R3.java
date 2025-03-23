package snw.srs.nms.impl;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_16_R3.*;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.Plugin;
import snw.srs.nms.NMSUtilitiesAdapter;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public final class V1_16_R3 implements NMSUtilitiesAdapter {
    private static final Cache<UUID, GameProfile> SKIN_CACHE;
    // CraftMetaSkull.setProfile(GameProfile)
    private static final MethodHandle SET_PROFILE;

    static {
        SKIN_CACHE = CacheBuilder.newBuilder()
                .expireAfterWrite(1, TimeUnit.MINUTES)
                .build();

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        try {
            Class<?> craftMetaSkullClazz = Class.forName("org.bukkit.craftbukkit.v1_16_R3.inventory.CraftMetaSkull");
            final Method setProfileMethod = craftMetaSkullClazz.getDeclaredMethod("setProfile", GameProfile.class);
            setProfileMethod.setAccessible(true);
            SET_PROFILE = lookup.unreflect(setProfileMethod);
        } catch (IllegalAccessException | NoSuchMethodException | ClassNotFoundException e) {
            throw new UnsupportedOperationException("Unsupported environment", e);
        }
    }

    private EntityPlayer toNMSPlayer(Player player) {
        return ((CraftPlayer) player).getHandle();
    }

    private void sendPacket(Player player, Packet<PacketListenerPlayOut> packet) {
        EntityPlayer handle = toNMSPlayer(player);
        PlayerConnection connection = handle.playerConnection;
        if (connection != null) {
            connection.sendPacket(packet);
        }
    }

    @Override
    public void showCreditScreen(Player player) {
        PacketPlayOutGameStateChange packet;
        packet = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.e, 1);
        sendPacket(player, packet);
    }

    @Override
    public CompletableFuture<ItemStack> getPlayerHead(Player player, Plugin requester) {
        CompletableFuture<ItemStack> future = new CompletableFuture<>();
        ItemStack itemStack = new ItemStack(Material.PLAYER_HEAD);
        EntityPlayer handle = toNMSPlayer(player);
        GameProfile profileHandle;
        GameProfile profileFromCache = SKIN_CACHE.getIfPresent(player.getUniqueId());
        if (profileFromCache != null) {
            profileHandle = profileFromCache;
        } else {
            profileHandle = handle.getProfile();
        }
        if (profileHandle.getProperties().isEmpty()) {
            TileEntitySkull.b(profileHandle, updated -> {
                completeSkull(itemStack, updated, future, false);
                return true; // IDK why Mojang use Predicate there
            }, false);
        } else {
            completeSkull(itemStack, profileHandle, future, true);
        }
        return future;
    }

    private void completeSkull(
            ItemStack itemStack,
            GameProfile profile,
            CompletableFuture<ItemStack> future,
            boolean upToDate
    ) {
        if (!upToDate) {
            SKIN_CACHE.put(profile.getId(), profile);
        }
        SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
        try {
            SET_PROFILE.invoke(meta, profile);
        } catch (Throwable ignored) {
        }
        itemStack.setItemMeta(meta);
        future.complete(itemStack);
    }
}
