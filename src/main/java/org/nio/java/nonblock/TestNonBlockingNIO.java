package org.nio.java.nonblock;

import com.sun.org.apache.regexp.internal.RE;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;

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
public class TestNonBlockingNIO {

    //客户端
    @Test
    public void client() {

        SocketChannel socketChannel = null;
        FileChannel inChannel = null;

        try {
            //1. 获取通道
            socketChannel = SocketChannel.open(new InetSocketAddress("127.0.0.1", 9999));
            //2. 切换非阻塞模式
            socketChannel.configureBlocking(false);
            //3. 分配指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            //4. 发送数据给服务端
            inChannel = FileChannel.open(Paths.get("pic/20170408_140516000_iOS.png"), StandardOpenOption.READ);
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                socketChannel.write(buffer);
                buffer.clear();
            }

            //接收服务器端反馈
//            Charset charset = Charset.forName("utf-8");
//            CharsetDecoder decoder = charset.newDecoder();
//            while (socketChannel.read(buffer) != -1) {
//                buffer.flip();
//                CharBuffer charBuffer = decoder.decode(buffer);
//                System.out.println(charBuffer.toString());
//            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socketChannel != null) {
                try {
                    socketChannel.close();
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
        Selector selector = null;
        SocketChannel socketChannel = null;
        FileChannel outChannel = null;

        try {
            //1. 获取通道
            serverSocketChannel = ServerSocketChannel.open();
            //2. 切换非阻塞模式
            serverSocketChannel.configureBlocking(false);
            //3. 绑定连接
            serverSocketChannel.bind(new InetSocketAddress(9999));
            //4. 获取选择器
            selector = Selector.open();
            //5. 将通道注册到选择器上, 并且指定“监听接收事件”
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
            //6. 轮询式的获取选择器上已经“准备就绪”的事件
            while (selector.select() > 0) {
                //7. 获取当前选择器中所有注册的“选择键(已就绪的监听事件)”
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    //8. 获取准备“就绪”的是事件
                    SelectionKey selectionKey = iterator.next();
                    //9. 判断具体是什么事件准备就绪
                    if (selectionKey.isAcceptable()) {
                        //10. 若“接收就绪”，获取客户端连接
                        socketChannel = serverSocketChannel.accept();
                        //11. 切换非阻塞模式
                        socketChannel.configureBlocking(false);
                        //12. 将该通道注册到选择器上
                        socketChannel.register(selector, SelectionKey.OP_READ);
                    }

                    if (selectionKey.isReadable()) {
                        outChannel = FileChannel.open(Paths.get("pic/7.jpg"), StandardOpenOption.WRITE,
                                StandardOpenOption.READ, StandardOpenOption.CREATE);
                        //13. 获取当前选择器上“读就绪”状态的通道
                        socketChannel = (SocketChannel) selectionKey.channel();
                        //14. 读取数据
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        while (socketChannel.read(buffer) != -1) {
                            buffer.flip();
                            outChannel.write(buffer);
                            buffer.clear();
                        }

                        outChannel.close();

                        //向客户端反馈
//                        Charset charset = Charset.forName("utf-8");
//                        CharsetEncoder encoder = charset.newEncoder();
//                        CharBuffer charBuffer = CharBuffer.allocate(1024);
//                        charBuffer.put("服务端已收到图片");
//                        charBuffer.flip();
//                        buffer = encoder.encode(charBuffer);
//                        socketChannel.write(buffer);
//                        charBuffer.clear();
//                        buffer.clear();

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (outChannel != null) {
                try {
                    outChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (serverSocketChannel != null) {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (selector != null) {
                try {
                    selector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
