package ua.nure.mpj.lb4.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserState {
    @Id
    private long userId;
    private Action action;
    private State state;
    private String data;

    public enum Action {
        CREATE_GROUP,
    }

    public enum State {
        WAITING_FOR_GROUP_NAME,
    }
}
