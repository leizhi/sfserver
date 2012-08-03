package com.mooo.mycoz.sfserver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class NIOSFServer {
	private static Log log = LogFactory.getLog(NIOSFServer.class);

//	private static Object initLock = new Object();

	private Vector<Thread> threadPool;
	private int maxConnMSec;
	
//	protected static final int SERVER_PORT = Integer.valueOf(PropertyManager.getProperty("serverPort")).intValue();

	private static int SERVICE_PORT = 8000;
	
	public NIOSFServer(int maxConns,double maxConnTime) throws IOException {
//		super(SERVICE_PORT);

	    ServerSocketChannel server = ServerSocketChannel.open();  
	    server.configureBlocking(false);  
	    server.socket().bind(new InetSocketAddress(SERVICE_PORT));  
	    Selector select = Selector.open();  
	    server.register(select, SelectionKey.OP_ACCEPT);  

	    /*
		select.select();
		Set readkeys = select.selectedKeys();
		Iterator iterator = readkeys.iterator();
		while (iterator.hasNext()) {
			SelectionKey key = (SelectionKey) iterator.next();
			if (key.isAcceptable()) {
				SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
				System.out.println("Accept connection from: " + client);
				client.configureBlocking(false);
				client.register(key.selector(), SelectionKey.OP_READ,ByteBuffer.allocate(1024));
			}
			if (key.isReadable()) {
				// 获得与客户端通信的信道
				SocketChannel clientChannel = (SocketChannel) key.channel();
				// 得到并清空缓冲区
				ByteBuffer buffer = (ByteBuffer) key.attachment();
				buffer.clear();
				// 读取信息获得读取的字节数
				long bytesRead = clientChannel.read(buffer);
				if (bytesRead == -1) {
					// 没有读取到内容的情况
					clientChannel.close();
				} else {
					// 将缓冲区准备为数据传出状态
					buffer.flip();
					// 将字节转化为为UTF-16的字符串
					String receivedString = Charset.forName("UTF-16")
							.newDecoder().decode(buffer).toString();
					// 控制台打印出来
					System.out.println("接收到来自"
							+ clientChannel.socket().getRemoteSocketAddress()
							+ "的信息:" + receivedString);
					// 准备发送的文本
					String sendString = "你好,客户端. @" + new Date().toString()
							+ "，已经收到你的信息" + receivedString;
					buffer = ByteBuffer.wrap(sendString.getBytes("UTF-16"));
					clientChannel.write(buffer);
					// 设置为下一次读取或是写入做准备
					key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
				}
			}
			if (key.isWritable()) {
				SocketChannel sc = (SocketChannel) key.channel();
				ByteBuffer writeBuffer = ByteBuffer.wrap("我的程序员之道"
						.getBytes("UTF-16"));
				sc.write(writeBuffer);
			}
			key.channel().close();
		}
*/
		//
		if(log.isDebugEnabled()) log.debug("服务器启动");
		
		threadPool = new Vector<Thread>(maxConns);

		maxConnMSec = (int) (maxConnTime * 86400000.0); // 86400 sec/day
		if (maxConnMSec < 30000 && maxConnMSec > 0) { // Recycle no less than 30 seconds.
			maxConnMSec = 30000;
		}

		try {
			//主线程进入轮询模式
			boolean forever = true;
			while (forever) {
					if(maxConns > 0 && threadPool.size() == maxConns){
						throw new Exception("线程池满");
					}
					
					//计算连接数
					int runCount = 0;
					for (Thread threadObj : threadPool) {
						if (threadObj.isAlive())
							runCount++;
					}
					if(log.isDebugEnabled())log.debug(">>>>>>>>>>>运行线程数:"+ runCount);
					if(log.isErrorEnabled()) log.error(">>>>>>>>>>>运行线程数:"+ runCount);

//					synchronized (initLock) {
//						wait(1000*10); //10 seconds
//					}
					
					//处理客户端请求并生成子线程
//					Thread thread = new Thread(new SessionThread(accept()));
					
//					thread.start();
					
					select.select();
					Set<SelectionKey> keys = select.selectedKeys();
					Iterator<SelectionKey> iter = keys.iterator();
//					SocketChannel sc;
					while (iter.hasNext()) {
						SelectionKey key = iter.next();
	
						if (key.isAcceptable())
							;//新的连接
						else if (key.isReadable())
							;//可读
						
						iter.remove(); //处理完事件的要从keys中删去
					}
				     
					Thread.sleep(20);//wait 20ms
//					threadPool.add(thread);
					if(log.isDebugEnabled())log.debug("<<<<<<<<<<<<LOOP Watch======");
			}//Loop End
		} catch (InterruptedException e) {
			e.printStackTrace();
			if(log.isErrorEnabled()) log.error("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			if(log.isErrorEnabled()) log.error("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
		} finally {
//			close();
		}
		
	}

	/*
	 * 访问子线程
	 */
	class SessionThread implements Runnable {
		private Socket socket;
		private OutputStream out;
		private InputStream in;

		private BufferedReader read = null;  
		private PrintStream print = null; 
		
		public SessionThread(Socket socket) throws IOException {
			this.socket = socket;

			in = socket.getInputStream();
			out = socket.getOutputStream();

			read = new BufferedReader(new InputStreamReader(in,"GBK"));  
			print = new PrintStream(out,true,"GBK");
		}

		public void run() {
			Integer userId=null;

			try {
//				String message = "--- Welcome to this chatroom 欢迎---";
//				print.println(message);
//				print.print(">");
				String requestLine=null;
				
				boolean forever = true;
				while (forever){			
					requestLine = read.readLine();
					
					if(log.isErrorEnabled()) log.error("requestLine:"+requestLine);

					if(requestLine==null || requestLine.equals("3")|| requestLine.equals("*3#"))
						break;
					
					requestLine = requestLine.trim();
					
					//打印请求数据
//					print.println("requestLine :" + requestLine);
					
					String response = ActionFactory.getInstance().forward(requestLine);
					//打印响应数据
//					print.println("response :" + response);
					print.println(response);
					
					String value=null;
					String split = "*0,"+Action.ACTION_SYN+",";
					if(response.indexOf(split)>-1){
						value = response.substring(split.length(),response.length()-1);
						if(log.isErrorEnabled()) log.error("value:"+value);
						if(value!=null && !value.equals("")){
							userId=new Integer(value);
						}
					}
					//wait input
					//print.print(">");
				}//end while
				
			} catch (Exception e) {
				e.printStackTrace();
				if(log.isErrorEnabled()) log.error("客户失去连接...");
	    		if(log.isErrorEnabled()) log.error("客户失去连接...");	

			} finally {
				try {
					if(log.isErrorEnabled()) log.error("客户退出...");
					
					if( in !=null)
						in.close();
					if( out !=null)
						out.close();
					if(!socket.isClosed())
						socket.close();
					
					threadPool.remove(this);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}//run end
	}
	
	public static void main(String[] args) throws IOException {
		new NIOSFServer(0,0);
	}
}