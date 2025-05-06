package ua.nure.mpj.lb4.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb4.entities.Group;

import java.util.Optional;

public interface GroupRepository extends JpaRepository<Group, Long> {
    Optional<Group> findByNameEquals(String name);
}
