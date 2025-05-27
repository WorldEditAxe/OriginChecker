# OriginChecker (1.21.5)
A simple Paper plugin that checks the server address of incoming connections, and blocks connections that do not have the correct server address. This works by examining a field in Minecraft Java's handshake packet that provides information about the intended server's hostname and port.
## Features
* Helps hide your Minecraft server from server scanners (ideal for private servers)
* Allows for stealthier cloaking by responding with TCP FIN or RST packets to have your server show up as completely offline
## Caveats
* <1.7 clients will not be able to ping or access the server at all
* Doesn't work for Bedrock clients/GeyserMC (Bedrock's protocol does not supply this information)
* Requires a domain name to be most effective
* Not tested (and may not work as intended) with BungeeCord/Velocity, but support isn't impossible
## Tutorial
### Prerequisites
* [PacketEvents](https://modrinth.com/plugin/packetevents) installed
* This plugin installed, obviously (from the latest [release](https://github.com/WorldEditAxe/originchecker/releases))
* A domain name for your server (A/AAAA/SRV record)
  * Any will work, if you don't want to pay you can use a dynamic DNS domain (e.g. [DuckDNS](https://www.duckdns.org/)) or one from a tunnel like [Playit](https://playit.gg). Both are free.
### Steps
1. Download this plugin (along with PacketEvents), and place them in your server's `plugins` folder.
2. Copy the following configuration into `plugins/OriginChecker/config.yml`. You will need to change a few things.
- Replace the example `localhost` hostname in `allowed_hosts` with the domain used to connect to your server.
3. Make sure that you can connect to the server, and you're done!
- If you want to go an extra mile to further hide your server, you can set `response_type` to `STEALTH_RST`. You will need to either reload the plugin (see below) or restart the server for it to take effect.
## Sample Configuration
The configuration can be reloaded on the fly with `/originchecker reload` (requires the permission `originchecker.reload`).
```yaml
# OriginChecker Configuration

# List of allowed hostnames/domains that players can connect from
allowed_hosts:
  - "localhost"

# How to respond to invalid origins
# Valid options: DISCONNECT_MESSAGE, STEALTH_FIN, STEALTH_RST
response_type: DISCONNECT_MESSAGE

# Message to show when a player is disconnected due to invalid origin
# (only valid when response_type is set to DISCONNECT_MESSAGE)
response_message: '<red>Invalid origin; please use the correct server address!</red>'

# Whether to respond to legacy ping requests (pre-1.7)
# It is recommended that you leave this set to false,
# people can find the server if it still responds to legacy
# pings! Realistically speaking, you probably won't need this :P
# This will also have the side effect of immediately closing
# non-Minecraft connections to the server.
respond_legacy_pings: false
```