package ua.nure.mpj.lb4.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ua.nure.mpj.lb4.entities.Subject;
import ua.nure.mpj.lb4.repositories.SubjectRepository;

import java.util.Optional;

@Service
public class SubjectService {
    private final Sort SORT_BY_ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    private final SubjectRepository subjectRepository;

    @Autowired
    public SubjectService(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    public Page<Subject> list(int page, int pageSize) {
        return subjectRepository.findAll(PageRequest.of(page, pageSize, SORT_BY_ID_ASC));
    }

    public Optional<Subject> get(long id) {
        return subjectRepository.findById(id);
    }

    public Subject save(Subject subject) {
        return subjectRepository.save(subject);
    }

    public void deleteById(long id) {
        subjectRepository.deleteById(id);
    }

    public long count() {
        return subjectRepository.count();
    }
}
