package me.q13x.originchecker.commons;

import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import net.mcbrawls.inject.api.Injector;
import net.mcbrawls.inject.api.InjectorContext;
import net.mcbrawls.inject.api.PacketDirection;
import org.jetbrains.annotations.NotNull;

/**
 * Helper class to block legacy Minecraft pings (and non-Minecraft connections)
 */
public class InjectLegacyHandler extends Injector {
    AbstractConfiguration config;

    public InjectLegacyHandler(AbstractConfiguration c) { this.config = c; }

    @Override
    public final boolean isRelevant(@NotNull InjectorContext ctx, PacketDirection direction) {
        if (config.respondToLegacyPings()) return false;
        if (ctx.pipeline().get("initial_checker") == null && ctx.pipeline().get("legacy_query") != null) {
            ctx.pipeline().addBefore("legacy_query", "initial_checker", new InitialPipelineChecker());
        }
        return false;
    }

    public class InitialPipelineChecker extends ChannelInboundHandlerAdapter {
        boolean checkedInitial = false;

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if (checkedInitial) {
                ctx.fireChannelRead(msg);
                return;
            }
            if (msg instanceof ByteBuf) {
                ByteBuf buf = (ByteBuf) msg;
                buf.markReaderIndex();
                try {
                    if (buf.readableBytes() == 0) {
                        return;
                    }

                    short packetId = buf.readUnsignedByte();

                    if (packetId == 0xFE) {
                        ctx.channel().config().setOption(ChannelOption.SO_LINGER, 0);
                        ctx.channel().close();
                        return;
                    } else {
                        try {
                            buf.resetReaderIndex();
                            var wrapper = PacketWrapper.createUniversalPacketWrapper(buf);
                            wrapper.readVarInt(); // proto ver
                            wrapper.readString(Short.MAX_VALUE); // host
                            wrapper.readShort(); // port
                            wrapper.readVarInt(); // intent
                        } catch (Exception e) {
                            ctx.channel().config().setOption(ChannelOption.SO_LINGER, 0);
                            ctx.channel().close();
                            return;
                        }
                    }
                } finally {
                    checkedInitial = true;
                    buf.resetReaderIndex();
                    ctx.fireChannelRead(msg);
                }
            }
        }
    }
}