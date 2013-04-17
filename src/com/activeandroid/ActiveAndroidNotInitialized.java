package com.activeandroid;

public class ActiveAndroidNotInitialized extends RuntimeException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1500240619927185418L;

	public ActiveAndroidNotInitialized() {
		super(
				"ActiveAndroid must be initialized with ActiveAndroid#initialize before interacting with database");
	}
}
