package com.example.diary.controller;

import com.example.diary.entity.Record;
import com.example.diary.repository.RecordRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

//Главная страница со всеми записями и поиском.
//Помечаем класс как Spring MVC Controller, без этого Spring
//не будет считать этот класс обработчиком веб-запросов.
@Controller
public class MainController {

    //определяем репозиторий записей дневников для передачи в качестве зависимости
    private final RecordRepository recordRepository;

    //Определяем конструктор с параметром для задания этой зависимости. Spring видит,
    //что для создания бина MainController требуется RecordRepository, находит его в
    //контексте (автоматически созданный бин нашего интерфейса) и внедряет сюда.
    public MainController(RecordRepository recordRepository) {
        this.recordRepository = recordRepository;
    }

    //Главная: все записи, отсортированные по дате создания (убывание)
    //Привязываем метод сразу к двум URL: корню сайта (/) и /records. Когда пользователь
    //входит в приложение или переходит по ссылке «Все записи», вызывается этот метод.
    @GetMapping({"/", "/records"})

    //При переходе по этим ссылкам запускается метод allRecords(), в который передаём
    //модель, которая заполнится списком всех записей, отсортированных по
    //дате создания и вернёт название шаблона для отображения всех записей
    public String allRecords(Model model) {

        //в репозитории выполнится запрос SELECT * FROM records ORDER BY created DESC
        List<Record> records = recordRepository.findAllByOrderByCreatedDesc();

        //чтобы Thymeleaf смог обратиться к полученному в шаблоне как ${records}.
        model.addAttribute("records", records);
        return "records-list";
    }

    // Поиск записей
    @GetMapping("/records/search")

    //@RequestParam("query") String query – извлекает значение параметра query из строки запроса.
    //Например, для URL /records/search?query=spring переменная query будет равна "spring".
    //Полученное значение поиска и модель для загрузки в неё результатов, передаём
    //в качестве параметров в метод, который позволит HTML-шаблону использовать модель,
    //в которой будут лежать список с результатами поиска и сама строка поиска.
    public String searchRecords(@RequestParam("query") String query, Model model) {
        List<Record> records = recordRepository.searchByDescription(query);
        model.addAttribute("records", records);
        model.addAttribute("searchQuery", query);
        return "records-list";
    }
}
//Т.о. одна страница records-list обслуживает и показ всех записей, и показ результатов поиска.
//Это избавляет от дублирования вёрстки. Логика в шаблоне (через th:if) определяет, показывать
//ли заголовок «Все записи» или «Результаты поиска».
//MainController отвечает только за главную страницу и поиск. Он не лезет в дневники или пользователей.