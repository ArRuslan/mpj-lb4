package ua.nure.mpj.lb4.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "subjects")
@Getter
@NoArgsConstructor
public class Subject {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Column(nullable = false, unique = true)
    private String name;

    @Setter
    @Column(nullable = false, unique = true)
    @JsonProperty(value = "short_name")
    private String shortName;

    public Subject(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }
}
