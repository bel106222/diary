package com.example.diary.controller;

import com.example.diary.entity.Diary;
import com.example.diary.entity.User;
import com.example.diary.repository.DiaryRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

//Создание, просмотр, редактирование, удаление дневников.
@Controller
@RequestMapping("/diaries")
public class DiaryController {

    private final DiaryRepository diaryRepository;

    public DiaryController(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    // Список всех дневников
    @GetMapping
    public String listDiaries(Model model) {
        model.addAttribute("diaries", diaryRepository.findAllByOrderByCreatedDesc());
        return "diary-list";
    }

    // Форма создания дневника
    @GetMapping("/new")
    public String newDiaryForm() {
        return "diary-form";
    }

    // Сохранение нового дневника
    @PostMapping
    public String createDiary(@RequestParam String diaryname, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Diary diary = new Diary();
        diary.setDiaryname(diaryname);
        diary.setUser(user);
        diaryRepository.save(diary);
        return "redirect:/diaries";
    }

    // Просмотр одного дневника (со списком записей)
    @GetMapping("/{id}")
    public String viewDiary(@PathVariable Long id, Model model) {
        Optional<Diary> diaryOpt = diaryRepository.findById(id);
        if (diaryOpt.isEmpty()) return "redirect:/diaries";
        Diary diary = diaryOpt.get();
        model.addAttribute("diary", diary);
        model.addAttribute("records", diary.getRecords()); // уже отсортированы? нет, нужно сортировать
        // Но можно оставить как есть или отсортировать в шаблоне.
        return "diary-view";
    }

    // Форма редактирования дневника (доступна только создателю)
    @GetMapping("/{id}/edit")
    public String editDiaryForm(@PathVariable Long id, Model model, HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Optional<Diary> diaryOpt = diaryRepository.findById(id);
        if (diaryOpt.isEmpty()) return "redirect:/diaries";
        Diary diary = diaryOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!diary.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав на редактирование");
            return "redirect:/diaries";
        }
        model.addAttribute("diary", diary);
        return "diary-form";
    }

    // Сохранение изменений
    @PostMapping("/{id}")
    public String updateDiary(@PathVariable Long id, @RequestParam String diaryname,
                              HttpSession session, RedirectAttributes redirectAttributes) {
        Optional<Diary> diaryOpt = diaryRepository.findById(id);
        if (diaryOpt.isEmpty()) return "redirect:/diaries";
        Diary diary = diaryOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!diary.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав");
            return "redirect:/diaries";
        }
        diary.setDiaryname(diaryname);
        diaryRepository.save(diary);
        return "redirect:/diaries/" + id;
    }

    // Удаление дневника (только создатель)
    @PostMapping("/{id}/delete")
    public String deleteDiary(@PathVariable Long id, HttpSession session,
                              RedirectAttributes redirectAttributes) {
        Optional<Diary> diaryOpt = diaryRepository.findById(id);
        if (diaryOpt.isEmpty()) return "redirect:/diaries";
        Diary diary = diaryOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!diary.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав на удаление");
            return "redirect:/diaries";
        }
        diaryRepository.delete(diary);
        return "redirect:/diaries";
    }
}