package com.mooo.mycoz.sfwine;

public class PosImpl {


	public MessageReturn processLogin(String userName, String userPassword) {
		// TODO Auto-generated method stub
		MessageReturn mr=new MessageReturn();
		try {
			userName =java.net.URLEncoder.encode(userName,"UTF-8");
			userPassword=java.net.URLEncoder.encode(userPassword,"UTF-8");
			String[] str=new Myclient("yuan9315.org").doget("/posHandset?method=login&name="+userName+"&password="+userPassword);
			if("200".equals(str[0])){
		    	String message=str[1];
		    	String m[]=message.split("\r\n");
		    	/*if(m.length!=2){
		    		mr.setFlag(false);
		    		mr.setMessage("数据格式错误");
		    		return mr;
		    	}*/
		    	if("success".equals(m[0]))
		    	   mr.setFlag(true);
		    	else
		    		mr.setFlag(false);
		    	mr.setMessage(m[1]);
		    }
			else{
				mr.setFlag(false);
		    	mr.setMessage("error:"+str[0]);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mr;
	}


	public MessageReturn searchCard(String uuid) {
		MessageReturn mr=new MessageReturn();
		try {
			uuid =java.net.URLEncoder.encode(uuid,"UTF-8");
			String[] str=new Myclient("yuan9315.org").doget("/posHandset?method=query&&rfidcode="+uuid);
		    if("200".equals(str[0])){
		    	String message=str[1];
		    	String m[]=message.split("\r\n");
		    	/*if(m.length!=2){
		    		mr.setFlag(false);
		    		mr.setMessage("数据格式错误");
		    		return mr;
		    	}*/
		    	if("success".equals(m[0]))
		    	   mr.setFlag(true);
		    	else
		    		mr.setFlag(false);
		    	mr.setMessage(m[1]);
		    }
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mr;
	}
	public MessageReturn loginOut(Integer userId){
		MessageReturn mr=new MessageReturn();
		try {
			String[] str=new Myclient("yuan9315.org").doget("/sourceWine/posHandset?method=loginOut&&userId="+userId);
			 if("200".equals(str[0])){
		    	/*iString message=str[1];
		    	String m[]=message.split(",");
		    	f(m.length!=2){
		    		mr.setFlag(false);
		    		mr.setMessage("数据格式错误");
		    		return mr;
		    	}
		    	if("success".equals(m[0]))
		    	   mr.setFlag(true);
		    	else
		    		mr.setFlag(false);
		    	mr.setMessage(m[1]);*/
		    	mr.setFlag(true);
		    }
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return mr;
	}
}
