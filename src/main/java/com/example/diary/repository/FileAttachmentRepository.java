package com.example.diary.repository;

import com.example.diary.entity.FileAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface FileAttachmentRepository extends JpaRepository<FileAttachment, Long> {

    //Декларируем метод для поиска файлов у заданной записи дневника.
    List<FileAttachment> findByRecordId(Long recordId);
}