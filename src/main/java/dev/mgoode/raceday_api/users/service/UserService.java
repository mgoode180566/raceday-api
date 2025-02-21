package dev.mgoode.raceday_api.users.service;

import dev.mgoode.raceday_api.users.model.User;
import dev.mgoode.raceday_api.users.repo.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
	
	UserRepository userRepository;
	
	public UserService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}
	
	public User findUserByName(String name) {
		return userRepository.findByUserName(name);
	}
	
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User appUser = userRepository.findByUserName(username);
		
		if (appUser != null) {
			return org.springframework.security.core.userdetails.User.withUsername(appUser.getUserName()).password(appUser.getPassword()).build();
		}
		return null;
	}
	
	public UUID registerUser(User user ) throws Exception {
		var bCryptEncoder = new BCryptPasswordEncoder();
		user.setPassword(bCryptEncoder.encode(user.getPassword()));
		user.setDateCreated(LocalDateTime.now());
		
		if (userRepository.findByUserName(user.getUserName()) != null) {
			throw new Exception("User name already exists");
		}
		
		try {
			return userRepository.save(user).getId();
		} catch (Exception ex) {
			throw new Exception(ex);
		}
	}
}
