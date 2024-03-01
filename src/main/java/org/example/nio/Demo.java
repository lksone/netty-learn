package org.example.nio;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * @author lks
 * @description todo
 * @e-mail 1056224715@qq.com
 * @date 2024/2/23 13:32
 */
public class Demo {

    public static void main(String[] args) {


    }

    public static void buffer(){
        try {
            //写入信息到指定的文件中
            FileOutputStream fos = new FileOutputStream("test.txt");
            FileChannel channel = fos.getChannel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            buffer.put("hello zuiyu!".getBytes());
            buffer.flip();
            channel.write(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 多通道
     * @throws Exception
     */
    public void buffersTest() throws Exception{
        // 1、字节输入管道
        FileInputStream fis = new FileInputStream("test.txt");
        FileChannel fisChannel = fis.getChannel();
        // 2、字节输出管道
        FileOutputStream fos = new FileOutputStream("test02.txt");
        FileChannel fosChannel = fos.getChannel();
        // 3、定义多个缓冲区做数据分散
        ByteBuffer buffer1 = ByteBuffer.allocate(4);
        ByteBuffer buffer2 = ByteBuffer.allocate(1024);
        ByteBuffer[] buffers  = {buffer1,buffer2};
        // 4、从通道中读取数据分散到各个缓冲区
        fisChannel.read(buffers);
        for (ByteBuffer buffer : buffers) {
            // 切换读数据模式
            buffer.flip();
            System.out.println(new String(buffer.array(),0,buffer.remaining()));
        }
        // 聚集写入到通道
        fosChannel.write(buffers);
        fisChannel.close();
        fosChannel.close();
        System.out.println("文件复制完成");
    }
}
