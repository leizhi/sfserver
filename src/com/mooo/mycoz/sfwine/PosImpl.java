package com.mooo.mycoz.sfwine;

public class PosImpl {


	public MessageReturn processLogin(String userName, String userPassword) {
		// TODO Auto-generated method stub
		MessageReturn mr=new MessageReturn();
		try {
			String[] str=new Myclient("yuan9315.org").doget("/sourceWine/posHandset?method=login&name="+userName+"&password="+userPassword);
			if(str[0]=="200"){
		    	String message=str[1];
		    	String m[]=message.split(",");
		    	if(m.length!=2){
		    		mr.setFlag(false);
		    		mr.setMessage("数据格式错误");
		    		return mr;
		    	}
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


	public MessageReturn searchCard(String uuid) {
		MessageReturn mr=new MessageReturn();
		try {
			String[] str=new Myclient("yuan9315.org").doget("/sourceWine/posHandset?method=query&&rfidcode="+uuid);
		    if(str[0]=="200"){
		    	String message=str[1];
		    	String m[]=message.split(",");
		    	if(m.length!=2){
		    		mr.setFlag(false);
		    		mr.setMessage("数据格式错误");
		    		return mr;
		    	}
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
		    if(str[0]=="200"){
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
