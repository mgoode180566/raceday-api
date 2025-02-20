package dev.mgoode.raceday_api.users.controller;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import dev.mgoode.raceday_api.users.model.User;
import dev.mgoode.raceday_api.users.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {
	
	@Value("${security.jwt.secret}")
	String secretKey;
	
	@Value("${security.jwt.issuer}")
	String issuer;
	
	@Value("${security.jwt.expiration}")
	Long tokenExpiration;
	
	UserService userService;
	
	AuthenticationManager authenticationManager;
	
	public UserController(UserService userService, AuthenticationManager authenticationManager) {
		this.userService = userService;
		this.authenticationManager = authenticationManager;
	}
	
	@PostMapping("/register")
	public ResponseEntity<Object> registerUser(@RequestBody User user, BindingResult result) {
		if (result.hasErrors()) {
			var errorList = result.getAllErrors();
			var errorsMap = new HashMap<String, String>();
			for( int i = 0; i < errorList.size() -1; i++) {
				FieldError fieldError = (FieldError) errorList.get(i);
				errorsMap.put( fieldError.getField(), fieldError.getDefaultMessage());
			}
			return ResponseEntity.badRequest().body(errorsMap);
		}
		try {
			UUID id =userService.registerUser(user);
		} catch (Exception ex) {
			var errorsMap = new HashMap<String,String>();
			errorsMap.put("error saving user to db", ex.getMessage());
			return ResponseEntity.badRequest().body(errorsMap);
		}
		
		var response = new HashMap<String,Object>();
		response.put("token", createJwtUserToken(user));
		response.put("user", user);
		
		return ResponseEntity.ok(response);
	}
	
	@PostMapping("/login")
	public ResponseEntity<Object> loginUser(@RequestBody User appUser, BindingResult result) {
		if (result.hasErrors()) {
			var errorList = result.getAllErrors();
			var errorsMap = new HashMap<String, String>();
			for( int i = 0; i < errorList.size() -1; i++) {
				FieldError fieldError = (FieldError) errorList.get(i);
				errorsMap.put( fieldError.getField(), fieldError.getDefaultMessage());
			}
			return ResponseEntity.badRequest().body(errorsMap);
		}
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(appUser.getUserName(), appUser.getPassword()));
			
			User user = userService.findUserByName(appUser.getUserName());
			var response = new HashMap<String,Object>();
			response.put("token", createJwtUserToken(user));
			response.put("user", user);
			
			return ResponseEntity.ok(response);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ResponseEntity.badRequest().body("Bad username or password");
	}
	
	@GetMapping("/account")
	public ResponseEntity<Object> getAccount(Authentication authentication) {
		var response = new HashMap<String, Object>();
		response.put("UserName", authentication.getName());
		response.put("Authorities", authentication.getAuthorities());
		var user = userService.findUserByName(authentication.getName());
		response.put("User", user);
		
		return ResponseEntity.ok(response);
	}
	
	private String createJwtUserToken(User appUser) {
		Instant now = Instant.now();
		JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
			.issuer(issuer)
			.issuedAt(now)
			.expiresAt(now.plusSeconds(tokenExpiration))
			.subject(appUser.getUserName())
			.build();
		var encoder = new NimbusJwtEncoder(new ImmutableSecret<>(secretKey.getBytes()));
		var params = JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(), jwtClaimsSet);
		return encoder.encode(params).getTokenValue();
	}
}
