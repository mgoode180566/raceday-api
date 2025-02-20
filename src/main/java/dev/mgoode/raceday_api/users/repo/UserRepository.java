package dev.mgoode.raceday_api.users.repo;
import dev.mgoode.raceday_api.users.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
		User findByid(UUID id);
		User findByUserName(String name);
}