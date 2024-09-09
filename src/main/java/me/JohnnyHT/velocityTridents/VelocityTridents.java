package me.JohnnyHT.velocityTridents;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public final class VelocityTridents extends JavaPlugin implements Listener {

    private final EntityDataAccessor<Byte> accessor = new EntityDataAccessor<Byte>(8, EntityDataSerializers.BYTE);

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident trident) {
            if (trident.getShooter() instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.TRIDENT) {return;}
                if (item.hasItemMeta() && item.getItemMeta().hasCustomModelData()
                        && item.getItemMeta().getCustomModelData() == 1) {
                    event.setCancelled(true);
                    if (player.isInWater()) {
                        Vector direction = player.getEyeLocation().getDirection().multiply(3);
                        player.setVelocity(direction);
                        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                        SynchedEntityData entityData = serverPlayer.getEntityData();
                        int bitmask = entityData.get(accessor);
                        entityData.set(accessor, (byte) (bitmask | 0x04));
                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                SynchedEntityData entityData = serverPlayer.getEntityData();
                                int bitmask = entityData.get(accessor);
                                entityData.set(accessor, (byte) (bitmask & ~0x04));
                            }
                        }.runTaskLater(this,20);
                        player.getWorld().playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, 1.0f, 1.0f);
                    }
                }
            }
        }
    }
}