package com.mooo.mycoz.sfwine;

public interface Action {
	

	
   public final static byte PROCESS_LOGIN = 0x01;
	
	public final static byte SEARCH_CARD = 0x02;
	
	public final static byte ACTION_EXIT = (byte) 0xEE;
	
    
	MessageReturn forward(String request);
}