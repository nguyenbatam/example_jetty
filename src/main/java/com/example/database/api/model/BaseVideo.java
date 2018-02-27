package com.example.database.api.model;

import com.example.database.common.EVideoStatus;

public class BaseVideo {
	public String id = "";
	public String title = "";
	public long authorId = 0;
	public String description = "";
	public String cover = "";
	public long timestamp;
	public EVideoStatus videoStatus = EVideoStatus._nomal;
}

