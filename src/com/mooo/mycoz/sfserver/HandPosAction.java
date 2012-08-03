package com.mooo.mycoz.sfserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mooo.mycoz.common.StringUtils;
import com.mooo.mycoz.db.pool.DbConnectionManager;
import com.mooo.mycoz.db.sql.AbstractSQL;

public class HandPosAction implements Action {
	
	/**
	 * 
	 */
	private static Log log = LogFactory.getLog(HandPosAction.class);

	private static final String EXISTS_USER="SELECT count(*) FROM User WHERE name=?";

	private static final String LOGIN="SELECT id,name,branchId FROM  User WHERE  name=? AND password=?";

	private static final String REGISTER_CARD="SELECT COUNT(id) FROM Card WHERE rfidcode=?";

	private static final String EXISTS_CARD="SELECT card.id FROM Card card,WineJar wineJar WHERE wineJar.id=card.wineJarId AND rfidcode=?";

	private static final String QUERY_CARD="SELECT card.rfidcode,wineJar.abbreviation,wineType.definition,wineLevel.definition,alcohol,volume,volumeUnit,material,card.branchId FROM Card card,WineJar wineJar,wineShared.WineType wineType,wineShared.WineLevel wineLevel WHERE wineJar.id=card.wineJarId AND wineJar.wineTypeId=wineType.id AND wineJar.wineLevelId=wineLevel.id AND card.rfidcode=?";

	private static final String ADD_CARD_PATROL_LOG="INSERT INTO CardJob(id,jobDate,cardId,userId,jobTypeId) VALUES(?,?,?,?,3)";

