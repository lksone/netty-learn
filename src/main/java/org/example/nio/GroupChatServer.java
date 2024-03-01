package org.example.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.Iterator;

/**
 * @author lks
 * @description 聊天室Service 服务器端
 * @e-mail 1056224715@qq.com
 * @date 2024/2/26 14:09
 */
@Slf4j
public class GroupChatServer {

    private Selector selector;

    private ServerSocketChannel listenChannel;

    private static final int port = 9999;

    public GroupChatServer() {
        try {
            selector = Selector.open();
            listenChannel = ServerSocketChannel.open();
            //绑定端口
            listenChannel.bind(new InetSocketAddress(port));
            ///设置非阻塞模式
            listenChannel.configureBlocking(false);

            //将该 listenChannel 注册到 selector
            listenChannel.register(selector, SelectionKey.OP_ACCEPT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void listen() {
        try {
            while (true) {
                int count = selector.select();
                if (count > 0) {
                    Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                    while (iterator.hasNext()) {
                        SelectionKey key = iterator.next();
                        //监听到 accept
                        if (key.isAcceptable()) {
                            SocketChannel sc = listenChannel.accept();
                            sc.configureBlocking(false);
                            //将该 sc 注册到 seletor
                            sc.register(selector, SelectionKey.OP_READ);
                            //提示
                            System.out.println(sc.getRemoteAddress() + " 上线 ");
                        }
                        //通道发送read事件，即通道是可读的状态(查看是否可读状态)
                        if (key.isReadable()) {
                            // 处理读(专门写方法..)
                            readData(key);
                        }
                        //当前的 key 删除，防止重复处理
                        iterator.remove();
                    }
                } else {
                    log.info("继续等待客户端连入--------------------------");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (listenChannel != null) {
                try {
                    listenChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 读取客户端信息
     *
     * @param key
     */
    private void readData(SelectionKey key) throws IOException {
        //得到channel
        SocketChannel channel = null;
        try {
            channel = (SocketChannel) key.channel();
            //创还能buffer
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            int read = channel.read(buffer);
            if (read > 0) {
                //把缓存区的数据转成字符串
                String msg = new String(buffer.array());
                //输出该消息
                log.info("form客户端:{}",msg);
                //向其它的客户端转发消息(去掉自己),专门写一个方法来处理
                sendInfoToOtherClients(msg, channel);
            }
        } catch (IOException e) {
            try {
                log.info(channel.getRemoteAddress() + "离线了..");
                //取消注册
                key.cancel();
                //关闭通道
                channel.close();
            } catch (IOException e2) {
                e2.printStackTrace();
            }
        }
    }

    /**
     * 发送数据给客户端
     *
     * @param msg  消息
     * @param self 自己
     */
    private void sendInfoToOtherClients(String msg, SocketChannel self) {
        log.info("服务器转发消息中---------------");
        try {
            for (SelectionKey selectionKey : selector.keys()) {
                SelectableChannel targetChannel = selectionKey.channel();
                //排除自己(自己和自己不能够通话)，将消息给其他的链接的客户端发送我发送的消息
                if (targetChannel instanceof SocketChannel && targetChannel != self) {
                    //转型
                    SocketChannel dest = (SocketChannel) targetChannel;
                    //将 msg 存储到 buffer
                    ByteBuffer buffer = ByteBuffer.wrap(msg.getBytes());
                    //将 buffer 的数据写入通道
                    dest.write(buffer);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        //创建服务器对象
        GroupChatServer groupChatServer = new GroupChatServer();
        groupChatServer.listen();
    }
}
