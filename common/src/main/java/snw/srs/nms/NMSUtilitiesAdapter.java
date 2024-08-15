package snw.srs.nms;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.CompletableFuture;

public interface NMSUtilitiesAdapter {
    void showCreditScreen(Player player);

    CompletableFuture<ItemStack> getPlayerHead(Player player, Plugin requester);
}
