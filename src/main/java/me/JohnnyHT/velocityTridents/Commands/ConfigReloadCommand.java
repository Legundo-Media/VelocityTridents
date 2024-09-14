package me.JohnnyHT.velocityTridents.Commands;

import me.JohnnyHT.velocityTridents.VelocityTridents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ConfigReloadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        try {
            VelocityTridents.getPlugin().loadConfig(true);
            commandSender.sendMessage(Component.text("Reloaded the Trident config!", NamedTextColor.GREEN));
        }
        catch (Exception ignore) {
            commandSender.sendMessage(Component.text("BAD CONFIG", NamedTextColor.RED));
        }
        return true;
    }
}
