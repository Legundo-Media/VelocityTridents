package me.JohnnyHT.velocityTridents;


import lombok.Getter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import java.util.Map;

public class TridentData implements ConfigurationSerializable {

    @Getter private final float volume;
    @Getter private final int customModelData;
    @Getter private final float velocity;
    @Getter private final float pitch;

    public TridentData(Map<String, Object> map) {
        volume = (float) ((double) map.getOrDefault("volume", 1.0));
        pitch = (float) ((double) map.getOrDefault("pitch", 1.0));
        customModelData = (int) map.get("customModelData");
        velocity = (float) ((double) map.getOrDefault("velocity", 2.0));

    }


    public boolean match(ItemStack trident) {
        ItemStack item = trident;
            if (item.getItemMeta().hasCustomModelData() && item.getItemMeta().getCustomModelData() == customModelData) {
                return true;
            }
        return false;
    }

    @Override
    public @NotNull Map<String, Object> serialize() {
        return Map.of();
    }
}
