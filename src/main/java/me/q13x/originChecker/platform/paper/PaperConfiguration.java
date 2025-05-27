package me.q13x.originchecker.platform.paper;

import me.q13x.originchecker.commons.AbstractConfiguration;
import me.q13x.originchecker.commons.ResponseType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;

public class PaperConfiguration extends AbstractConfiguration {

    private final JavaPlugin plugin;
    private String[] allowedHosts;
    private ResponseType responseType;
    private String responseMessage;
    private boolean respondPings;

    public PaperConfiguration(JavaPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
        loadConfig();
    }

    public void reloadConfig() {
        // Force reload the config from disk
        plugin.reloadConfig();
        loadConfig();
    }

    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        allowedHosts = config.getStringList("allowed_hosts").toArray(new String[0]);

        try {
            responseType = ResponseType.valueOf(config.getString("response_type", "DISCONNECT_MESSAGE").toUpperCase());
        } catch (IllegalArgumentException e) {
            responseType = ResponseType.DISCONNECT_MESSAGE;
        }
        respondPings = config.getBoolean("respond_legacy_pings", false);
        responseMessage = config.getString("response_message", "<red>Invalid origin; please use the correct server address!</red>");
    }


    @Override
    public String toString() {
        return String.format("PaperConfiguration {hosts=%s,responseType=%s,respondLegacy=%s,msg=%s}", Arrays.toString(allowedHosts), responseType, respondPings, responseMessage);
    }

    @Override
    public String[] getAllowedHosts() {
        return allowedHosts;
    }

    @Override
    public ResponseType getResponseType() {
        return responseType;
    }

    @Override
    public String getResponseMessage() {
        return responseMessage;
    }

    @Override
    public boolean respondToLegacyPings() { return respondPings; }
}