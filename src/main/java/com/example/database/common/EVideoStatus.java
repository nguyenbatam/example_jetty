package com.example.database.common;

public enum EVideoStatus {
	_nomal(0), _delete(1),_hidden(2);

	private final int value;

	private EVideoStatus(int value) {
		this.value = value;
	}

	public static EVideoStatus getEUserStatus(String value) {
		if (value == null)
			return _nomal;
		switch (value) {
		case "0":
			return _nomal;
		case "1":
			return _delete;
		case "2":
			return _hidden;
		}
		return _nomal;

	}

	public int getValue() {
		return this.value;
	}
}

