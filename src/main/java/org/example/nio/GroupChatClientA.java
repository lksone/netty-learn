package org.example.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Scanner;
import java.util.Set;

/**
 * @author lks
 * @description 客户端代码
 * @e-mail 1056224715@qq.com
 * @date 2024/2/26 15:03
 */
@Slf4j
public class GroupChatClientA {

    //定义相关的属性
    private final String HOST = "127.0.0.1";
    private final int PORT = 9999;
    //选择器
    private Selector selector;
    private SocketChannel socketChannel;
    private String username;

    //构造器初始化
    public GroupChatClientA() {
        try {
            //获取连接器
            selector = Selector.open();
            //连接服务器
            socketChannel = SocketChannel.open(new InetSocketAddress(HOST, PORT));
            socketChannel.configureBlocking(false);
            socketChannel.register(selector, SelectionKey.OP_READ);
            //获取登录的IP地址
            username = socketChannel.getLocalAddress().toString();
            log.info(username + " is ok...");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 向服务器发送消息
     *
     * @param info
     */
    public void sendInfo(String info) {
        info = username + " 说：" + info;
        try {
            //将信息发送给服务器
            socketChannel.write(ByteBuffer.wrap(info.getBytes()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 读取从服务器端回复的消息
     */
    public void readInfo() {
        try {
            Set<SelectionKey> keys = selector.keys();
            //获取通道个数
            int readChannels = selector.select();
            if (readChannels < 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey next = iterator.next();
                    if (next.isReadable()) {
                        //得到相关的通道
                        SocketChannel sc = (SocketChannel) next.channel();
                        //得到一个 Buffer
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        //读取
                        sc.read(buffer);
                        //把读到的缓冲区的数据转成字符串
                        String msg = new String(buffer.array());
                        log.info(msg.trim());
                    }
                }
                //删除当前的 selectionKey,防止重复操作
                iterator.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        //启动我们客户端
        GroupChatClientA chatClient = new GroupChatClientA();
        //启动一个线程,每个 3 秒，读取从服务器发送数据
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    chatClient.readInfo();
                    try {
                        Thread.currentThread().sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        //发送数据给服务器端,发送数据信息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String s = scanner.nextLine();
            chatClient.sendInfo(s);
        }
    }
}
