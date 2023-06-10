package udp;

import codec.User;
import codec.UserDatagramDecoder;
import codec.UserDatagramEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class SimpleUdp extends Thread {

    private static final Logger log = LoggerFactory.getLogger(SimpleUdp.class);

    private final String name;
    private final InetSocketAddress addr;
    private InitListener listener;
    private Channel channel;

    public SimpleUdp(String name, InetSocketAddress addr) {
        this.name = name;
        this.addr = addr;
    }

    public interface InitListener {
        void initComplete();
    }

    public void addInitListener(InitListener listener) {
        this.listener = listener;
    }

    @Override
    public void run() {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group)
                    .channel(NioDatagramChannel.class)
                    .handler(new ChannelInitializer<DatagramChannel>() {
                        @Override
                        protected void initChannel(DatagramChannel ch) {
                            ch.pipeline()
                                    .addLast(new UserDatagramEncoder())
                                    .addLast(new UserDatagramDecoder())
                                    .addLast(new UserHandler(name));
                        }
                    })
                    .option(ChannelOption.SO_BROADCAST, true);
            channel = bootstrap.bind(addr).sync().channel();
            log.info("{} - listening {}", name, channel.localAddress());
            if (listener != null) {
                listener.initComplete();
            }
            channel.closeFuture().sync();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            group.shutdownGracefully();
        }
    }

    public void send(User msg, InetSocketAddress recipient) {
        try {
            if (channel != null) {
                AddressedEnvelope<User, InetSocketAddress> envelope = new DefaultAddressedEnvelope<>(msg, recipient);
                channel.writeAndFlush(envelope).sync();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        final InetSocketAddress addr0 = new InetSocketAddress("127.0.0.1", 65001);
        final InetSocketAddress addr1 = new InetSocketAddress("127.0.0.1", 65002);
        final InetSocketAddress broadcastAddr = new InetSocketAddress("255.255.255.255", 65002);
        final SimpleUdp su0 = new SimpleUdp("su0", addr0);
        final SimpleUdp su1 = new SimpleUdp("su1", addr1);
        su0.addInitListener(new InitListener() {
            @Override
            public void initComplete() {
                su0.send(new User(0L, "Romeo"), addr1);
                su0.send(new User(1L, "Juliet"), broadcastAddr);
            }
        });
        su0.start();
        su1.start();
    }
}
