package com.example.jetty.resources;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;

import org.jboss.resteasy.annotations.GZIP;

import com.example.jetty.model.HttpApiResponse;
import com.example.services.UserService;
import com.example.utils.HttpResponceCode;

import net.arnx.jsonic.JSON;

/**
 * api about info of user and action of user
 * 
 * @author tamnb
 *
 */
@Path("/users") // prefix url
public class UserResource {

	@Path("/{uId}/videos/{videoId}/like") // uri is /users/{uId}/videos/{videoId}/like
	@POST // Method HTTP
	@GZIP // if client send with header Accept-Encoding:gzip , </br>
			// returned data will compressed with gzip with </br>
	// and returned header has field Content-Encoding:gzip </br>
	// some library in client can auto decompress
	public String actionLikeVideo(@PathParam("uId") long uId, @PathParam("videoId") String videoId,
			@Context HttpServletRequest req, @Context HttpServletResponse res) {
		boolean check = UserService.likeVideo(videoId, uId);
		if (!check)
			return JSON.encode(new HttpApiResponse(HttpResponceCode.SUCCESS, "success", null));
		else
			return JSON.encode(
					new HttpApiResponse(HttpResponceCode.SERVER_ERROR, "Error when processing in server", null));

	}

}
