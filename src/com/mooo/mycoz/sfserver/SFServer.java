package com.mooo.mycoz.sfserver;

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
					
					if(requestLine==null || requestLine.equals("3")|| requestLine.equals("*3#"))
						break;
					
					requestLine = requestLine.trim();
					
					//打印请求数据
//					print.println("requestLine :" + requestLine);
					
					String response = ActionFactory.getInstance().forward(requestLine,userId);
					//打印响应数据
//					print.println("response :" + response);
					print.println(response);
					
					String value=null;
					String split = "*0,"+Action.PROCESS_LOGIN+",";
					if(response.indexOf(split)>-1){
						value = response.substring(split.length(),response.length()-1);
						System.out.println("value:"+value);
						if(value!=null && !value.equals("")){
							userId=new Integer(value);
						}
					}
					//wait input
					//print.print(">");
				}//end while
				
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("客户失去连接...");
			} finally {
				try {
					System.out.println("客户退出...");
					
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