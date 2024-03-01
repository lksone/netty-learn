package org.example.bio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author lks
 * @description BIO连接数据信息
 * @e-mail 1056224715@qq.com
 * @date 2024/2/22 17:37
 */
@Slf4j
public class BIOServer {

    public static void main(String[] args) throws IOException {
        ExecutorService executorService = Executors.newCachedThreadPool();
        ServerSocket serverSocket = new ServerSocket(9999);
        System.out.println("服务器启动---------------");
        while (true) {
            log.info("123");
            System.out.println("线程信息ID=" + Thread.currentThread().getId() + "名称：" + Thread.currentThread().getName());
            Socket accept = serverSocket.accept();
            System.out.println("连接到一个客户断");
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    //处理具体的逻辑
                    handler(accept);
                }
            });

        }
    }

    /**
     *
     * @param socket
     */
    public static void handler(Socket socket) {
        try {
            System.out.println("线程名称：" + Thread.currentThread().getName() + "线程ID:" + Thread.currentThread().getId());

            byte[] bytes = new byte[1024 * 3];
            //获取socket传输的报文
            InputStream inputStream = socket.getInputStream();
            int read;
            while ((read = inputStream.read(bytes)) != -1) {
                //输出客户端发送的数据
                System.out.println(new String(bytes, 0, read));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
