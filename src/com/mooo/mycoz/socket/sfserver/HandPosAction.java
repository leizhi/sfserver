package com.mooo.mycoz.socket.sfserver;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.mooo.mycoz.common.CalendarUtils;
import com.mooo.mycoz.common.StringUtils;
import com.mooo.mycoz.db.pool.DbConnectionManager;

public class HandPosAction implements Action {
	
	/**
	 * 
	 */
	private static Log log = LogFactory.getLog(HandPosAction.class);

	private static final String QUERY_USER_ID="SELECT id FROM User WHERE name=?";

	private static final String QUERY_BRANCH_ID="SELECT branchId FROM User WHERE id=?";

	private static final String LOGIN="SELECT id,name FROM  User WHERE  name=? AND password=?";

	private static final String EXISTS_CARD="SELECT count(*) FROM Card WHERE uuid=?";
	
	private static final String REGISTER_CARD="SELECT COUNT(id) FROM Card WHERE rfidcode=? AND card.branchId=?";

	private static final String ACTIVATE_CARD="SELECT COUNT(card.id) FROM Card card,CardJob cardJob WHERE cardJob.cardId=card.id AND cardJob.branchId=card.branchId AND cardJob.jobTypeId=2 AND card.rfidcode=? AND card.branchId=?";

	private static final String SELECT_MAX_BY_LIKE="SELECT MAX(rfidcode) nowCode FROM Card WHERE rfidcode LIKE ?";

	private static final String QUERY_CARD="SELECT card.rfidcode,wineJar.abbreviation,wineType.definition,wineLevel.definition,alcohol,volume,volumeUnit,material,card.branchId FROM Card card,WineJar wineJar,wineShared.WineType wineType,wineShared.WineLevel wineLevel WHERE wineJar.id=card.wineJarId AND wineJar.wineTypeId=wineType.id AND wineJar.wineLevelId=wineLevel.id AND card.rfidcode=?";

	private static final String ADD_CARD="INSERT INTO Card(id,rfidcode,uuid,wineryId,wineJarId,branchId,processId,cardTypeId) VALUES(?,?,?,?,0,?,0,?)";

	private static final String ADD_CARD_JOB="INSERT INTO CardJob(id,jobDate,cardId,userId,jobTypeId,branchId,processId,spotNormal,cardNormal) VALUES(?,?,?,?,?,?,0,'Y','Y')";

	private static final String COUNT_PROCESS="SELECT COUNT(id) FROM CardJob WHERE cardId=? AND branchId=?";

	private static final String UPDATE_CARD_JOB="UPDATE CardJob SET processId=? WHERE cardId=? AND branchId=? AND processId=0";

	private static final String EXISTS_CARD_JOB="SELECT COUNT(id) FROM CardJob WHERE jobTypeId=3  AND processId=0 AND cardId=? AND branchId=? AND userId=? AND jobDate=?";

	//sfcomm call
	
	public int getUserId(String userName){
		Connection conn=null;
        PreparedStatement pstmt = null;
        int userId = -1;
        try {
    		if(StringUtils.isNull(userName)){
    			throw new NullPointerException("请输入用户名");
    		}
    		
			conn = DbConnectionManager.getConnection();
			pstmt = conn.prepareStatement(QUERY_USER_ID);
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
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return userId;
	}
	
	public int getBranchId(int userId){
		Connection conn=null;
        PreparedStatement pstmt = null;
        int branchId = -1;
        try {
			conn = DbConnectionManager.getConnection();
			pstmt = conn.prepareStatement(QUERY_BRANCH_ID);
            pstmt.setInt(1, userId);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	branchId = rs.getInt(1);
            }
		}catch (NullPointerException e) {
			if(log.isErrorEnabled()) log.error("NullPointerException:"+e.getMessage());	
		}catch (SQLException e) {
			if(log.isErrorEnabled()) log.error("SQLException:"+e.getMessage());	
	   }finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return branchId;
	}
	