	public int getUserId(String userName){
		Connection connection=null;
        PreparedStatement pstmt = null;
        int userId=-1;
        
        try {
    		if(StringUtils.isNull(userName)){
    			throw new NullPointerException("请输入用户名");
    		}
    		
			connection = DbConnectionManager.getConnection();
			pstmt = connection.prepareStatement(EXISTS_USER);
            pstmt.setString(1, userName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	userId = rs.getInt(1);
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
		return userId;
	}
	
	public int processAuth(String userName,String password){
		Connection connection=null;
        PreparedStatement pstmt = null;
        int userId=0;
        
        try {
        	if(StringUtils.isNull(userName)){
    			throw new NullPointerException("请输入用户名");
    		}
    		
    		if(StringUtils.isNull(password)){
    			throw new NullPointerException("请输入密码");
    		}
    		
			connection = DbConnectionManager.getConnection();
			
			pstmt = connection.prepareStatement(LOGIN);
            pstmt.setString(1, userName);
            pstmt.setString(2, StringUtils.hash(password));
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	userId = rs.getInt(1);
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
		return userId;
	}
	
	public int saveCardJob(String rfidcode,String userName,String dateTime) throws ParseException{
		Connection connection=null;
        PreparedStatement pstmt = null;
        int RET=-1;
        try {
			connection = DbConnectionManager.getConnection();
			pstmt = connection.prepareStatement(ADD_CARD_PATROL_LOG);
			
			int cardId = IDGenerator.getId(connection,"Card","rfidcode",rfidcode);
			if(cardId<0){
				RET = 1;
				throw new CardException("无此卡记录"); 
			}
			
			if(!IDGenerator.enableCard(connection, rfidcode)){
				RET = 2;
				throw new CardException("此卡未激活"); 
			}
			
			int cardJobId = IDGenerator.getNextID(connection,"CardJob");
			pstmt.setInt(1, cardJobId);
			pstmt.setTimestamp(2, new Timestamp(AbstractSQL.dtformat.parse(dateTime).getTime()));
			pstmt.setInt(3, cardId);
			pstmt.setInt(4, getUserId(userName));
			pstmt.execute();
			
			RET=0;
			
        }catch (CardException e) {
			if(log.isErrorEnabled()) log.error("CardException:"+e.getMessage());
		}catch (Exception e) {
			if(log.isErrorEnabled()) log.error("Exception:"+e.getMessage());
			RET=3;
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
	
	public String synchronize(String userName,String userPassword,String buffer){
		String response = "";
		
		try{
			if(getUserId(userName)<1)
				throw new NullPointerException("*1#");//无此用户
			
			int userId = processAuth(userName,userPassword);
			
			if(userId<0)
				throw new NullPointerException("*2#");//登录验证失败
			
			String[] record=buffer.split("/");
			String rfid=null;
			int saveCardJob = -1;
			boolean isHead = true;
			
			for(int i=0;i<record.length;i++){
				record[i]=record[i].trim();
				
				String[] parameter = record[i].split(",");
				
				rfid = parameter[0].trim();
				saveCardJob = saveCardJob(parameter[1].trim(),parameter[2].trim(),parameter[3].trim());
				
				if(saveCardJob!=0){
					if(isHead){
						isHead=false;
						response += rfid+","+saveCardJob;
					}else{
						response += ";"+rfid+","+saveCardJob;
					}
				}
			}
			
			if(isHead){
				response = "*0#";
			}else{
				response = "*3;"+response+";#;#";
			}
			
		}catch(Exception e){
			response=e.getMessage();
		}
		
		return response;
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

		    String[] args=doRequest.split(";");
		    
		    if(log.isDebugEnabled()) log.debug("length:"+args.length);
		    
			for(int i=0;i<args.length;i++){
				args[i]=args[i].trim();
				if(log.isDebugEnabled()) log.debug(args[i]);
			}
			
			int cmd = Integer.parseInt(args[0]);//命令

			 switch(cmd){
				case Action.SEARCH_CARD:
					if(args.length !=4){
						response = "参数不正确";
				    }
					
					response = cardPatrol(args[1],args[2],args[3]);
					
					break;
				case Action.ACTION_SYN:
					
					if(args.length !=4){
						response = "参数不正确";
				    }
					
					response = synchronize(args[1],args[2],args[3]);
					
					break;
				default:
					break;
			   }
		}catch(Exception e){
			response = "数据格式不正确";
		}
		
		return response;
	}
	
	public String cardPatrol(String userName,String userPassword,String rfidcode){
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			pstmt = conn.prepareStatement(REGISTER_CARD);
			pstmt.setString(1, rfidcode);
            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
            if(count<1){
            	throw new NullPointerException("此标签未授权"); 
            }
            
			pstmt = conn.prepareStatement(EXISTS_CARD);
			pstmt.setString(1, rfidcode);
            rs = pstmt.executeQuery();
            count = 0;
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
            if(count<1){
            	throw new NullPointerException("此标签未激活"); 
            }
            
			pstmt = conn.prepareStatement(QUERY_CARD);
			pstmt.setString(1, rfidcode);
			
            rs = pstmt.executeQuery();
            String str="";
            while (rs.next()) {
            	str += "标示号:"+rs.getString(1)+",";
            	str += "酒罐号:"+rs.getString(2)+",";
            	str += "酒香型:"+rs.getString(3)+",";
            	str += "酒等级:"+rs.getString(4)+",";
            	str += "酒精度:"+rs.getString(5)+"%,";
            	str += "容积:"+rs.getString(6)+",";
            	str += "单位:"+rs.getString(7)+",";
            	str += "原料:"+rs.getString(8);            }
            
			pstmt = conn.prepareStatement(ADD_CARD_PATROL_LOG);
			
			int cardJobId = IDGenerator.getNextID(conn,"CardJob");
			if(log.isDebugEnabled()) log.debug("cardJobId:"+cardJobId);

			pstmt.setInt(1, cardJobId);
			pstmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			if(log.isDebugEnabled()) log.debug("rfidcode:"+rfidcode);

			int cardId = IDGenerator.getId(conn,"Card","rfidcode",rfidcode);
			if(log.isDebugEnabled()) log.debug("cardId:"+cardId);
			if(cardId<0){
				throw new NullPointerException("无此卡记录"); 
			}
			
			pstmt.setInt(3, cardId);
			pstmt.setInt(4, processAuth(userName,userPassword));
			pstmt.execute();
			
			conn.commit();
			
    		response +="0,"+Action.SEARCH_CARD+","+str;

			if(log.isDebugEnabled()) log.debug("save finlsh");
		} catch (Exception e) {
			response +="1,"+e.getMessage();

			if(log.isDebugEnabled()) log.debug("CardDBObject Exception="+e.getMessage());
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
}