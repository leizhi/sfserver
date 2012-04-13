package com.mooo.mycoz.sfwine;

public class ActionForward {

	public void forward(String request){
		String[] args = request.split(" +\n*");
		
		if(args==null || args.length <1 || args[0].length()!=1)
			return;
		
		byte[] cmd = args[0].getBytes();
		
		byte dcmd = cmd[0];
		switch(dcmd){
			case Action.PROCESS_LOGIN:
				if(args.length !=3)
					return;
				
				break;
			case Action.SEARCH_CARD:
				if(args.length !=2)
					return;
				
				break;
			default:
				break;
		}
		System.out.println("length:"+args.length);
		for(int i=0;i<args.length;i++){
			System.out.println(args[i]);
		}
	}
}
