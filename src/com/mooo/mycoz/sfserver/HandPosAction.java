package com.mooo.mycoz.sfserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mooo.mycoz.common.StringUtils;
import com.mooo.mycoz.db.pool.DbConnectionManager;

public class HandPosAction implements Action {
	
	/**
	 * 
	 */
	private static Log log = LogFactory.getLog(HandPosAction.class);

	private static final String EXISTS_USER="SELECT count(*) FROM User WHERE name=?";

	private static final String LOGIN="SELECT id,name,branchId FROM  User WHERE  name=? AND password=?";

	private static final String ADD_CARD_PATROL_LOG="SELECT id,name,branchId FROM  User WHERE  name=? AND password=?";
	
//	private static final String QUERY_CARD="SELECT id,name,branchId FROM  User WHERE  name=? AND password=?";

	public int processLogin(String userName,String password){
		if(log.isDebugEnabled()) log.debug("processLogin");	
        int RET=0;
        
		Connection connection=null;
        PreparedStatement pstmt = null;
        int count=0;
        try {
    		if(log.isDebugEnabled()) log.debug("processLogin getName:"+userName);	
    		if(log.isDebugEnabled()) log.debug("processLogin getPassword:"+password);	

    		if(StringUtils.isNull(userName)){
    			RET =-1;
    			throw new NullPointerException("请输入用户名");
    		}
    		
    		if(StringUtils.isNull(password)){
    			RET =-2;
    			throw new NullPointerException("请输入密码");
    		}
			connection = DbConnectionManager.getConnection();
			pstmt = connection.prepareStatement(EXISTS_USER);
            pstmt.setString(1, userName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
            if(count < 1){
            	RET =-3;
    			throw new NullPointerException("无此用户");
            }
            count=0;
            
            pstmt = connection.prepareStatement(LOGIN);
            pstmt.setString(1, userName);
            pstmt.setString(2, StringUtils.hash(password));

            rs = pstmt.executeQuery();
            while (rs.next()) {
            	rs.getInt(1);
            	rs.getString(2);
            	rs.getInt(3);

            	count=1;
            }
            
    		if (log.isDebugEnabled()) log.debug("count:"+count);

            if(count !=1){
            	RET =-4;
    			throw new NullPointerException("用户和密码不匹配");
            }
		}catch (NullPointerException e) {
			if(log.isErrorEnabled()) log.error("NullPointerException:"+e.getMessage());	
		}catch (SQLException e) {
			if(log.isErrorEnabled()) log.error("SQLException:"+e.getMessage());	
	   }finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(connection != null)
					connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return RET;
	}
	
	public void cardPatrol(Integer cardId,Integer userId){
		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			pstmt = conn.prepareStatement(ADD_CARD_PATROL_LOG);
//			int cardId = IDGenerator.getNextID("Card");
			pstmt.setInt(1, cardId);
			pstmt.execute();
			
			conn.commit();
			if(log.isDebugEnabled()) log.debug("save finlsh");
		} catch (Exception e) {
			System.out.println("CardDBObject Exception="+e.getMessage());
			try {
				conn.rollback();
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			
		}finally{

			try {
				pstmt.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

		}
	}
	
	public int processLogout(String userName,String password){
		return 0;
	}
	
	public String forward(String requestLine) {
//		String[] args = request.split(" +\n*");
		String response = null;
		String message = null;

		try{
			if(!requestLine.startsWith("*")||!requestLine.endsWith("#")){
				message = "数据格式不正确";
			}
			
			String doRequest=requestLine.substring(requestLine.indexOf("*")+1,
					requestLine.lastIndexOf("#"));

		    String[] args=doRequest.split(",");
		    
		    System.out.println("length:"+args.length);
		    
			for(int i=0;i<args.length;i++){
				args[i]=args[i].trim();
				System.out.println(args[i]);
			}
			
			int cmd = Integer.parseInt(args[0]);//命令
			int result = 0;//执行结果

			 switch(cmd){
				case Action.PROCESS_LOGIN:
					if(args.length !=3){
						message = "参数不正确";
				    }
					
					result = processLogin(args[1], args[2]);

					if(result!=0){
						response = "*"+result+","+message+"#";
					}else{
						response = "*"+result+"#";
					}
					
					break;
				case Action.SEARCH_CARD:
					if(args.length !=1){
						message = "参数不正确";
				    }
					
	//				cardPatrol(args[0],args[0],args[0]);
					break;
				default:
					break;
			   }
		}catch(Exception e){
			message = "数据格式不正确";
		}
		
		return response;
	}
}