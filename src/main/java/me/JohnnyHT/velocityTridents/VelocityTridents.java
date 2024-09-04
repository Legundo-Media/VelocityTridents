package me.JohnnyHT.velocityTridents;

import io.netty.channel.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Pose;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class VelocityTridents extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void removePlayer(Player player){
        Channel channel = ((CraftPlayer) player).getHandle().connection.connection.channel;
        channel.eventLoop().submit(()->{
            channel.pipeline().remove(player.getName() + "Trident");
            return null;
        });
    }

    private final Set<Integer> pleyers = ConcurrentHashMap.newKeySet();


    private void tridentPacketListener(Player player){
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler(){

            @Override
            public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
                if(!(msg instanceof ClientboundSetEntityDataPacket packet)) {super.write(ctx, msg, promise); return;}
                if(!pleyers.contains(packet.id())) {super.write(ctx, msg, promise); return;}
                List<SynchedEntityData.DataValue<?>> values = packet.packedItems();
                byte bitMask = 0x05;
                boolean adjusted = true;
                for (int i = 0; i < values.size(); i++) {
                    SynchedEntityData.DataValue<?> value = values.get(i);
                    if (value.id() != 8) {continue;}
                    bitMask = (byte) value.value();
                    bitMask |= 0x05;
                    values.remove(i);
                    i--;
                    adjusted = true;
                    break;
                }
                if(adjusted) {values.add(new SynchedEntityData.DataValue<>(8, EntityDataSerializers.BYTE, bitMask));}
                super.write(ctx, msg, promise);
            }
        };

        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().connection.connection.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getName() + "Trident", channelDuplexHandler);
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e){
        tridentPacketListener(e.getPlayer());
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent e){
        removePlayer(e.getPlayer());
    }


    private void addSpin(Player player){
        pleyers.add(((CraftPlayer)player).getHandle().getId());
        ServerPlayer user = ((CraftPlayer)player).getHandle();
        user.refreshEntityData(user);
    }

    private void removeSpin(Player player){
        pleyers.remove(((CraftPlayer)player).getHandle().getId());
        ServerPlayer user = ((CraftPlayer)player).getHandle();
        user.refreshEntityData(user);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident) {
            Bukkit.broadcastMessage("Yeet 1");

            Trident trident = (Trident) event.getEntity();
            if (trident.getShooter() instanceof Player) {
                Bukkit.broadcastMessage("Yeet 2");

                Player player = (Player) trident.getShooter();
                // Check if the player is in water
                Bukkit.broadcastMessage("Yeet 3");
                event.setCancelled(true);
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getItemMeta().getCustomModelData() == 1) {
                    event.setCancelled(true);
                    if (item != null && item.getType() == Material.TRIDENT) {
                        if (player.isInWater()) {
                            Bukkit.broadcastMessage("Yeet 4");
                            if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()) {

                                event.setCancelled(true);
                                Bukkit.broadcastMessage("Yeet 5");

                                Vector direction = player.getEyeLocation().getDirection().multiply(2);
                                player.setVelocity(direction);

                                addSpin(player);

                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        removeSpin(player);
                                    }
                                }.runTaskLater(this,20);
                                player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.0f);
                            }
                        }
                    }
                }
            }
        }

    }
}
