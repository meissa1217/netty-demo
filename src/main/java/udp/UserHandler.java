package udp;

import codec.User;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserHandler extends SimpleChannelInboundHandler<User> {

    private static final Logger log = LoggerFactory.getLogger(UserHandler.class);

    private final String name;

    public UserHandler(String name) {
        this.name = name;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, User msg) throws Exception {
        log.info("{} - receive msg: {}", name, msg);
    }
}
