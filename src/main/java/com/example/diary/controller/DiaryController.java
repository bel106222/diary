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

//@RequestMapping("/diaries") - определяет, что все методы внутри этого контроллера
//будут относиться к URL, начинающемуся с /diaries.
@RequestMapping("/diaries")

public class DiaryController {

    //определяем зависимость, которая будет внедряется через конструктор.
    private final DiaryRepository diaryRepository;

    //передаём в конструктор репозиторий дневников
    public DiaryController(DiaryRepository diaryRepository) {
        this.diaryRepository = diaryRepository;
    }

    // Список всех дневников
    //@GetMapping (без дополнительного пути) — обрабатывает GET /diaries.
    @GetMapping

    //метод позволяет передать в HTML-шаблон список всех дневников, отсортированных от новых к старым.
    public String listDiaries(Model model) {
        model.addAttribute("diaries", diaryRepository.findAllByOrderByCreatedDesc());
        return "diary-list";
    }

    //Форма создания дневника
    //Обрабатывает GET /diaries/new. Просто показывает пустую форму diary-form.html.
    //Мы не передаём никаких данных, потому что форма сама знает, куда отправлять POST.
    @GetMapping("/new")
    public String newDiaryForm() {
        return "diary-form";
    }

    //Сохранение нового дневника
    //Oбрабатываем POST-запрос на /diaries.
    @PostMapping

    //@RequestParam String diaryname — получает название дневника из поля формы <input name="diaryname">.
    public String createDiary(@RequestParam String diaryname, HttpSession session) {
        User user = (User) session.getAttribute("currentUser");
        Diary diary = new Diary();
        //устанавливаем название и автора. Дата создания проставится автоматически методом @PrePersist.
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
        model.addAttribute("records", diary.getRecords()); //записи придут без сортировки
        //Но можно оставить как есть или отсортировать уже в шаблоне.
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

        //используется тот же шаблон, что и для создания дневника, только он уже будет
        //заполнен из модели
        return "diary-form";
    }

    //Сохранение изменений
    //Обрабатываем POST /diaries/{id}
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