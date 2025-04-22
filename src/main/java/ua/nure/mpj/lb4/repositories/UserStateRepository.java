package ua.nure.mpj.lb4.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.UserState;

public interface UserStateRepository extends JpaRepository<UserState, Long> {
}
