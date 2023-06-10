package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class UserEncoder extends MessageToByteEncoder<User> {

    @Override
    protected void encode(ChannelHandlerContext ctx, User msg, ByteBuf out) throws Exception {
        byte[] bytes = KryoSerializer.serialize(msg);
        int length = bytes.length;
        out.writeInt(length).writeBytes(bytes);
    }
}
