package ua.nure.mpj.lb4.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ua.nure.mpj.lb4.entities.UserState;
import ua.nure.mpj.lb4.repositories.UserStateRepository;

import java.util.Optional;

@Service
public class UserStateService {
    private final UserStateRepository userStateRepository;

    @Autowired
    public UserStateService(UserStateRepository userStateRepository) {
        this.userStateRepository = userStateRepository;
    }

    public Optional<UserState> get(long userId) {
        return userStateRepository.findById(userId);
    }

    public UserState save(UserState userState) {
        return userStateRepository.save(userState);
    }

    public void deleteById(long id) {
        userStateRepository.deleteById(id);
    }

    public UserState setState(long userId, UserState.Action action, UserState.State state, String data) {
        Optional<UserState> stateOpt = get(userId);
        if (stateOpt.isEmpty())
            return save(new UserState(userId, action, state, data));

        UserState userState = stateOpt.get();
        userState.setAction(action);
        userState.setState(state);
        userState.setData(data);

        return save(userState);
    }
}
