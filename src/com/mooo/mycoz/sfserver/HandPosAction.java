package com.mooo.mycoz.sfserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;

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

	private static final String QUERY_CARD="SELECT card.rfidcode,wineJar.abbreviation,wineType.definition,wineLevel.definition,alcohol,volume,volumeUnit,material from Card card,WineJar wineJar,wineShared.WineType wineType,wineShared.WineLevel wineLevel WHERE wineJar.id=card.wineJarId AND wineJar.wineTypeId=wineType.id AND wineJar.wineLevelId=wineLevel.id AND card.rfidcode=?";

	private static final String ADD_CARD_PATROL_LOG="INSERT INTO CardJob(id,jobDate,cardId,userId,jobTypeId) VALUES(?,?,?,?,3)";

	public String processLogin(String userName,String password){
		if(log.isDebugEnabled()) log.debug("processLogin");	
		String response = "*";

		Connection connection=null;
        PreparedStatement pstmt = null;
        int count=0;
        try {
    		if(log.isDebugEnabled()) log.debug("processLogin getName:"+userName);	
    		if(log.isDebugEnabled()) log.debug("processLogin getPassword:"+password);	

    		if(StringUtils.isNull(userName)){
    			throw new NullPointerException("请输入用户名");
    		}
    		
    		if(StringUtils.isNull(password)){
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
    			throw new NullPointerException("用户和密码不匹配");
            }
            
    		response +="0";
		}catch (NullPointerException e) {
			response +="-1,"+e.getMessage();
			if(log.isErrorEnabled()) log.error("NullPointerException:"+e.getMessage());	
		}catch (SQLException e) {
			response +="-2,"+e.getMessage();
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
		return response += "#";
	}
	
	public String cardPatrol(String rfidcode,Integer userId){
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			pstmt = conn.prepareStatement(QUERY_CARD);
			pstmt.setString(1, rfidcode);
			
            ResultSet rs = pstmt.executeQuery();
            String str="";
            while (rs.next()) {
            	str += "标示号:"+rs.getString(1)+",";
            	str += "酒罐号:"+rs.getString(2)+",";
            	str += "酒香型:"+rs.getString(3)+",";
            	str += "酒等级:"+rs.getString(4)+",";
            	str += "酒精度:"+rs.getString(5)+"%,";
            	str += "容积:"+rs.getString(6)+",";
            	str += "单位:"+rs.getString(7)+",";
            	str += "原料:"+rs.getString(8);
            }
            
			pstmt = conn.prepareStatement(ADD_CARD_PATROL_LOG);
			int cardJobId = IDGenerator.getNextID("CardJob");
			pstmt.setInt(1, cardJobId);
			pstmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));

			int cardId = IDGenerator.getId("Card","rfidcode",rfidcode);
			pstmt.setInt(3, cardId);
			
			pstmt.setInt(4, userId);
			pstmt.execute();
			
			conn.commit();
			
    		response +="0,"+str;
			if(log.isDebugEnabled()) log.debug("save finlsh");
		} catch (Exception e) {
			response +="-1,"+e.getMessage();

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
		
		return response += "#";
	}
	
	public String cardPatrol(String cardId,String userId){
		return cardPatrol(cardId,new Integer(userId) );
	}
	
	public String forward(String requestLine) {
//		String[] args = request.split(" +\n*");
		String response = null;

		try{
			if(!requestLine.startsWith("*")||!requestLine.endsWith("#")){
				response = "数据格式不正确";
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

			 switch(cmd){
				case Action.PROCESS_LOGIN:
					if(args.length !=3){
						response = "参数不正确";
				    }
					
					response = processLogin(args[1], args[2]);
					break;
				case Action.SEARCH_CARD:
					if(args.length !=3){
						response = "参数不正确";
				    }
					
					response = cardPatrol(args[1],args[2]);
					
					break;
				default:
					break;
			   }
		}catch(Exception e){
			response = "数据格式不正确";
		}
		
		return response;
	}
}