	public int processAuth(String userName,String password){
		Connection conn=null;
        PreparedStatement pstmt = null;
        int userId=-1;
        
        try {
        	if(StringUtils.isNull(userName)){
    			throw new NullPointerException("请输入用户名");
    		}
    		
    		if(StringUtils.isNull(password)){
    			throw new NullPointerException("请输入密码");
    		}
    		
			conn = DbConnectionManager.getConnection();
			
			pstmt = conn.prepareStatement(LOGIN);
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
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return userId;
	}
	
	public boolean existsPatrol(Integer cardId,Integer branchId,Integer userId,String dateTime){
		Connection conn=null;
        PreparedStatement pstmt = null;
        
        try {
			conn = DbConnectionManager.getConnection();
			
			pstmt = conn.prepareStatement(EXISTS_CARD_JOB);
            pstmt.setInt(1, cardId);
            pstmt.setInt(2, branchId);
            pstmt.setInt(3, userId);
            pstmt.setTimestamp(4, new Timestamp(CalendarUtils.dtparse(dateTime).getTime()));
            
            int count=-1;
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	count = rs.getInt(1);
            }
			
            if(log.isDebugEnabled()) log.debug("count:"+count);

            if(count>0) return true;
            
		}catch (NullPointerException e) {
			if(log.isErrorEnabled()) log.error("NullPointerException:"+e.getMessage());	
		}catch (SQLException e) {
			if(log.isErrorEnabled()) log.error("SQLException:"+e.getMessage());	
		}catch (Exception e) {
			if(log.isErrorEnabled()) log.error("SQLException:"+e.getMessage());	
	   
		}finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
		return false;
	}
	
	public int saveCardJob(String rfidcode,String userName,String dateTime) throws SQLException{
		Connection conn=null;
        PreparedStatement pstmt = null;
        int RET=-1;
        try {
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			int cardId = IDGenerator.getId("Card","rfidcode",rfidcode);
			if(cardId<0){
				RET = 1;
				throw new CardException("无此标签记录"); 
			}
			
			int userId=getUserId(userName);
			if(userId<0){
				RET = 3;
				throw new CardException("无此用户"); 
			}
			int branchId = getBranchId(userId);
			
			pstmt = conn.prepareStatement(ACTIVATE_CARD);
			pstmt.setString(1, rfidcode);
			pstmt.setInt(2, branchId);
			ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
            if(count<1){
				RET = 2;
            	throw new CardException("此标签未激活"); 
            }

			if(existsPatrol(cardId,branchId,userId,dateTime)){
				RET = 4;
				throw new CardException("此标签已上传"); 
			}

			if(log.isDebugEnabled()) log.debug("userName:"+userName);
			if(log.isDebugEnabled()) log.debug("userId:"+userId);
			
			pstmt = conn.prepareStatement(COUNT_PROCESS);
			pstmt.setInt(1, cardId);
			pstmt.setInt(2, branchId);
            rs = pstmt.executeQuery();
            int processId = 0;
            while (rs.next()) {
            	processId = rs.getInt(1);
            }
            
			pstmt = conn.prepareStatement(UPDATE_CARD_JOB);
			pstmt.setInt(1, processId);
			pstmt.setInt(2, cardId);
			pstmt.setInt(3, branchId);
			pstmt.execute();
			
			pstmt = conn.prepareStatement(ADD_CARD_JOB);
			int cardJobId = IDGenerator.getNextID("CardJob");
			
			if(log.isDebugEnabled()) log.debug("cardJobId:"+cardJobId);

			pstmt.setInt(1, cardJobId);
			pstmt.setTimestamp(2, new Timestamp(CalendarUtils.dtparse(dateTime).getTime()));
			pstmt.setInt(3, cardId);
			pstmt.setInt(4, userId);
			pstmt.setInt(5, 3);
			pstmt.setInt(6, branchId);
			pstmt.execute();
			
			conn.commit();
			
			RET=0;
        }catch (CardException e) {
			conn.rollback();
			if(log.isErrorEnabled()) log.error("CardException:"+e.getMessage());
		}catch (Exception e) {
			conn.rollback();
			if(log.isErrorEnabled()) log.error("Exception:"+e.getMessage());
			RET=3;
	   }finally {
			conn.setAutoCommit(true);
			
			if(pstmt != null)
				pstmt.close();
			if(conn != null)
				conn.close();
		}
        return RET;
	}
	
