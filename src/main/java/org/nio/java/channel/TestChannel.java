package org.nio.java.channel;

import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * Created by XJX on 2017/4/11.
 * 一、通道（Channel）：用于源节点与目标节点的连接。在 Java NIO 中负责缓冲区中数据的传输。Channel 本身不存储数据，因此需要配合缓冲区进行传输。
 * <p>
 * 二、通道的主要实现类
 * java.nio.channels.Channel 接口：
 * |--FileChannel
 * |--SocketChannel
 * |--ServerSocketChannel
 * |--DatagramChannel
 * <p>
 * 三、获取通道
 * 1. Java 针对支持通道的类提供了 getChannel() 方法
 * 本地 IO：
 * FileInputStream/FileOutputStream
 * RandomAccessFile
 * <p>
 * 网络IO：
 * Socket
 * ServerSocket
 * DatagramSocket
 * <p>
 * 2. 在 JDK 1.7 中的 NIO.2 针对各个通道提供了静态方法 open()
 * 3. 在 JDK 1.7 中的 NIO.2 的 Files 工具类的 newByteChannel()
 * <p>
 * 四、通道之间的数据传输
 * transferFrom()
 * transferTo()
 * <p>
 * 五、分散(Scatter)与聚集(Gather)
 * 分散读取（Scattering Reads）：将通道中的数据分散到多个缓冲区中
 * 聚集写入（Gathering Writes）：将多个缓冲区中的数据聚集到通道中
 * <p>
 * 六、字符集：Charset
 * 编码：字符串 -> 字节数组
 * 解码：字节数组  -> 字符串
 */
public class TestChannel {

    //利用通道完成文件的复制（非直接缓冲区）
    @Test
    public void test01() {
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inputStream = new FileInputStream("pic/20170407_070822000_iOS.png");
            outputStream = new FileOutputStream("pic/1.jpg");

            //①获取通道
            inChannel = inputStream.getChannel();
            outChannel = outputStream.getChannel();

            //②分配指定大小的缓冲区
            ByteBuffer buffer = ByteBuffer.allocate(1024);

            //③将通道中的数据存入缓冲区中
            while (inChannel.read(buffer) != -1) {
                buffer.flip();
                outChannel.write(buffer);
                buffer.clear();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (outChannel != null) {
                    outChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (inChannel != null) {
                    inChannel.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    //使用直接缓冲区完成文件的复制(内存映射文件)，只有ByteBuffer支持
    @Test
    public void test02() {
        Instant start = Instant.now();

        FileChannel inChannel = null;
        FileChannel outChannel = null;
        try {
            inChannel = FileChannel.open(Paths.get("pic/20170408_140516000_iOS.png"), StandardOpenOption.READ);
            outChannel = FileChannel.open(Paths.get("pic/2.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ,
                    StandardOpenOption.CREATE);

            //内存映射文件
            MappedByteBuffer inMappedBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            MappedByteBuffer outMappedBuffer = outChannel.map(FileChannel.MapMode.READ_WRITE, 0, inChannel.size());

            //直接对缓冲区进行数据的读写操作
            byte[] bytes = new byte[inMappedBuffer.limit()];
            inMappedBuffer.get(bytes);
            outMappedBuffer.put(bytes);

            Instant end = Instant.now();
            System.out.println("所耗时间为：" + Duration.between(start, end).toMillis());
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

            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //通道之间的数据传输(直接缓冲区)
    @Test
    public void test03() {
        FileChannel inChannel = null;
        FileChannel outChannel = null;

        try {
            inChannel = FileChannel.open(Paths.get("pic/20170408_140516000_iOS.png"), StandardOpenOption.READ);
            outChannel = FileChannel.open(Paths.get("pic/3.jpg"), StandardOpenOption.WRITE, StandardOpenOption.READ,
                    StandardOpenOption.CREATE);
            outChannel.transferFrom(inChannel, 0, inChannel.size());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inChannel != null) {
                try {
                    inChannel.close();
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

    //分散和聚集
    @Test
    public void test04() {
        RandomAccessFile accessFile = null;
        FileChannel outChannel = null;
        FileChannel inChannel = null;
        try {
//            accessFile = new RandomAccessFile("pic/20170407_070822000_iOS.png", "rw");
//
//            //1. 获取通道
//            inChannel = accessFile.getChannel();
            inChannel = FileChannel.open(Paths.get("pic/20170407_070822000_iOS.png"), StandardOpenOption.READ);

            ////2. 分配指定大小的缓冲区
            ByteBuffer buffer1 = ByteBuffer.allocate(100);
            ByteBuffer buffer2 = ByteBuffer.allocate(1024);

            //4. 聚集写入
            outChannel = FileChannel.open(Paths.get("pic/4.jpg"), StandardOpenOption.WRITE,
                    StandardOpenOption.READ, StandardOpenOption.CREATE);

            //3. 分散读取
            ByteBuffer[] buffers = {buffer1, buffer2};
            while (inChannel.read(buffers) != -1) {

                for (ByteBuffer buffer : buffers) {
                    buffer.flip();
                }

                System.out.println(new String(buffers[0].array(), 0, buffers[0].limit()));
                System.out.println(new String(buffers[1].array(), 0, buffers[1].limit()));

                outChannel.write(buffers);

                for (ByteBuffer buffer : buffers) {
                    buffer.clear();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (accessFile != null) {
                try {
                    accessFile.close();
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

            if (inChannel != null) {
                try {
                    inChannel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //字符集
    @Test
    public void test05() {
        SortedMap<String, Charset> charsets = Charset.availableCharsets();
        Set<Map.Entry<String, Charset>> entrySets = charsets.entrySet();
        for (Map.Entry<String, Charset> entry : entrySets) {
            System.out.println(entry.getKey() + "=" + entry.getValue());
        }

        Charset charset = Charset.forName("UTF-8");

        //获取编码器
        CharsetEncoder encoder = charset.newEncoder();

        //获取解码器
        CharsetDecoder decoder = charset.newDecoder();

        CharBuffer buffer = CharBuffer.allocate(1024);
        buffer.put("无善无恶心之体，\n" +
                "有善有恶意之动。\n" +
                "知善知恶是良知，\n" +
                "为善去恶是格物。");
        buffer.flip();

        try {
            //编码
            ByteBuffer byteBuffer = encoder.encode(buffer);
            for (int i = 0; i < 62; i++) {
                System.out.print(byteBuffer.get());
            }

            //解码
            byteBuffer.flip();
            CharBuffer charBuffer = decoder.decode(byteBuffer);
            System.out.println(charBuffer.toString());
        } catch (CharacterCodingException e) {
            e.printStackTrace();
        }
    }
}
