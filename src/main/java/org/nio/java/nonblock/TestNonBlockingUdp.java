package org.nio.java.nonblock;

import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.Scanner;

/**
 * Created by XJX on 2017/4/14.
 */
public class TestNonBlockingUdp {

    @Test
    public void send() {
        DatagramChannel datagramChannel = null;
        try {
            datagramChannel = DatagramChannel.open();
            datagramChannel.configureBlocking(false);
            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            Scanner in = new Scanner(System.in);
//            while (in.hasNext()) {
            String str = "空灭境";
            buffer.put((LocalDateTime.now().toString() + ":\n" + str).getBytes());
            buffer.flip();
            datagramChannel.send(buffer, new InetSocketAddress("127.0.0.1", 9999));
            buffer.clear();
//            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (datagramChannel != null) {
                try {
                    datagramChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Test
    public void receive() {
        DatagramChannel channel = null;
        Selector selector = null;
        try {
            channel = DatagramChannel.open();
            channel.configureBlocking(false);
            channel.bind(new InetSocketAddress(9999));
            selector = Selector.open();
            channel.register(selector, SelectionKey.OP_READ);
            while (selector.select() > 0) {
                Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        ByteBuffer buffer = ByteBuffer.allocate(1024);
                        channel.receive(buffer);
                        buffer.flip();
                        System.out.println(new String(buffer.array(), 0, buffer.limit()));
                        buffer.clear();
                    }
                }
                iterator.remove();
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
