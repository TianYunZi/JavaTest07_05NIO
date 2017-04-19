package org.nio.java.nonblock;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Created by XJX on 2017/4/14.
 * 一、使用 NIO 完成网络通信的三个核心：
 * <p>
 * 1. 通道（Channel）：负责连接
 * <p>
 * java.nio.channels.Channel 接口：
 * |--SelectableChannel
 * |--SocketChannel
 * |--ServerSocketChannel
 * |--DatagramChannel
 * <p>
 * |--Pipe.SinkChannel
 * |--Pipe.SourceChannel
 * <p>
 * 2. 缓冲区（Buffer）：负责数据的存取
 * <p>
 * 3. 选择器（Selector）：是 SelectableChannel 的多路复用器。用于监控 SelectableChannel 的 IO 状况
 */
public class TestBlockingNIO {

    //客户端
    @Test
    public void client() {
        SocketChannel channel = null;
        FileChannel inChannel = null;

        //1. 获取通道
        try {
            channel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));
            inChannel = FileChannel.open(Paths.get("pic/20170407_070822000_iOS.png"), StandardOpenOption.READ);

            //2. 分配指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //3. 读取本地文件，并发送到服务端
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                channel.write(buffer);
                buffer.clear();
            }

            channel.shutdownOutput();//告诉服务端发送完成

            //接收服务端反馈
            Charset charset = Charset.forName("utf-8");
            CharsetDecoder decoder = charset.newDecoder();
            while (channel.read(buffer) != -1) {
                buffer.flip();
                System.out.println(decoder.decode(buffer).toString());
                buffer.clear();
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //服务端
    @Test
    public void server() {
        ServerSocketChannel serverSocketChannel = null;
        SocketChannel socketChannel = null;
        FileChannel outChannel = null;
        CharBuffer charBuffer = null;

        try {
            //1. 获取通道
            serverSocketChannel = ServerSocketChannel.open();
            //2. 绑定连接
            serverSocketChannel.bind(new InetSocketAddress(9999));
            //3. 获取客户端连接的通道
            socketChannel = serverSocketChannel.accept();
            outChannel = FileChannel.open(Paths.get("pic/5.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ,
                    StandardOpenOption.CREATE);
            //4. 分配指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //5. 接收客户端的数据，并保存到本地
            while (socketChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }

            //发送反馈给客户端
            Charset charset = Charset.forName("utf-8");
            CharsetEncoder encoder = charset.newEncoder();
            charBuffer = CharBuffer.allocate(1024);
            charBuffer.put("服务器已收到图片");
            charBuffer.flip();
            buffer = encoder.encode(charBuffer);
            socketChannel.write(buffer);
            charBuffer.clear();
            buffer.clear();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            //6. 关闭通道
            if (serverSocketChannel != null) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (socketChannel != null) {
                try {
                    socketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
