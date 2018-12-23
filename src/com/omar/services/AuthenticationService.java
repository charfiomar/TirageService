package com.omar.services;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.omar.application.TirageApplication;
import com.omar.jwtauth.AuthenticationController;
import com.omar.jwtauth.JWTTokenNeeded;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@Stateless
public class AuthenticationService {

	@EJB
	private AuthenticationController authenticationController;
	
	private ObjectMapper mapper = new ObjectMapper();

	@POST
	@Path("/login")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public Response login(@FormParam("login") String login, @FormParam("password") String password) {

		JsonNode response;
		String token;
		String credentials = authenticationController.authentication(login, password);

		if (credentials != null) {
			response = mapper.createObjectNode();

			int id = Integer.parseInt(credentials.substring(1, credentials.indexOf("/")));
			((ObjectNode) response).put("id", id);

			if (credentials.substring(credentials.indexOf("/") + 1).equalsIgnoreCase("agent")) {
				((ObjectNode) response).put("isAgent", true);
			} else {
				((ObjectNode) response).put("isAgent", false);
			}
			((ObjectNode) response).put("message", "logged in successfully");

			token = createJWTToken(login);
		} else {
			ObjectMapper mapper = new ObjectMapper();
			response = mapper.createObjectNode();

			((ObjectNode) response).put("message", "login failure, wrong credentials");

			return Response.status(Response.Status.UNAUTHORIZED).entity(response).build();
		}

		return Response.ok().header(HttpHeaders.AUTHORIZATION, "Bearer " + token).entity(response).build();

	}

	@GET
	@JWTTokenNeeded
	@Path("test")
	public Response test() {
		return Response.ok().build();
	}

	private String createJWTToken(String login) {

		String jwtToken = Jwts.builder().setSubject(login).setIssuedAt(new Date())
				.setExpiration(toDate(LocalDateTime.now().plusMinutes(30L)))
				.signWith(TirageApplication.secretKey, SignatureAlgorithm.HS512).compact();

		return jwtToken;
	}

	private Date toDate(LocalDateTime localDateTime) {
		Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set(localDateTime.getYear(), localDateTime.getMonthValue() - 1, localDateTime.getDayOfMonth(),
				localDateTime.getHour(), localDateTime.getMinute(), localDateTime.getSecond());
		return calendar.getTime();
	}
}
