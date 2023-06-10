package codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

import java.util.List;

public class UserDecoder extends ReplayingDecoder<UserDecoderState> {

    private int length;

    public UserDecoder() {
        // Set the initial state.
        super(UserDecoderState.READ_LENGTH);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        switch (state()) {
            case READ_LENGTH:
                length = in.readInt();
                checkpoint(UserDecoderState.READ_CONTENT);
            case READ_CONTENT:
                ByteBuf frame = in.readBytes(length);
                checkpoint(UserDecoderState.READ_LENGTH);
                byte[] bytes = new byte[frame.readableBytes()];
                frame.readBytes(bytes);
                User user = (User) KryoSerializer.deserialize(bytes);
                out.add(user);
                break;
            default:
                throw new IllegalStateException("invalid state: " + state());
        }
    }
}
