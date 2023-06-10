package tcp;

import codec.User;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class UserServerHandler extends ChannelInboundHandlerAdapter {

    private final String name;

    public UserServerHandler(String name) {
        this.name = name;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        User msg = new User(117L, "John");
        final ChannelFuture f = ctx.writeAndFlush(msg);
        f.addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
