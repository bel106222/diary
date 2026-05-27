package com.example.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // хранится в виде хеша BCrypt

    @Column(nullable = false)
    private LocalDateTime created;

    // Связь с дневниками (автор)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Diary> diaries;

    // Связь с записями (автор)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Record> records;

    @PrePersist
    public void prePersist() {
        this.created = LocalDateTime.now();
    }
}