	public String synchronize(String userName,String userPassword,String buffer){
		String response = "";
		
		try{
			if(getUserId(userName)<0)
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
	
	public String cardPatrol(String userName,String userPassword,String rfidcode) throws SQLException{
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
            
			int userId=processAuth(userName,userPassword);
			if(userId<0){
				throw new CardException("无此用户"); 
			}
			int branchId = getBranchId(userId);
			
			pstmt = conn.prepareStatement(REGISTER_CARD);
			pstmt.setString(1, rfidcode);
			pstmt.setInt(2, branchId);
            ResultSet rs = pstmt.executeQuery();
			int count = 0;
           
			while (rs.next()) {
            	count = rs.getInt(1);
            }
            if(count<1){
            	throw new CardException("此标签未授权"); 
            }
            
			pstmt = conn.prepareStatement(ACTIVATE_CARD);
			pstmt.setString(1, rfidcode);
			pstmt.setInt(2, branchId);
            rs = pstmt.executeQuery();
            count = 0;
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
            if(count<1){
            	throw new CardException("此标签未激活"); 
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
            	str += "原料:"+rs.getString(8);            
            }
			
			pstmt = conn.prepareStatement(ADD_CARD_JOB);
			
			int cardJobId = IDGenerator.getNextID("CardJob");
			if(log.isDebugEnabled()) log.debug("cardJobId:"+cardJobId);

			pstmt.setInt(1, cardJobId);
			pstmt.setTimestamp(2, new Timestamp(Calendar.getInstance().getTimeInMillis()));
			if(log.isDebugEnabled()) log.debug("rfidcode:"+rfidcode);

			int cardId = IDGenerator.getId("Card","rfidcode",rfidcode);
			if(log.isDebugEnabled()) log.debug("cardId:"+cardId);
			if(cardId<0){
				throw new NullPointerException("无此卡记录"); 
			}
			
			pstmt.setInt(3, cardId);
			pstmt.setInt(4, processAuth(userName,userPassword));
			pstmt.setInt(5, 3);
			pstmt.setInt(6, branchId);
			pstmt.execute();

			conn.commit();
			
    		response +="0,"+Action.SEARCH_CARD+","+str;

			if(log.isDebugEnabled()) log.debug("save finlsh");
		} catch (Exception e) {
			response +="1,"+e.getMessage();
			conn.rollback();
			
			if(log.isDebugEnabled()) log.debug("CardDBObject Exception="+e.getMessage());
			e.printStackTrace();
		}finally{
			conn.setAutoCommit(true);
			if(pstmt != null)
				pstmt.close();
			if(conn != null)
				conn.close();
		}
		
		return response += "#";
	}
	
	//Card
	public String saveCard(String userId,String rfidcode,String uuid,String wineryName,String cardTypeName) throws SQLException{
		if(log.isDebugEnabled()) log.debug("save Card start");
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;
		try{
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			pstmt = conn.prepareStatement(ADD_CARD);
			long cardId = IDGenerator.getNextID("Card");
			pstmt.setLong(1, cardId);
			pstmt.setString(2, rfidcode);
			pstmt.setString(3, uuid);

			int wineryId = IDGenerator.getId("Winery", "definition", wineryName);
			pstmt.setInt(4, wineryId);
			
			Integer lId=new Integer(userId);
			int branchId = getBranchId(lId);
			pstmt.setInt(5, branchId);
			
			int cardTypeId = IDGenerator.getId("wineShared.CardType", "cardTypeName", cardTypeName);
			pstmt.setInt(6, cardTypeId);
			pstmt.execute();
			
			pstmt = conn.prepareStatement(ADD_CARD_JOB);
			
			int cardJobId = IDGenerator.getNextID("CardJob");
			
			pstmt.setLong(1, cardJobId);
			pstmt.setTimestamp(2, new Timestamp(new Date().getTime()));
			pstmt.setLong(3, cardId);
			pstmt.setLong(4, lId);
			pstmt.setInt(5, 1);
			pstmt.setInt(6, branchId);
			pstmt.execute();
			
			conn.commit();
			if(log.isDebugEnabled()) log.debug("save finlsh");
    		
			response +="0;"+Action.SAVE_CARD;
		} catch (Exception e) {
			conn.rollback();
			response +="1;"+e.getMessage();

			e.printStackTrace();
		}finally{
			conn.setAutoCommit(false);
			
			if(pstmt != null)
				pstmt.close();
			if(conn != null)
				conn.close();
		}
		if(log.isDebugEnabled()) log.debug("response finlsh"+response);

		return response += "#";
	}
	
	private static final String EXISTS_USER="SELECT count(*) FROM User WHERE name=?";
	
	public static String processLogin(String userName,String userPassWord) {
		String response = "*";

		Connection conn=null;
        PreparedStatement pstmt = null;
        int count=0;
        try {

    		if(StringUtils.isNull(userName))
    			throw new NullPointerException("请输入用户名");
    		
    		if(StringUtils.isNull(userPassWord))
    			throw new NullPointerException("请输入密码");
    		
			conn = DbConnectionManager.getConnection();
			pstmt = conn.prepareStatement(EXISTS_USER);
            pstmt.setString(1, userName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
            if(count < 1)
    			throw new NullPointerException("无此用户");
            
            count=0;
            
            pstmt = conn.prepareStatement(LOGIN);
            pstmt.setString(1, userName);
            pstmt.setString(2, userPassWord);

            rs = pstmt.executeQuery();
        	int userId = -1;
            while (rs.next()) {
            	userId = rs.getInt(1);
            }
            
            if(userId < 0)
    			throw new NullPointerException("用户和密码不匹配");
            
            response +="0;"+Action.ACTION_LOGIN+";"+userId;
		}catch (Exception e) {
			response +="1;"+e.getMessage();
			
			e.printStackTrace();
	   }finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}

        return response += "#";
	}
	
	private static final String ADD_USER_INFO="INSERT INTO UserInfo(id,uuid) VALUES(?,?)";

	private static final String ADD_USER="INSERT INTO User(id,name,password,userInfoId,branchId) VALUES(?,?,?,?,1)";

	public void processRegister(String userName,String userPassWord,String serialNumber) throws SQLException{
		if(log.isDebugEnabled()) log.debug("processRegister");	

		Connection conn=null;
        PreparedStatement pstmt = null;
        long count=0;
        try {
			conn = DbConnectionManager.getConnection();
			conn.setAutoCommit(false);
			
			pstmt = conn.prepareStatement(EXISTS_USER);
            pstmt.setString(1, userName);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	count = rs.getInt(1);
            }
            
    		if(log.isDebugEnabled()) log.debug("count:"+count);	

            if(count > 0) throw new NullPointerException("此用户已注册");
            
            pstmt = conn.prepareStatement(EXISTS_CARD);
            pstmt.setString(1, StringUtils.hash(serialNumber));
            
            rs = pstmt.executeQuery();
            while (rs.next()) {
            	count = rs.getInt(1);
            }
    		if(log.isDebugEnabled()) log.debug("count:"+count);	

            if(count > 0) throw new NullPointerException("此卡已注册");
            
            pstmt = conn.prepareStatement(ADD_USER_INFO);
            int userInfoId = IDGenerator.getNextID("UserInfo");
            pstmt.setLong(1, userInfoId);
            pstmt.setString(2, StringUtils.hash(serialNumber));
            pstmt.execute();
            
            pstmt = conn.prepareStatement(ADD_USER);
            pstmt.setLong(1, IDGenerator.getNextID("UserInfo"));
            pstmt.setString(2, userName);
            pstmt.setString(3, StringUtils.hash(userPassWord));
            pstmt.setInt(4, userInfoId);
            pstmt.execute();

            conn.commit();
		}catch (NullPointerException e) {
			conn.rollback();
			if(log.isErrorEnabled()) log.error("NullPointerException:"+e.getMessage());	
		}catch (SQLException e) {
			conn.rollback();
			if(log.isErrorEnabled()) log.error("SQLException:"+e.getMessage());	
		}catch (Exception e) {
			conn.rollback();
			if(log.isErrorEnabled()) log.error("SQLException:"+e.getMessage());	
		}finally {
			conn.setAutoCommit(false);

			if(pstmt != null)
				pstmt.close();
			if(conn != null)
				conn.close();
		}
	}
	
