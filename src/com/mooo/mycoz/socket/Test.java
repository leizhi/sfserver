package com.mooo.mycoz.socket;

import com.mooo.mycoz.socket.sfserver.IDGenerator;

public class Test {

	public static void main(String[] args) {
		int wineryId = IDGenerator.getId("Winery", "definition", "腾龙酒业");
		System.out.println("wineryId:"+wineryId);
	}
}
