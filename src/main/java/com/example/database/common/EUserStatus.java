package com.example.database.common;

public enum EUserStatus {
	 nomarl(0), ban_account(1);

	private final int value;

	private EUserStatus(int value) {
		this.value = value;
	}

	public static EUserStatus getEUserStatus(String value) {
		if (value == null)
			return nomarl;
		switch (value) {
		case "0":
			return nomarl;
		case "1":
			return ban_account;
		}
		return nomarl;

	}

	public static EUserStatus getEUserStatus(int value) {
		if (value < 0)
			return nomarl;
		switch (value) {
		case 0:
			return nomarl;
		case 1:
			return ban_account;
		}
		return nomarl;

	}

	public int getValue() {
		return this.value;
	}
}
