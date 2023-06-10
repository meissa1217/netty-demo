package tcp;

import codec.UserDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;

public class SimpleTcpClient extends Thread {

    private final String name;
    private final InetSocketAddress remoteAddr;

    public SimpleTcpClient(String name, InetSocketAddress remoteAddr) {
        this.name = name;
        this.remoteAddr = remoteAddr;
    }

    @Override
    public void run() {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(workerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new UserDecoder())
                                    .addLast(new UserClientHandler(name));
                        }
                    });
            ChannelFuture f = bootstrap.connect(remoteAddr).sync();
            f.channel().closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    public static void main(String[] args) {
        final InetSocketAddress remoteAddr = new InetSocketAddress("127.0.0.1", 65001);
        final SimpleTcpClient client = new SimpleTcpClient("client", remoteAddr);
        client.start();
    }
}