package com.mooo.mycoz.sfwine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class WineServer extends ServerSocket {
	private static Log log = LogFactory.getLog(WineServer.class);

//	private static Object initLock = new Object();

	private Vector<Thread> threadPool;
	private int maxConnMSec;
	
//	protected static final int SERVER_PORT = Integer.valueOf(PropertyManager.getProperty("serverPort")).intValue();

	private static int SERVICE_PORT = 8000;
	
	public WineServer(int maxConns,double maxConnTime) throws IOException {
		super(SERVICE_PORT);

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
				try {
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
					System.out.println(">>>>>>>>>>>运行线程数:"+ runCount);

//					synchronized (initLock) {
//						wait(1000*10); //10 seconds
//					}
					
					//处理客户端请求并生成子线程
					Thread thread = new Thread(new SessionThread(accept()));
					thread.start();
					
					Thread.sleep(20);//wait 20ms
					threadPool.add(thread);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(log.isDebugEnabled())log.debug("<<<<<<<<<<<<LOOP Watch======");
			}//Loop End
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
		} finally {
			close();
		}
		
	}

	////////////////////////////////////////////////
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

			// 从Socket中获取输入流和输出流，由于我们只做一个简单的字符串通讯，所以采用BufferedRead和PrintStream来封装输入、输出流   
			read = new BufferedReader(new InputStreamReader(socket.getInputStream()));  
			print = new PrintStream(socket.getOutputStream());  
		}

		public void run() {
				try {
					String message = "--- Welcome to this chatroom 欢迎---\n";
					print.println(message);
//					out.write(value.getBytes());
					message = "input your nickname:\n";
//					out.write(value.getBytes());
					print.println(message);
					
//					String str="";
//					String cmd="";
//
//					byte[] rbytes = new byte[1024];
					
					/*
					 * 这里循环可以使服务器持续的接收客户端信息。
						read.readLine()通过输入流读取一段字符串，
						赋值给message变量，如果message字符串不为“exit”则循环，
						否则结束循环
					 *
					 */
					while (!(message = read.readLine()).equals("exit")){
						//返回打印数据
						print.println("request is:" + message);

						//Read Stream
//						byte bbyte = (byte)in.read();
//						System.out.printf("\tread 0x%02X 0d%04d \n", bbyte,bbyte);
////						System.out.printf("\t0xaa 0x%02X 0d%04d \n", 0xaa,0xaa);
//
//						//启始位 s
//						//终止位 e
//						if((char)bbyte=='s'){
//							for(int i=0;i<1024;i++){
//								bbyte=(byte) in.read();
//								
//								if((char)bbyte=='e'){
//									break;
//								}
//								rbytes[i] = bbyte;
//							}
//						}
							
//						System.out.flush();
						
//						in.read(rbytes,0,12);
//						System.out.println("request:"+new String(rbytes,"GBK"));
						
/*
						if(bt==(byte)0xAA){
							System.out.println("客户端查询数据库...");
							System.out.println("sql ruest:"+str);
							
							bt = (byte)in.read();
							
							System.out.printf("\tcomammd 0x%02X 0d%3d", bt,bt);
							
							byte hSize=(byte)in.read();
							byte lSize=(byte)in.read();
							
							//System.out.printf("\thSize 0x%02X lSize 0x%02X", hSize,lSize);
							
							//System.out.printf("\thSize<<8 0x%04X", ((int)hSize<<8)+lSize);

							System.out.printf("\tSize 0x%04X 0d%3d", ((int)(hSize<<8)+lSize),((int)(hSize<<8)+lSize));

							for(i=0;i<((int)(hSize<<8)+lSize);i++){
								bt = (byte)in.read();
								System.out.printf("\tdate 0x%02X 0d%3d", bt,bt);
							}
							
							bt = (byte)in.read();
							System.out.printf("\tend 0x%02X 0d%3d", bt,bt);

							System.out.println();
						} else {

							if(bt==13){
								cmd +=(char)bt;
								bt = (byte)in.read();
								if(bt==10){
									cmd +=(char)bt;
									System.out.println("cmd ="+cmd);
									cmd = "";
								}
							} else {
								cmd +=(char)bt;
							}
							//out.write(cmd.getBytes());
							//out.flush();
							
							if (cmd.equals("search")) {
								System.out.println("客户端查询数据库...");
								out.write(str.getBytes());
							}
							
							if (cmd.equals("bye")) {
								System.out.println("客户端下线！");
								in.close();
								out.close();
								socket.close();
								threadPool.remove(this);
								break;
							}
						} 
						*/
						//Write Stream
						/*
						byte[] bytes = new byte[1024];
						int readSize=0;
						
						while (socket.isConnected()) {
							
							readSize = System.in.read(bytes,0,readSize);
							
							str = new String(bytes).toString();
							str = str.substring(0,readSize);
							
							socket.getOutputStream().write(str.getBytes());
							
							str= str.substring(0,readSize-2);
							
							if (str.equals("bye")) {
								threadPool.remove(this);
								System.out.println("服务器退出！");
								System.exit(1);
							}
						}
						*/
					}//end while
					
				} catch (Exception e) {
					e.printStackTrace();
					try {
						in.close();
						out.close();
						
						if(!socket.isClosed())
							socket.close();
						
						threadPool.remove(this);

						System.out.println("客户失去连接...");
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}//catch end
			}//run end
		}
	
	public static void main(String[] args) throws IOException {
		new WineServer(0,0);
	}
}