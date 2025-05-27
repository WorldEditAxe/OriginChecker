package me.q13x.originchecker.commons;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListener;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerDisconnect;
import io.netty.channel.ChannelOption;
import io.netty.channel.socket.nio.NioSocketChannel;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.logging.Logger;

public class ModernHandshakeHandler {
    public static void hookModern(AbstractConfiguration config, Logger logger) {
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListener() {
                    @Override
                    public void onPacketReceive(PacketReceiveEvent e) {
                        if (e.getPacketType() != PacketType.Handshaking.Client.HANDSHAKE && e.getPacketType() != PacketType.Handshaking.Client.LEGACY_SERVER_LIST_PING)
                            return;
                        NioSocketChannel sock = ((NioSocketChannel) e.getChannel());
                        User u = e.getUser();

                        var hs = new WrapperHandshakingClientHandshake(e);
                        if (!config.isCorrectOrigin(hs.getServerAddress())) {
                            if (config.getAllowedHosts().length == 0) {
                                logger.warning("There was a ping/login request sent to your server, but no hostnames have been configured in OriginChecker!");
                                logger.warning("Navigate to plugins/OriginChecker/config.yml, and add an hostname to allowed_hosts to fix this issue!");

                                if (hs.getIntention() != WrapperHandshakingClientHandshake.ConnectionIntention.STATUS) {
                                    u.sendPacket(new WrapperLoginServerDisconnect(
                                            MiniMessage.miniMessage().deserialize("<red>OriginChecker hasn't been configured yet; add some hosts to allowed_hosts!</red>")
                                    ));
                                }
                                u.closeConnection();
                                return;
                            }
                            switch (config.getResponseType()) {
                                case DISCONNECT_MESSAGE:
                                    if (hs.getIntention() != WrapperHandshakingClientHandshake.ConnectionIntention.STATUS) {
                                        u.sendPacket(new WrapperLoginServerDisconnect(
                                                MiniMessage.miniMessage().deserialize(config.getResponseMessage())
                                        ));
                                    }
                                    u.closeConnection();
                                    break;
                                case STEALTH_FIN:
                                    u.closeConnection();
                                    break;
                                case STEALTH_RST:
                                    sock.config().setOption(ChannelOption.SO_LINGER, 0);
                                    try {
                                        sock.close().sync();
                                    } catch (InterruptedException ex) {
                                        throw new RuntimeException(ex);
                                    }
                                    break;
                            }
                        }
                    }
                }, PacketListenerPriority.HIGHEST
        );
    }

}
