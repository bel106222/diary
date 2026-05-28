package com.example.diary.controller;

import com.example.diary.entity.Record;
import com.example.diary.repository.RecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//Главная страница со всеми записями и поиском.
@Controller
public class MainController {

    private final RecordRepository recordRepository;

    public MainController(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    // Главная: все записи, отсортированные по дате создания (убывание)
    @GetMapping({"/", "/records"})
    public String allRecords(Model model) {
        List<Record> records = recordRepository.findAllByOrderByCreatedDesc();
        model.addAttribute("records", records);
        return "records-list";
    }

    // Поиск записей
    @GetMapping("/records/search")
    public String searchRecords(@RequestParam("query") String query, Model model) {
        List<Record> records = recordRepository.searchByDescription(query);
        model.addAttribute("records", records);
        model.addAttribute("searchQuery", query);
        return "records-list";
    }
}