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

/**
 * 常规 UDP 通讯示例
 * <p>
 * a. 使用编解码器
 * b. 支持单播/广播
 */
public class CommonUdp extends Thread {

    private static final Logger log = LoggerFactory.getLogger(CommonUdp.class);

    /**
     * 用于标识的名称
     */
    private final String name;

    /**
     * 绑定的本地地址、端口
     */
    private final InetSocketAddress addr;

    /**
     * 启动完成回调
     */
    private InitListener listener;

    /**
     * 建立的 DatagramChannel
     */
    private Channel channel;

    public interface InitListener {
        void initComplete() throws Exception;
    }

    public CommonUdp(String name, InetSocketAddress addr) {
        this.name = name;
        this.addr = addr;
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
                        protected void initChannel(DatagramChannel ch) throws Exception {
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
        final CommonUdp su0 = new CommonUdp("su0", addr0);
        final CommonUdp su1 = new CommonUdp("su1", addr1);
        su0.addInitListener(new InitListener() {
            @Override
            public void initComplete() throws Exception {
                // 单播
                su0.send(new User(0L, "Romeo"), addr1);
                // 广播
                su0.send(new User(1L, "Juliet"), broadcastAddr);
            }
        });
        su0.start();
        su1.start();
    }
}
