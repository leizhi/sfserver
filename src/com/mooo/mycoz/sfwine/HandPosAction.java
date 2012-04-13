package com.mooo.mycoz.sfwine;

public class HandPosAction implements Action{

	
	public MessageReturn forward(String request) {
		PosImpl pos=new PosImpl();
		MessageReturn msg=new MessageReturn();
		String[] args = request.split(" +\n*");
		
		if(args==null || args.length <1 || args[0].length()!=1){
			msg.setFlag(false);
			msg.setMessage("没有输入参数");
			return msg;
		}
		int cmd = (byte) Integer.parseInt(args[0]);
		
		switch(cmd){
		case Action.PROCESS_LOGIN:
			if(args.length !=3){
				msg.setFlag(false);
			    msg.setMessage("参数不正确");
		    }
	    	msg=pos.processLogin(args[1], args[2]);
			break;
		case Action.SEARCH_CARD:
			if(args.length !=2){
				msg.setFlag(false);
			    msg.setMessage("参数不正确");
		    }
			
			msg=pos.searchCard(args[1]);
			break;
		case Action.ACTION_EXIT:
			msg=pos.loginOut(Integer.parseInt(args[1]));
			break;
		default:
			break;
		}
		System.out.println("length:"+args.length);
		for(int i=0;i<args.length;i++){
			System.out.println(args[i]);
		}
		return msg;
	}
	public MessageReturn forward1(String request) {
		
		MessageReturn msg=new MessageReturn();
//		line = "1      leizhi     leizhi    \n";
		String[] args = request.split(" +\n*");
		
		if(args==null || args.length <1 || args[0].length()>1){
			msg.setFlag(false);
			msg.setMessage("没有输入参数");
			return msg;
		}
			
		
		byte[] cmd = args[0].getBytes();
		if(cmd==null || cmd.length > 1){
			msg.setFlag(false);
			msg.setMessage("没有输入命令");
			return msg;
	    }
		
		byte dcmd = cmd[0];
		PosImpl pos=new PosImpl();
		switch(dcmd){
			case Action.PROCESS_LOGIN:
				if(args.length !=3){
					msg.setFlag(false);
				    msg.setMessage("参数不正确");
			    }
				
		    	msg=pos.processLogin(args[1], args[2]);
				break;
			case Action.SEARCH_CARD:
				if(args.length !=2){
					msg.setFlag(false);
				    msg.setMessage("参数不正确");
			    }
				
//				factory.searchCard(args);
				msg=pos.searchCard(args[1]);
				break;
			case Action.ACTION_EXIT:
				break;
			default:
				break;
		}
		System.out.println("length:"+args.length);
		for(int i=0;i<args.length;i++){
			System.out.println(args[i]);
		}
		return msg;
	}

}