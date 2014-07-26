package com.mooo.mycoz.socket.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mooo.mycoz.socket.handpos.ActionFactory;

public class SFServer{
	private static Log log = LogFactory.getLog(SFServer.class);
	
	private static int SERVICE_PORT = 8000;
	private static int SERVICE_BACKLOG = 1024;

	private long[] threadPool;
		
	public SFServer(){
		Thread thread = new Thread(new SwitchThread(SERVICE_BACKLOG));
		thread.start();
	}
	
	public class SwitchThread implements Runnable {
		int maxConns = SERVICE_BACKLOG;
		
		public SwitchThread(int maxConns){
			this.maxConns = maxConns;
			threadPool = new long[maxConns];

			for(int i=0;i< maxConns;i++){
				threadPool[i]=0;
			}
		}
		
		public void run() {
			if(log.isDebugEnabled()) log.debug("服务器启动");
			
			try {
				ServerSocket sSocket = new ServerSocket(SERVICE_PORT,SERVICE_BACKLOG);
				
				//主线程进入轮询模式
				boolean forever = true;
				while (forever) {
					try {
						//计算连接数
						int freeConns = 0;		
						for(int i=0;i< threadPool.length;i++){
							if (threadPool[i]==0){
								freeConns++;
							}
						}
						if(log.isDebugEnabled())log.debug("运行线程数:"+(maxConns-freeConns));

						if(freeConns==0){
//							throw new Exception("线程池满");
							if(log.isDebugEnabled())log.debug("线程池满:");
//							Thread.sleep(20);//wait 20ms
						}

						if(log.isDebugEnabled())log.debug("可用线程数:"+freeConns);

						if(freeConns>0) new SessionThread(sSocket.accept());
						
					} catch (Exception e) {
						if(log.isErrorEnabled()) log.error("主线程服务异常:" + e.getMessage());
					}
					if(log.isDebugEnabled())log.debug("<<<<<<<<<<<<LOOP Watch======");
				}//Loop End
		
			if(log.isDebugEnabled()) log.debug("sSocket close at:"+new Date());
			sSocket.close();
		} catch (Exception e) {
			if(log.isErrorEnabled()) log.error("服务器结束于异常:" + e.getMessage());
		} finally {
			if(log.isErrorEnabled()) log.error("服务器停止于:"+new Date());
		}
		}
	}
	/*
	 * 访问子线程
	 */
	class SessionThread implements Runnable {
		private Thread runner;

		private Socket socket;
		private OutputStream out;
		private InputStream in;

		private BufferedReader read = null;  
		private PrintStream print = null;
		
		public SessionThread(Socket socket) throws IOException {
			this.socket = socket;
			this.socket.setKeepAlive(true);
			this.socket.setSoTimeout(3*60*1000);//keep 3 minutes timeout
			
			in = socket.getInputStream();
			out = socket.getOutputStream();

			read = new BufferedReader(new InputStreamReader(in,"GB18030")); 
			print = new PrintStream(out,true,"GB18030");
			
			// Fire up the background housekeeping thread

			runner = new Thread(this);
			
			for(int i=0;i< threadPool.length;i++){
				if (threadPool[i]==0){
					threadPool[i] = runner.getId();
					break;
				}
			}
			
			runner.start();
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
						//print.flush();
						if(log.isDebugEnabled()) log.debug("检测到攻击:" + socket.getRemoteSocketAddress());
						
						throw new Exception("Unsafe");
					}

					print.println(response);
					//print.flush();
					//wait input
					//print.print(">");
				}//end while
				
			} catch (Exception e) {
				if(log.isErrorEnabled()) log.error("服务器线程异常:" + e.getMessage());
			} finally {
				try {
					if(log.isDebugEnabled()) log.debug("服务器线程主动关闭连接");
					
					if( in !=null)
						in.close();
					if( out !=null)
						out.close();
					if(!socket.isClosed())
						socket.close();
					
					for(int i=0;i< threadPool.length;i++){
						if (threadPool[i] == runner.getId()){
							threadPool[i] = 0;
							break;
						}
					}
					
					if(log.isDebugEnabled()) log.debug("服务器线程成功关闭连接");
				} catch (IOException e) {
					if(log.isErrorEnabled()) log.error("客户失去连接 自动关闭连接 Exception:" + e.getMessage());
				}
			}
			if(log.isDebugEnabled()) log.debug("服务线程主动关闭客户端:"+socket.getRemoteSocketAddress()+" close at:"+new Date());
		}//run end
	}
	
	public static void main(String[] args){
		new SFServer();
	}
}
