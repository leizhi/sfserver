package com.mooo.mycoz.test;


import java.net.URLEncoder;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;

public class Myclient 
{
	private HttpClient client;
	/**
	 * @param host
	 * @param port
	 * @param CONTENT_CHARSET
	 */
	public Myclient(String host,int port,String CONTENT_CHARSET)
	{
		this.client = new HttpClient();
		client.getHostConfiguration().setHost(host,port);
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, CONTENT_CHARSET);
	}
	/**
	 * @param host
	 * @param port
	 */
	public Myclient(String host,int port)
	{
		this.client = new HttpClient();
		client.getHostConfiguration().setHost(host,port);
	}
	/**
	 * @param host
	 */
	public Myclient(String host)
	{
		this.client = new HttpClient();
		client.getHostConfiguration().setHost(host,80);
	}
	/**
	 * @param host
	 * @param CONTENT_CHARSET
	 */
	public Myclient(String host,String CONTENT_CHARSET)
	{
		this.client = new HttpClient();
		client.getHostConfiguration().setHost(host,80);
		client.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, CONTENT_CHARSET);
	}
	/**
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String[] doget(String url) throws Exception
	{
		String [] back=new String[2];
		GetMethod get=new GetMethod(url);
		try
		{
			back[0]=String.valueOf(client.executeMethod(get));
			back[1]=get.getResponseBodyAsString();
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			get.releaseConnection();
		}
		return back;
	}
	/**
	 * @param url
	 * @param value
	 * @return
	 * @throws Exception
	 */
	public String[] dopost(String url,String[][] value) throws Exception
	{
		try
		{
			String[] back=new String[2];
			PostMethod post = new PostMethod(url);
			int n=value.length;
			NameValuePair values[]=new NameValuePair[n];
			for(int i=0;i<n;i++)
			{
				System.out.println(value[i][0]);
				System.out.println(value[i][1]);
				values[i]=new NameValuePair(value[i][0],value[i][1]);
				System.out.println(value[i][0]+" send ok");
			}
			post.setRequestBody(values);
			try
			{
				back[0]=String.valueOf(client.executeMethod(post));
				back[1]=post.getResponseBodyAsString();			
			}
			catch(Exception e)
			{
				throw e;
			}
			finally
			{
				post.releaseConnection();
			}
			return back;
		}
		catch(Exception e)
		{
			throw e;
		}
	}
	/**
	 * @param file
	 * @param url
	 * @return
	 * @throws Exception
	 */
	public String[] filepost(String[][] file,String url) throws Exception
	{
		String[] back=new String[2];
		MultipartPostMethod filePost = new MultipartPostMethod(url); 
		int n=file.length;
		for(int i=0;i<n;i++)
		{
			filePost.addParameter(file[i][0],file[i][1]); 			
		}
		client.getHttpConnectionManager(). getParams().setConnectionTimeout(5000); 
		try
		{
			back[0] = String.valueOf(client.executeMethod(filePost)); 
			back[2] = filePost.getResponseBodyAsString();
		}
		catch(Exception e)
		{
			throw e;
		}
		return back;
	}
	public void setProxy(String Proxy,int port)
	{
		client.getHostConfiguration().setProxy(Proxy,port); 
	}
	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
//		myclient client=new myclient("59.151.2.201",8102,"gb2312");
//		client.setProxy("121.10.119.10",80);
//		String[] back;
//		try {
//			for(int n=10800000;n<10800100;n++)
//			{
//				back=client.doget("msg.jsp?cmd=9&sid="+n);
//				System.out.print("|");
//				if(back[1].indexOf("δ������֤")!=-1)
//					continue;
//				System.out.print("״̬��"+back[0]);
//				System.out.println("���ݣ�"+back[1]);
//				System.out.println("��Ч��"+n);
//			}
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		String[][] temp = new String[3][2];
		temp[0][0] = "accountid";
		temp[0][1] = "10012";
		temp[1][0] = "to";
		temp[1][1] = "13980557395";
		temp[2][0] = "content";
		temp[2][1] = "12121";
		String ss=URLEncoder.encode("test");
		try {
			//System.out.print(new myclient("192.168.3.11","8088").doget("/sourceWine/posHandset?method=query&&rfidcode=4f9f45b4b58dae06")[0]);
			String[] str=new Myclient("yuan9315.org").doget("/sourceWine/posHandset?method=query&&rfidcode=4f9f45b4b58dae06");
			//String[] str=new Myclient("192.168.20.92").doget("/sourceWine/index.jsp");
			for(int i=0;i<str.length;i++){
				System.out.print(str[i]);
			}
			//System.out.print(str[0]);
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
