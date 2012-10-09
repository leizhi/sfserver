package com.mooo.mycoz.socket;

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

import com.mooo.mycoz.sfserver.ActionFactory;

public class MyServer{
	private static Log log = LogFactory.getLog(MyServer.class);

//	private static Object initLock = new Object();

	private Vector<Thread> threadPool;
	private int maxConnMSec;
	
	private ServerSocket sSocket;
	private static int SERVICE_PORT = 8000;
	
	public MyServer(int maxConns,double maxConnTime) throws IOException {
		sSocket = new ServerSocket(SERVICE_PORT);

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
					if(log.isDebugEnabled()) log.debug(">>>>>>>>>>>运行线程数:"+ runCount);

//					synchronized (initLock) {
//						wait(1000*10); //10 seconds
//					}
					
					//处理客户端请求并生成子线程
					Thread thread = new Thread(new SessionThread(sSocket.accept()));
					thread.start();
//					thread.join();
					
					Thread.sleep(20);//wait 20ms
					threadPool.add(thread);
					if(log.isDebugEnabled())log.debug("<<<<<<<<<<<<LOOP Watch======");
			}//Loop End
		} catch (InterruptedException e) {
			e.printStackTrace();
			if(log.isDebugEnabled()) log.debug("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			if(log.isDebugEnabled()) log.debug("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
		} finally {
			sSocket.close();
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
			if(log.isDebugEnabled()) log.debug("SessionThread start...");

			try {
//				String message = "--- Welcome to this chatroom 欢迎---";
//				print.println(message);
//				print.print(">");
				String requestLine=null;
				
				boolean forever = true;
				while (forever){			
					requestLine = read.readLine();
					
					if(log.isDebugEnabled()) log.debug("requestLine:"+requestLine);

					if(requestLine==null || requestLine.equals("3")|| requestLine.equals("*3#"))
						break;
					
					requestLine = requestLine.trim();
					
					//打印请求数据
//					print.println("requestLine :" + requestLine);
					
					String response = ActionFactory.getInstance().forward(requestLine);
					//打印响应数据
//					print.println("response :" + response);
					print.println(response);
					print.flush();
					
					//wait input
					//print.print(">");
				}//end while
				
			} catch (Exception e) {
				e.printStackTrace();
				if(log.isDebugEnabled()) log.debug("客户失去连接...");
	    		if(log.isDebugEnabled()) log.debug("客户失去连接...");	

			} finally {
				try {
					if(log.isDebugEnabled()) log.debug("客户退出...");
					
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
			if(log.isDebugEnabled()) log.debug("SessionThread end...");
		}//run end
	}
	
	public static void main(String[] args) throws IOException {
		new MyServer(0,0);
	}
}