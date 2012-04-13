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

public class SFServer extends ServerSocket {
	private static Log log = LogFactory.getLog(SFServer.class);

//	private static Object initLock = new Object();

	private Vector<Thread> threadPool;
	private int maxConnMSec;
	
//	protected static final int SERVER_PORT = Integer.valueOf(PropertyManager.getProperty("serverPort")).intValue();

	private static int SERVICE_PORT = 8000;
	
	public SFServer(int maxConns,double maxConnTime) throws IOException {
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
					if(log.isDebugEnabled())log.debug("<<<<<<<<<<<<LOOP Watch======");
			}//Loop End
		} catch (InterruptedException e) {
			e.printStackTrace();
			System.out.println("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
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
		private Integer userId=0;
		
		public SessionThread(Socket socket) throws IOException {
			this.socket = socket;

			in = socket.getInputStream();
			out = socket.getOutputStream();

			// 从Socket中获取输入流和输出流，由于我们只做一个简单的字符串通讯，所以采用BufferedRead和PrintStream来封装输入、输出流   
			read = new BufferedReader(new InputStreamReader(socket.getInputStream(),"GBK"));  
//			print = new PrintStream(socket.getOutputStream());
			print = new PrintStream(socket.getOutputStream(),true,"GBK");
		}

		public void run() {
			try {
				String message = "--- Welcome to this chatroom 欢迎---";
				print.println(message);
				print.print(">");
				/*
				 * 这里循环可以使服务器持续的接收客户端信息。
					read.readLine()通过输入流读取一段字符串，
					赋值给message变量，如果message字符串不为“exit”则循环，
					否则结束循环
				 *
				 */
				while (!(message = read.readLine()).equals("exit")){
					//返回打印数据
					print.println("response :" + message);

					if(message.equals("exit"))
					message="exit +\n*"+userId;
					
					MessageReturn msgr=ActionFactory.getInstance().forward(message);
					print.println("response :"+msgr.getFlag()+","+msgr.getMessage());
					
					//处理登陆操作的用户ID 
					if(msgr.getFlag()){
						String[] str=msgr.getMessage().split(",");
						if(str.length==3){
							userId=Integer.parseInt(str[0]);
						}
					}
					//wait input
					print.print(">");
				}//end while
				
			} catch (Exception e) {
				String message="exit +\n*"+userId;
				ActionFactory.getInstance().forward(message);
				e.printStackTrace();
				System.out.println("客户失去连接...");
			} finally {
				try {
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
		new SFServer(0,0);
	}
}