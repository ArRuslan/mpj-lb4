package ua.nure.mpj.lb4.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb4.entities.Group;

public interface GroupRepository extends JpaRepository<Group, Long> {
}
