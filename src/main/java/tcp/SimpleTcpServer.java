package tcp;

import codec.UserEncoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * TCP 通讯示例 - 服务端
 */
public class SimpleTcpServer extends Thread {

    private static final Logger log = LoggerFactory.getLogger(SimpleTcpServer.class);

    /**
     * 用于标识的名称
     */
    private final String name;

    /**
     * 绑定的本地地址、端口
     */
    private final InetSocketAddress addr;

    public SimpleTcpServer(String name, InetSocketAddress addr) {
        this.name = name;
        this.addr = addr;
    }

    @Override
    public void run() {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline()
                                    .addLast(new UserEncoder())
                                    .addLast(new UserServerHandler(name));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture f = serverBootstrap.bind(addr).sync();
            log.info("{} - listening {}", name, f.channel().localAddress());
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 65001);
        final SimpleTcpServer server = new SimpleTcpServer("server", addr);
        server.start();
    }
}
