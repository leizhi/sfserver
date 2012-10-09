package com.mooo.mycoz.socket;
import java.io.IOException;  
import java.net.InetSocketAddress;  
import java.nio.ByteBuffer;  
import java.nio.channels.SocketChannel;  

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
  
/** 
 * NIO客户端 
 *  
 * @author shirdrn 
 */  
public class NioTcpClient {  
  
	private static Log log = LogFactory.getLog(NioTcpClient.class);
	
    private InetSocketAddress inetSocketAddress;  
      
    public NioTcpClient(String hostname, int port) {  
        inetSocketAddress = new InetSocketAddress(hostname, port);  
    }  
      
    /** 
     * 发送请求数据 
     * @param requestData 
     */  
    public void send(String requestData) {  
        try {  
            SocketChannel socketChannel = SocketChannel.open(inetSocketAddress);  
            socketChannel.configureBlocking(false);  
            ByteBuffer byteBuffer = ByteBuffer.allocate(512);  
            socketChannel.write(ByteBuffer.wrap(requestData.getBytes()));  
            while (true) {  
                byteBuffer.clear();  
                int readBytes = socketChannel.read(byteBuffer);  
                if (readBytes > 0) {  
                    byteBuffer.flip();  
                    log.info("Client: readBytes = " + readBytes);  
                    log.info("Client: data = " + new String(byteBuffer.array(), 0, readBytes));  
                    socketChannel.close();  
                    break;  
                }  
            }  
  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    public static void main(String[] args) {  
        String hostname = "127.0.0.1";  
        String requestData = "Actions speak louder than words!";  
        int port = 8000;  
        new NioTcpClient(hostname, port).send(requestData);  
    }  
} 
