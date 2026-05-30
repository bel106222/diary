package com.example.diary.repository;

import com.example.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // Все дневники отсортированы по дате создания (сначала новые)
    List<Diary> findAllByOrderByCreatedDesc();
}