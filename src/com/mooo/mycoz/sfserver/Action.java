package com.mooo.mycoz.sfserver;

public interface Action {
	
	public final static byte ACTION_SYN = 1;

	public final static byte SEARCH_CARD = 2;//deprecated

	public final static byte ACTION_EXIT = 3;

	public final static byte SEARCH_WINERYS = 10;

	public final static byte SEARCH_CARD_TYPES = 11;

	public final static byte EXIST_CARD = 12;

	public final static byte NEXT_RFID_CODE = 13;

	public final static byte SAVE_CARD = 14;

	public final static byte ACTION_LOGIN = 80;
	
	public final static byte ACTION_EXIT1 = 81;

	String forward(String request);
}