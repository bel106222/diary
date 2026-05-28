package com.example.diary.repository;

import com.example.diary.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {
    // Все записи, отсортированные по дате создания (сначала новые)
    List<Record> findAllByOrderByCreatedDesc();

    // Записи конкретного дневника
    List<Record> findByDiaryIdOrderByCreatedDesc(Long diaryId);

    // Поиск по тексту описания (LIKE)
    @Query("SELECT r FROM Record r WHERE LOWER(r.recorddescription) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY r.created DESC")
    List<Record> searchByDescription(@Param("query") String query);
}