package dev.mgoode.raceday_api.event.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//@Entity
//@Table(name = "session")
public class Session {
	
	Id id;
	
	String sessionName;
	
	LocalDateTime startTime;
	
	List<Lap> laps;
	
	
	
	
	
}
