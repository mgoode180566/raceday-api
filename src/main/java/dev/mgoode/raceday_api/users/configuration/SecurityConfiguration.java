package dev.mgoode.raceday_api.users.configuration;

import dev.mgoode.raceday_api.users.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.spec.SecretKeySpec;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	
	@Value("${security.jwt.secret}")
	String jwtSecretKey;
	
	@Bean
	public SecurityFilterChain securityFilterChain( HttpSecurity httpSecurity ) throws Exception {
		return httpSecurity.csrf(csrf -> csrf.disable())
						.authorizeHttpRequests(auth -> auth
						.requestMatchers("/users/register").permitAll()
						.requestMatchers("/users/login").permitAll()
						.requestMatchers("/users/home").permitAll()
						.anyRequest().authenticated()
					)
					.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
					.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
					.build();
	}
	
	@Bean
	public JwtDecoder getJwt() {
		var secretKey = new SecretKeySpec(jwtSecretKey.getBytes(), "");
		return NimbusJwtDecoder.withSecretKey(secretKey).macAlgorithm(MacAlgorithm.HS256).build();
	}
	
	@Bean
	public AuthenticationManager authenticationManager(UserService userService) {
		DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
		daoAuthenticationProvider.setUserDetailsService(userService);
		daoAuthenticationProvider.setPasswordEncoder(new BCryptPasswordEncoder());
		return new ProviderManager(daoAuthenticationProvider);
	}
}