	public static String getKey(String value) {
		
		Connection conn=null;
        PreparedStatement pstmt = null;
        String result = null;
        try {
        	String sql = "SELECT abbreviation FROM Winery WHERE definition=?";
        	
			conn = DbConnectionManager.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, value);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	result = rs.getString(1);
            }
		}catch (SQLException e) {
			e.printStackTrace();
	   }finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
        return result;
	}
	
	public static String nextRfidCode(String winery) {
		String response = "*";

		String nextCode=null;
		
		String wineryCode=null;
		wineryCode = getKey(winery);
		
		if(log.isDebugEnabled()) log.debug("wineryCode:"+wineryCode);

		if(wineryCode==null || wineryCode.length()>6){
			wineryCode="000000";
		}else if(wineryCode.length()>0 && wineryCode.length()<6){
			for(int i=wineryCode.length();i<6;i++){
				wineryCode +="0";
			}
		}
		
		String nowDate = CalendarUtils.dformat2(Calendar.getInstance().getTime());
		
		if(nowDate==null || nowDate.length()!=6)
			nowDate="000000";
		
		String prefix = wineryCode+nowDate;
		
		String nextNumber = "0000";
		
		Connection conn=null;
        PreparedStatement pstmt = null;
        try {
			conn = DbConnectionManager.getConnection();
            pstmt = conn.prepareStatement(SELECT_MAX_BY_LIKE);
            pstmt.setString(1, prefix+"%");
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
            	nextCode = rs.getString(1);
            }
            
            if(nextCode!=null && nextCode.length()==16){
            	nextNumber = nextCode.substring(12);
            	
            	int number = new Integer(nextNumber);
            	
            	number++;
            	
            	if(number<10){
            		nextNumber = "000"+number;
            	}else if(number<100){
            		nextNumber = "00"+number;
            	}else if(number<1000){
            		nextNumber = "0"+number;
            	}else{
            		nextNumber = ""+number;
            	}
            }
            
            nextCode = prefix+nextNumber;
			
            response +="0;"+Action.NEXT_RFID_CODE+";"+nextCode;
		}catch (Exception e) {
			response +="1;"+e.getMessage();
			e.printStackTrace();
	   }finally {
			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
		}
        return response += "#";
	}
	
	public String existCard(String  uuid){
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try{
			conn = DbConnectionManager.getConnection();
			pstmt = conn.prepareStatement(EXISTS_CARD);
			pstmt.setString(1, uuid);
			
			ResultSet result = pstmt.executeQuery();
			int count = 0;
			
			if(result.next()){
				count = result.getInt(1);
			}
			
			if(log.isErrorEnabled()) log.error("count:"+count);	

			if(count > 0) throw new NullPointerException("此卡已注册");
           
			response +="0;"+Action.EXIST_CARD;
		} catch (Exception e) {
			response +="1;"+e.getMessage();
			System.out.println("Exception="+e.getMessage());
		}finally{

			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
        return response += "#";
	}
	
	private static final String SEARCH_WINERYS="SELECT w.definition FROM Winery w,WineryMap wm WHERE wm.wineryId=w.id AND wm.userId=? ORDER BY w.id";

	public String searchWinerys(String  userId){
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try{
			conn = DbConnectionManager.getConnection();
			pstmt = conn.prepareStatement(SEARCH_WINERYS);
			pstmt.setInt(1, new Integer(userId));
			
			ResultSet result = pstmt.executeQuery();
			
			String winerys="";
			while(result.next()){
				
				if(winerys.equals(""))
					winerys = ";"+result.getString(1);
				else
					winerys += ","+result.getString(1);
			}

			response +="0;"+Action.SEARCH_WINERYS+winerys;
			
		} catch (Exception e) {
			response +="1;"+e.getMessage();
			System.out.println("Exception="+e.getMessage());
		}finally{

			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
        return response += "#";
	}
	
	private static final String SEARCH_CARD_TYPES="SELECT cardTypeName FROM wineShared.CardType ORDER BY id";

	public String searchCardTypes(){
		String response = "*";

		Connection conn = null;
		PreparedStatement pstmt = null;

		try{
			conn = DbConnectionManager.getConnection();
			pstmt = conn.prepareStatement(SEARCH_CARD_TYPES);
			ResultSet result = pstmt.executeQuery();
			
			String cardTypes="";
			while(result.next()){
				
				if(cardTypes.equals(""))
					cardTypes = ";"+result.getString(1);
				else
					cardTypes += ","+result.getString(1);
			}

			response +="0;"+Action.SEARCH_CARD_TYPES+cardTypes;
			
		} catch (Exception e) {
			response +="1;"+e.getMessage();
			System.out.println("Exception="+e.getMessage());
		}finally{

			try {
				if(pstmt != null)
					pstmt.close();
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		
        return response += "#";
	}
	
	public String forward(String requestLine) {
//		String[] args = request.split(" +\n*");
		String response = null;
		try{
			if(requestLine==null || !requestLine.startsWith("*")||!requestLine.endsWith("#")){
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
				case Action.EXIST_CARD:
					if(args.length !=2){
						response = "参数不正确";
				    }
					
					response = existCard(args[1]);
					break;
				case Action.NEXT_RFID_CODE:
					if(args.length !=2){
						response = "参数不正确";
				    }
					
					response = nextRfidCode(args[1]);
					break;
				case Action.SAVE_CARD:
					if(args.length !=6){
						response = "参数不正确";
				    }
					
					response = saveCard(args[1],args[2],args[3],args[4],args[5]);

					break;
				case Action.SEARCH_CARD_TYPES:
					if(args.length !=1){
						response = "参数不正确";
				    }
					
					response = searchCardTypes();

					break;
				case Action.SEARCH_WINERYS:
					if(args.length !=2){
						response = "参数不正确";
				    }
					
					response = searchWinerys(args[1]);
					
					break;
				case Action.ACTION_LOGIN:
					if(args.length !=3){
						response = "参数不正确";
				    }
					
					response=processLogin(args[1],args[2]);
					
					break;
				default:
					break;
			   }
		}catch(Exception e){
			response = "请求处理异常";
			e.printStackTrace();
		}
		
		return response;
	}
}