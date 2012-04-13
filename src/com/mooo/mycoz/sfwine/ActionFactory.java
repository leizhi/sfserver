package com.mooo.mycoz.sfwine;

import com.mooo.mycoz.db.DbConfig;

public class ActionFactory {
	private static Object initLock = new Object();
	
	private static String className = "com.mooo.mycoz.sfwine.HandPosAction";
	private static Action factory = null;
	
	public static Action getInstance() {
		if (factory == null) {
			synchronized (initLock) {
				if (factory == null) {
					String classNameProp = DbConfig.getProperty("SFServer.action");
					if (classNameProp != null) {
						className = classNameProp;
					}
					try {
						// Load the class and create an instance.
						Class<?> c = Class.forName(className);
						factory = (Action) c.newInstance();
					} catch (Exception e) {
						System.err.println("Failed to load ForumFactory class "+ className+ ". Yazd cannot function normally.");
						e.printStackTrace();
						return null;
					}
				}
			}
		}
		return factory;
	}
}
