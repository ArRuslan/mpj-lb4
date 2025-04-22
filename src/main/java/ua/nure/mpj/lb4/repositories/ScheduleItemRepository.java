package ua.nure.mpj.lb4.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.entities.ScheduleItem;

public interface ScheduleItemRepository extends JpaRepository<ScheduleItem, Long> {
    public Page<ScheduleItem> findAllByGroup(Group group, Pageable pageable);
}
