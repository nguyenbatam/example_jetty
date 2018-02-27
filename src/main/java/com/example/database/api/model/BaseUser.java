package com.example.database.api.model;

import com.example.database.common.EUserStatus;

public class BaseUser {
	public long id;
	public String name = "";
	public String avatar = "";
	public EUserStatus accountStatus = EUserStatus.nomarl;
}

