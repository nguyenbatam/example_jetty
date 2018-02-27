package com.example.database.common;

public enum ESubcriber {
	test(0),statistic(1);

	private final int value;

	private ESubcriber(int value) {
		this.value = value;
	}

	public int getValue() {
		return this.value;
	}

	public static ESubcriber getEnum(int value) {
		switch (value) {

		case 1:
			return statistic;
		}

		return test;
	}
}
