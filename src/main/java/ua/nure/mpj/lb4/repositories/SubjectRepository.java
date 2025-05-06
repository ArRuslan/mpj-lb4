package ua.nure.mpj.lb4.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.Subject;

import java.util.Optional;

public interface SubjectRepository extends JpaRepository<Subject, Long> {
    Optional<Subject> findByShortNameEquals(String name);
}
