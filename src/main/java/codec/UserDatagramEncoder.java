package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.AddressedEnvelope;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.net.InetSocketAddress;
import java.util.List;

public class UserDatagramEncoder extends MessageToMessageEncoder<AddressedEnvelope<User, InetSocketAddress>> {

    @Override
    protected void encode(ChannelHandlerContext ctx, AddressedEnvelope<User, InetSocketAddress> msg, List<Object> out) throws Exception {
        User user = msg.content();
        byte[] bytes = KryoSerializer.serialize(user);
        ByteBuf buf = ctx.alloc().buffer(bytes.length);
        buf.writeBytes(bytes);
        DatagramPacket packet = new DatagramPacket(buf, msg.recipient());
        out.add(packet);
    }
}
