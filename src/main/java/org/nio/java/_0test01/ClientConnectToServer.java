package org.nio.java._0test01;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/**
 * Created by XJX on 2017/3/13.
 */
public class ClientConnectToServer {

    public static void main(String[] args) {
//        ConcurrentLinkedQueue<String> queue = new ConcurrentLinkedQueue<String>();
//        ExecutorService executorService = ExecutorServiceSingleton.getInstance();

        SocketChannel channel = null;
        Selector selector = null;

        try {
            channel = SocketChannel.open();
            channel.configureBlocking(false);
            channel.connect(new InetSocketAddress("120.55.94.78", 9999));
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_CONNECT);
            //selector.select();
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
