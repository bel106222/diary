package com.example.diary.controller;

import com.example.diary.entity.*;
import com.example.diary.entity.Record;
import com.example.diary.repository.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

//Создание, просмотр, редактирование, удаление записей. Загрузка файлов.
@Controller
@RequestMapping("/diaries/{diaryId}/records")
public class RecordController {

    private final RecordRepository recordRepository;
    private final DiaryRepository diaryRepository;
    private final FileAttachmentRepository fileAttachmentRepository;
    private final String uploadDir;

    public RecordController(RecordRepository recordRepository,
                            DiaryRepository diaryRepository,
                            FileAttachmentRepository fileAttachmentRepository,
                            @Value("${file.upload-dir}") String uploadDir) {
        this.recordRepository = recordRepository;
        this.diaryRepository = diaryRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.uploadDir = uploadDir;
    }

    // Форма создания новой записи в дневнике
    @GetMapping("/new")
    public String newRecordForm(@PathVariable Long diaryId, Model model) {
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);
        if (diaryOpt.isEmpty()) return "redirect:/diaries";
        model.addAttribute("diary", diaryOpt.get());
        model.addAttribute("record", new Record());
        return "record-form";
    }

    // Сохранение новой записи
    @PostMapping
    public String createRecord(@PathVariable Long diaryId,
                               @RequestParam String recorddescription,
                               @RequestParam(required = false) MultipartFile[] files,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);
        if (diaryOpt.isEmpty()) return "redirect:/diaries";
        User user = (User) session.getAttribute("currentUser");
        Record record = new Record();
        record.setDiary(diaryOpt.get());
        record.setUser(user);
        record.setRecorddescription(recorddescription);
        recordRepository.save(record);

        // Сохраняем прикреплённые файлы
        if (files != null && files.length > 0) {
            saveFiles(record, files);
        }
        return "redirect:/diaries/" + diaryId;
    }

    // Просмотр одной записи
    @GetMapping("/{recordId}")
    public String viewRecord(@PathVariable Long diaryId,
                             @PathVariable Long recordId,
                             Model model,
                             HttpSession session) {
        Optional<Record> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;
        Record record = recordOpt.get();
        model.addAttribute("record", record);
        model.addAttribute("isAuthor", record.getUser().getId()
                .equals(((User) session.getAttribute("currentUser")).getId()));
        return "record-view";
    }

    // Форма редактирования (только автор)
    @GetMapping("/{recordId}/edit")
    public String editRecordForm(@PathVariable Long diaryId,
                                 @PathVariable Long recordId,
                                 Model model, HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        Optional<Record> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;
        Record record = recordOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!record.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав на редактирование");
            return "redirect:/diaries/" + diaryId;
        }
        model.addAttribute("record", record);
        model.addAttribute("diary", record.getDiary());   // <-- добавляем дневник
        return "record-form";
    }

    // Сохранение изменений записи
    @PostMapping("/{recordId}")
    public String updateRecord(@PathVariable Long diaryId,
                               @PathVariable Long recordId,
                               @RequestParam String recorddescription,
                               @RequestParam(required = false) MultipartFile[] files,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<Record> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;
        Record record = recordOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!record.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав");
            return "redirect:/diaries/" + diaryId;
        }
        record.setRecorddescription(recorddescription);
        recordRepository.save(record);
        if (files != null && files.length > 0) {
            saveFiles(record, files);
        }
        return "redirect:/diaries/" + diaryId + "/records/" + recordId;
    }

    // Удаление записи (только автор)
    @PostMapping("/{recordId}/delete")
    public String deleteRecord(@PathVariable Long diaryId,
                               @PathVariable Long recordId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<Record> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;
        Record record = recordOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!record.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав на удаление");
            return "redirect:/diaries/" + diaryId;
        }
        // Удаляем файлы с диска
        for (FileAttachment fa : record.getFiles()) {
            Path filePath = Paths.get(uploadDir, fa.getFilepath());
            try {
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
        }
        recordRepository.delete(record);
        return "redirect:/diaries/" + diaryId;
    }

    // Вспомогательный метод сохранения файлов
    private void saveFiles(Record record, MultipartFile[] files) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue;
                // Генерируем уникальное имя файла
                String originalFilename = file.getOriginalFilename();
                String ext = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : "";
                String newFilename = UUID.randomUUID().toString() + ext;
                Path targetPath = uploadPath.resolve(newFilename);
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                FileAttachment fa = new FileAttachment();
                fa.setRecord(record);
                fa.setFilepath(newFilename);
                fileAttachmentRepository.save(fa);
            }
        } catch (IOException e) {
            throw new RuntimeException("Ошибка сохранения файла", e);
        }
    }
}