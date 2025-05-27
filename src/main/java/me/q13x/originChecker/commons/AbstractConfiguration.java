package me.q13x.originchecker.commons;

public abstract class AbstractConfiguration {
    public boolean isCorrectOrigin(String addr) {
        for (String host : getAllowedHosts()) {
            if (host.equalsIgnoreCase(addr)) {
                return true;
            }
        }
        return false;
    }

    public abstract String[] getAllowedHosts();
    public abstract ResponseType getResponseType();
    public abstract String getResponseMessage();
    public abstract boolean respondToLegacyPings();
}
