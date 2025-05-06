package ua.nure.mpj.lb4.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.nure.mpj.lb4.entities.Group;
import ua.nure.mpj.lb4.repositories.GroupRepository;

import java.util.Optional;

@Service
public class GroupService {
    private final Sort SORT_BY_ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    private final GroupRepository groupRepository;

    @Autowired
    public GroupService(GroupRepository groupRepository) {
        this.groupRepository = groupRepository;
    }

    public Page<Group> list(int page, int pageSize) {
        return groupRepository.findAll(PageRequest.of(page, pageSize, SORT_BY_ID_ASC));
    }

    public Optional<Group> get(long id) {
        return groupRepository.findById(id);
    }

    public Group save(Group group) {
        return groupRepository.save(group);
    }

    public void deleteById(long id) {
        groupRepository.deleteById(id);
    }

    public long count() {
        return groupRepository.count();
    }

    public Optional<Group> findByName(String name) {
        return groupRepository.findByNameEquals(name);
    }
}
