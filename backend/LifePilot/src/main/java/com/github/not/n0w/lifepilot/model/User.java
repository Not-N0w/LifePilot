package com.github.not.n0w.lifepilot.model;

import jakarta.persistence.*;
import lombok.Data;
import org.antlr.v4.runtime.misc.NotNull;

import java.util.List;


@Data
@Entity
@Table(name = "users")
public class User {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;

    @Column(name="name")
    private String name;

    @Column(name="gender")
    private String gender;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_tasks",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private List<Task> tasks;

    @Column(name = "usual_dialog_style")
    @Enumerated(EnumType.STRING)
    private DialogStyle usualDialogStyle = DialogStyle.BASE;


    public void removeTask(AiTaskType type) {
        if (tasks != null) {
            tasks.removeIf(t -> t.getName() == type);
        }
    }
}
