package com.omar.application;

import java.security.Key;
import java.util.HashSet;
import java.util.Set;

import javax.crypto.KeyGenerator;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import com.omar.jwtauth.JWTTokenNeededFilter;
import com.omar.services.AuthenticationService;
import com.omar.services.DocumentService;
import com.omar.services.EnseignantService;
import com.omar.services.MatiereService;
import com.omar.services.TacheService;

@ApplicationPath("/api")
public class TirageApplication extends Application {
	
	private Set<Object> singletons = new HashSet<Object>();
	private Set<Class<?>> classes = new HashSet<Class<?>>();
	
	public static final Key secretKey;
	
	static {
		KeyGenerator keyGen = null;
		try {
			keyGen = KeyGenerator.getInstance("HmacSHA512");
		} catch (Exception e) {
			e.printStackTrace();
		}
		secretKey = keyGen.generateKey();
	}

	public TirageApplication() {
		
		this.classes.add(AuthenticationService.class);
		this.classes.add(EnseignantService.class);
		this.classes.add(TacheService.class);
		this.classes.add(MatiereService.class);
		this.classes.add(DocumentService.class);
		
		this.singletons.add(new JWTTokenNeededFilter());
	}

	@Override
	public Set<Class<?>> getClasses() {
		return classes;
	}
	
	@Override
	public Set<Object> getSingletons() {
		return singletons;
	}
}
