package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class UserDatagramDecoder extends MessageToMessageDecoder<DatagramPacket> {

    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket packet, List<Object> out) {
        ByteBuf buf = packet.content();
        byte[] bytes = new byte[buf.readableBytes()];
        buf.readBytes(bytes);
        User user = (User) KryoSerializer.deserialize(bytes);
        out.add(user);
    }
}
