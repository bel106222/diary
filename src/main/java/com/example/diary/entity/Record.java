package com.example.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "records")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Автор записи
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    // Дневник, в котором находится запись
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diaryid", nullable = false)
    private Diary diary;

    @Column(nullable = false, length = 5000)
    private String recorddescription;

    @Column(nullable = false)
    private LocalDateTime created;

    // Прикреплённые файлы
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileAttachment> files;

    @PrePersist
    public void prePersist() {
        this.created = LocalDateTime.now();
    }
}