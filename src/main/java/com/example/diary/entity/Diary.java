package com.example.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "diaries")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String diaryname;

    // Автор дневника (только он сможет удалять/редактировать эту запись)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    @Column(nullable = false)
    private LocalDateTime created;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Record> records;

    @PrePersist
    public void prePersist() {
        this.created = LocalDateTime.now();
    }
}