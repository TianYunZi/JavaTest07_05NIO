package org.nio.java._0test02;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by XJX on 2017/3/13.
 */
public class SocketTest {

    /**
     * 接收服务消息并向服务器发送消息.
     */
    public static void main(String[] args) {
        Socket socket = null;
        try {
            socket = new Socket("120.55.94.78", 12345);
//            socket = new Socket("127.0.0.1", 8080);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(socket.getInputStream());
            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
            String str = "今天风很大";
            System.out.println("开始向服务器发送消息：");
            dataOutputStream.writeUTF(str);
            System.out.println("服务器发送消息完成.");
            byte[] bytes = new byte[1024];
            int temp = 0;
            while ((temp = bufferedInputStream.read(bytes)) != -1) {
                System.out.println("正在读取消息:" + new String(bytes, 0, temp));
            }
            System.out.println("接收服务器消息完成.");
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
