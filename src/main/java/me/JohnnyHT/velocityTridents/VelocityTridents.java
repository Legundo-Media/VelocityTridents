package me.JohnnyHT.velocityTridents;

import lombok.Getter;
import me.JohnnyHT.velocityTridents.Commands.ConfigReloadCommand;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
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

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class VelocityTridents extends JavaPlugin implements Listener {

    private final EntityDataAccessor<Byte> accessor = new EntityDataAccessor<Byte>(8, EntityDataSerializers.BYTE);
    private final HashMap<UUID, BukkitRunnable> playerTimers = new HashMap<>();

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this, this);
        plugin = this;
        saveDefaultConfig();

        ConfigurationSerialization.registerClass(TridentData.class);
        loadConfig(false);

        Objects.requireNonNull(getCommand("tridentreloadconfig")).setExecutor(new ConfigReloadCommand());

    }

    public void loadConfig(boolean reload) {
        if (reload) reloadConfig();
        soundCategory = SoundCategory.valueOf(getConfig().getString("soundCategory"));
        tridentData = (List<TridentData>) getConfig().getList("trident");
    }

    @Getter
    private static VelocityTridents plugin;
    @Getter
    private static SoundCategory soundCategory;
    @Getter
    private static List<TridentData> tridentData;


    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Trident trident) {
            if (trident.getShooter() instanceof Player player) {
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() != Material.TRIDENT) {
                    return;
                }
                var tridentConfig = tridentData.stream().filter(j -> j.match(item)).findFirst();
                if (tridentConfig.isPresent()) {
                    event.setCancelled(true);
                    if (player.isInWater()) {
                        Vector direction = player.getEyeLocation().getDirection();
                        player.setVelocity(direction.multiply(tridentConfig.get().getVelocity()));
                        ServerPlayer serverPlayer = ((CraftPlayer) player).getHandle();
                        SynchedEntityData entityData = serverPlayer.getEntityData();
                        int bitmask = entityData.get(accessor);
                        entityData.set(accessor, (byte) (bitmask | 0x04));
                        UUID playerUUID = player.getUniqueId();
                        if (playerTimers.containsKey(playerUUID)) {
                            playerTimers.get(playerUUID).cancel();
                        }
                        BukkitRunnable timerTask = new BukkitRunnable() {

                                @Override
                                public void run () {
                                SynchedEntityData entityData = serverPlayer.getEntityData();
                                int bitmask = entityData.get(accessor);
                                entityData.set(accessor, (byte) (bitmask & ~0x04));

                            }
                        };
                        timerTask.runTaskLater(this,20);
                        playerTimers.put(playerUUID, timerTask);

                        player.playSound(player.getLocation(), Sound.ITEM_TRIDENT_RIPTIDE_1, soundCategory, tridentConfig.get().getVolume(), tridentConfig.get().getPitch());
                    }
                }
            }
        }
    }
}