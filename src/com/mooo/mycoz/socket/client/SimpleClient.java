package com.mooo.mycoz.socket.client;

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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SimpleClient {
	private static Log log = LogFactory.getLog(SimpleClient.class);

	//send command
	private static String COMMAND_TEST = "*1sa,root#";
	
	public SimpleClient(String host,int port){

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
//				socket.getChannel().open();
//				Connects this socket to the server.
				socket.connect(new InetSocketAddress(host, port),1000*60*60*12);//建立连接最多等待6s
//				socket.setKeepAlive(true);
				socket.setSoTimeout(1000*60*60*12);//time out 3s
//				socket.setSoLinger(true, 1000);
				
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
					if(log.isDebugEnabled())log.debug("read:"+read);
					if(log.isDebugEnabled())log.debug("buffer:"+buffer);
					if(log.isDebugEnabled())log.debug("readLine:"+read.readLine());
					buffer=read.readLine();
                    if(buffer!=null) buffer=buffer.trim();
					System.out.println(buffer);
					if(log.isDebugEnabled())log.debug("readLine buffer:"+buffer);

					finishTime = System.currentTimeMillis();
					hours = (finishTime - startTime) / 1000 / 60 / 60;
					minutes = (finishTime - startTime) / 1000 / 60 - hours * 60;
					seconds = (finishTime - startTime) / 1000 - hours * 60 * 60 - minutes * 60;
					
					expendsTime = finishTime - startTime;
					
					if(log.isDebugEnabled())log.debug("expendsTime:"+expendsTime);
					
					if(log.isDebugEnabled())log.debug("host:" + host + "\t port:" 
							+ port +"\t expends:   " + hours + ":" + minutes + ":"	
							+ seconds+"\t expendsTime:"+expendsTime);
					
					//Thread.sleep(500);
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
	}
	
	public static void main(String[] args) {
//		new SimpleClient("122.225.88.84",8000);//0 no limit
		new SimpleClient("127.0.0.1",8000);//0 no limit
	}
}
