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

import com.mooo.mycoz.socket.sfserver.ActionFactory;

public class SFServer{
	private static Log log = LogFactory.getLog(SFServer.class);

//	private static Object initLock = new Object();

	private Vector<Thread> threadPool;
	private int maxConnMSec;
	
	private ServerSocket sSocket;
	private static int SERVICE_PORT = 8000;
	
	public SFServer(int maxConns,double maxConnTime) throws IOException {
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
			if(log.isErrorEnabled()) log.error("InterruptedException:" + e.getMessage());
		} catch (Exception e) {
			if(log.isErrorEnabled()) log.error("Exception:" + e.getMessage());
		} finally {
			if(log.isDebugEnabled()) log.debug("sSocket close");
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
			this.socket.setKeepAlive(true);
			this.socket.setSoTimeout(30*60*1000);//keep 30 minutes timeout
			
			in = socket.getInputStream();
			out = socket.getOutputStream();

			read = new BufferedReader(new InputStreamReader(in,"GBK"));  
			print = new PrintStream(out,true,"GBK");
		}

		public void run() {
			if(log.isDebugEnabled()) log.debug(socket.getRemoteSocketAddress()+"accept...");

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
					if(log.isDebugEnabled()) log.debug("requestLine:"+requestLine);
					
					String response = ActionFactory.getInstance().forward(requestLine);
					//打印响应数据
					if(log.isDebugEnabled()) log.debug("response:" + response);
					if(response.equals("Unsafe")){
						print.println("警告:请不要尝试任何攻击,我们将会做出法律回应!");
						print.flush();
						if(log.isDebugEnabled()) log.debug("检测到攻击:" + socket.getRemoteSocketAddress());
						
						throw new Exception("Unsafe");
					}

					print.println(response);
					print.flush();
					//wait input
					//print.print(">");
				}//end while
				
			} catch (Exception e) {
				if(log.isErrorEnabled()) log.error("服务器异常 Exception:" + e.getMessage());
			} finally {
				try {
					if(log.isDebugEnabled()) log.debug("主动关闭连接");
					
					if( in !=null)
						in.close();
					if( out !=null)
						out.close();
					if(!socket.isClosed())
						socket.close();
					
					threadPool.remove(this);
					
					if(log.isDebugEnabled()) log.debug("成功关闭连接");
				} catch (IOException e) {
					if(log.isErrorEnabled()) log.error("客户失去连接 自动关闭连接 Exception:" + e.getMessage());
				}
			}
			if(log.isDebugEnabled()) log.debug(socket.getRemoteSocketAddress()+" close...");
		}//run end
	}
	
	public static void main(String[] args) throws IOException {
		new SFServer(0,0);
	}
}
