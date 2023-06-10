package tcp;

import codec.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger log = LoggerFactory.getLogger(UserClientHandler.class);

    private final String name;

    public UserClientHandler(String name) {
        this.name = name;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("{} - connected to: {}", name, ctx.channel().remoteAddress());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        User m = (User) msg;
        log.info("{} - receive msg: {}", name, m);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
