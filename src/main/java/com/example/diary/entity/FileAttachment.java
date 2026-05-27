package com.example.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "files")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class FileAttachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recordid", nullable = false)
    private Record record;

    @Column(nullable = false)
    private String filepath;   // путь/имя файла относительно uploads

    @Column(nullable = false)
    private LocalDateTime attached;

    @PrePersist
    public void prePersist() {
        this.attached = LocalDateTime.now();
    }
}