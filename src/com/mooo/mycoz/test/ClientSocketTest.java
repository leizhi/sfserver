package com.mooo.mycoz.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.PrintStream;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ClientSocketTest {
	private static Log log = LogFactory.getLog(ClientSocketTest.class);
	
	private static Object initLock = new Object();

	private Vector<Thread> threadPool;
	private int maxConnMSec;
	
	private static int WACTCH_PORT = 8000;

	//扫描主线程
	public ClientSocketTest(String host,int maxConns,double maxConnTime) throws IOException {
		
		if(log.isDebugEnabled())log.debug("监控启动");
		
		threadPool = new Vector<Thread>(maxConns);

		maxConnMSec = (int) (maxConnTime * 86400000.0); // 86400 sec/day
		if (maxConnMSec < 30000 && maxConnMSec > 0) { // Recycle no less than 30 seconds.
			maxConnMSec = 30000;
		}
		
		try {
			//生成子线程
			for(int i=0;i<maxConns;i++){
				Thread thread = new Thread(new ScanThread(host,WACTCH_PORT));
				thread.start();
				
				Thread.sleep(5000);//wait
				threadPool.add(thread);
			}

			//主线程进入监控模式
			boolean forever = true;
			while (forever) {
				try {
					////
					int runCount = 0;

					for (Thread threadObj : threadPool) {
						if (threadObj.isAlive())
							runCount++;
					}
					if(log.isDebugEnabled())log.debug(">>>>>>>>>>>运行线程数:"+ runCount);
					System.out.println(">>>>>>>>>>>运行线程数:"+ runCount);

//					if (runCount == 0) {
//						break;
//					}
					
					synchronized (initLock) {
//						wait(1000*30); //30 seconds
						Thread.sleep(5000); // sleep 5 seconds.
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				if(log.isDebugEnabled())log.debug("<<<<<<<<<<<<LOOP Watch======");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("Exception:" + e.getMessage());
			if(log.isDebugEnabled())log.debug("Exception:" + e.getMessage());
		}
	}
	
	//send command
	private static String COMMAND_TEST = "*1sa,root#";

	// --- ScanThread 站点扫描线程
	class ScanThread implements Runnable {
		private String host;
		private int port;
		
		private long createTime;

		public ScanThread(String host,int port) {
			this.host = host;
			this.port = port;

			createTime = System.currentTimeMillis();
		}

		public void run() {
			Socket socket = null;
			OutputStream out = null;
			InputStream in = null;
			BufferedReader read = null;
			PrintStream print = null;
			
			String buffer = null;
			
			long finishTime = 0l;
			long hours = 0l;
			long minutes = 0l;
			long seconds = 0l;
			
			long expendsTime = 0l;
			
			long startTime = System.currentTimeMillis();
				try {
					socket = new Socket();
//					socket.getChannel().open();
//					Connects this socket to the server.
					socket.connect(new InetSocketAddress(host, port),6000);//建立连接最多等待6s
//					socket.setKeepAlive(true);
					socket.setSoTimeout(3000);//time out 3s
//					socket.setSoLinger(true, 1000);
					
					in = socket.getInputStream();
					out = socket.getOutputStream();
					read = new BufferedReader(new InputStreamReader(socket.getInputStream(),"GBK"));  
					print = new PrintStream(socket.getOutputStream(),true,"GBK");
					
					finishTime = System.currentTimeMillis();
					expendsTime = finishTime - startTime;
					if(log.isDebugEnabled())log.debug("\thost:" + host + "\t port:" + port + "\t expendsTime:"+expendsTime+"\t 连接成功!");
					
					boolean forever = true;
					while (forever) {
					
					// write do
					finishTime = System.currentTimeMillis();
					expendsTime = finishTime - startTime;
					if(log.isDebugEnabled())log.debug("write begin expendsTime:"+expendsTime);
					
					print.println(COMMAND_TEST);

					finishTime = System.currentTimeMillis();
					expendsTime = finishTime - startTime;
					if(log.isDebugEnabled())log.debug("write end/read begin expendsTime:"+expendsTime+"\t in.available():"+in.available());

					// read do
					if(buffer!=null)
					buffer=read.readLine().trim();

					finishTime = System.currentTimeMillis();
					hours = (finishTime - startTime) / 1000 / 60 / 60;
					minutes = (finishTime - startTime) / 1000 / 60 - hours * 60;
					seconds = (finishTime - startTime) / 1000 - hours * 60 * 60 - minutes * 60;
					
					expendsTime = finishTime - startTime;
					if(log.isDebugEnabled())log.debug("expendsTime:"+expendsTime);
					if(log.isDebugEnabled())log.debug("host:" + host + "\t port:" 
							+ port +"\t expends:   " + hours + ":" + minutes + ":"	
							+ seconds+"\t expendsTime:"+expendsTime);
					
					
					// do end for timeout
					long age = System.currentTimeMillis() - createTime;
					if(log.isDebugEnabled())log.debug("age:"+age+"\tmaxConnMSec:"+maxConnMSec);

					Thread.sleep(500);
					
					if (age > maxConnMSec && maxConnMSec > 0) { // Force a reset at the max
						System.out.println("===超时 退出=====");
						break;
					}

				}//loop while
					
				} catch (UnknownHostException e) {
					e.printStackTrace();
					if(log.isDebugEnabled())log.debug("host:" + host + "\t port:" + port + "主机未找到!");
				} catch (SocketException e) {
					e.printStackTrace();
					if(log.isDebugEnabled())log.debug("host:" + host + "\t port:" + port + "建立连接失败!");
				} catch (IOException e) {
					e.printStackTrace();
					if(log.isDebugEnabled())log.debug("IOException"+e.getMessage());
				} catch (Exception e) {
					e.printStackTrace();
					if(log.isDebugEnabled())log.debug("Exception"+e.getMessage());
				} finally {
					
					try {
						if(out!=null)
							out.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					try {
						if(in!=null)
							in.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					try {
						if(socket!=null)
							socket.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					//clear 
					buffer = null;
					
					socket = null;
					out = null;
					in = null;
				}//end try
				
				if(log.isDebugEnabled())log.debug("host:" + host + "\t port:" + port + "连接完成!");

		} // end run
	}
	
	public static void main(String[] args) {
		try {
			new ClientSocketTest("122.225.88.87",200,0);//0 no limit
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
