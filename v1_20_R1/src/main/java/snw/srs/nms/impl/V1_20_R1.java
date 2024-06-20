package snw.srs.nms.impl;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.bukkit.craftbukkit.v1_20_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import snw.srs.nms.NMSUtilitiesAdapter;

public class V1_20_R1 implements NMSUtilitiesAdapter {

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
}
