package com.mooo.mycoz.sfserver;

public interface Action {
	
	public final static byte PROCESS_LOGIN = 0x01;//deprecated

	public final static byte SEARCH_CARD = 0x02;//deprecated

	public final static byte ACTION_EXIT = 0x03;

	public final static byte ACTION_SYN = 0x04;

	String forward(String request);
}
