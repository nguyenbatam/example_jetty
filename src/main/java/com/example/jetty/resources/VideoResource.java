package com.example.jetty.resources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import org.jboss.resteasy.annotations.GZIP;

import com.example.jetty.model.HttpApiResponse;
import com.example.services.VideoService;
import com.example.services.model.ListVideosResponse;
import com.example.utils.HttpResponceCode;

import net.arnx.jsonic.JSON;

/**
 * all api about get list video & video info
 * 
 * @author tamnb
 *
 */
@Path("/videos")
public class VideoResource {

	@Path("") // uri only /videos?last=?&length=?&viewId=?
	@GET // Method HTTP
	@GZIP // if client send with header Accept-Encoding:gzip , </br>
	// returned data will compressed with gzip with </br>
	// and returned header has field Content-Encoding:gzip </br>
	// some library in client can auto decompress
	public String getTopViewVideos(@DefaultValue("-1") @QueryParam("last") long last,
			@DefaultValue("20") @QueryParam("length") int length, @DefaultValue("") @QueryParam("viewId") long viewId,
			@Context HttpServletRequest req, @Context HttpServletResponse res) {
		ListVideosResponse listVideosResponse = VideoService.getTopLikeVideosByScore(last, length, viewId);
		if (listVideosResponse == null)
			return JSON.encode(
					new HttpApiResponse(HttpResponceCode.SERVER_ERROR, "Error when processing in server", null));
		else
			return JSON.encode(new HttpApiResponse(HttpResponceCode.SUCCESS, "success", listVideosResponse));
	}

}