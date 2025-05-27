package me.q13x.originchecker.platform.paper;

import com.github.retrooper.packetevents.PacketEvents;
import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import me.q13x.originchecker.commons.InjectLegacyHandler;
import me.q13x.originchecker.commons.ModernHandshakeHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.mcbrawls.inject.spigot.InjectSpigot;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public final class PaperOriginCheckerPlugin extends JavaPlugin {
    PaperConfiguration config;
    InjectLegacyHandler legacy;

    Logger logger = getLogger();

    @EventHandler
    public void onLoad() {
        PacketEvents.getAPI().load();
        config = new PaperConfiguration(this);
        reload();
        legacy = new InjectLegacyHandler(config);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, cmds -> {
            var builder = Commands.literal("originchecker")
                    .then(
                            Commands.literal("reload")
                                    .requires(s -> s.getSender().hasPermission("originchecker.reload"))
                                    .executes(ctx -> {
                                        var exec = ctx.getSource().getSender();
                                        reload();
                                        exec.sendMessage(Component.text("Successfully reloaded OriginChecker's configuration!").color(TextColor.color(0, 255, 0)));
                                        return Command.SINGLE_SUCCESS;
                                    })
                    );
            cmds.registrar().register(builder.build());
        });
        logger.info(Component.text("Loaded OriginChecker.").color(TextColor.color(0, 255, 0)).content());
    }

    public void reload() {
        config.reloadConfig();
        logger.info(config.toString());
        if (config.getAllowedHosts().length == 0) {
            logger.warning("** NO ALLOWED HOSTS SET!");
            logger.warning("Consider adding a hostname to allowed_hosts in this plugin's config.yml file,");
            logger.warning("otherwise no clients will be able to connect to the server!");
        }
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        InjectSpigot.INSTANCE.registerInjector(legacy);
        ModernHandshakeHandler.hookModern(config, logger);
        logger.info("Enabled OriginChecker!");
    }

    @Override
    public void onDisable() {
        PacketEvents.getAPI().terminate();
        logger.info("Goodbye!");
    }
}
