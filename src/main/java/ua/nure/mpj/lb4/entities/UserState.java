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
        UPDATE_GROUP,
        CREATE_SUBJECT,
        UPDATE_SUBJECT,
        CREATE_SCHEDULE,
        VIEW_SCHEDULE,
    }

    public enum State {
        WAITING_FOR_GROUP_NAME,
        WAITING_FOR_SUBJECT_NAME,
        WAITING_FOR_SUBJECT_SHORT_NAME,
        WAITING_FOR_SCHEDULE_GROUP_NAME,
        WAITING_FOR_SCHEDULE_SUBJECT_NAME,
        WAITING_FOR_SCHEDULE_DATE,
        WAITING_FOR_SCHEDULE_POSITION,
        WAITING_FOR_SCHEDULE_TYPE,
    }
}
