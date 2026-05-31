package com.example.diary.advice;

import com.example.diary.entity.User;
import jakarta.servlet.http.HttpSession;

//аннотация, которая помечает класс как «глобальный помощник» для всех контроллеров.
import org.springframework.web.bind.annotation.ControllerAdvice;

//аннотация, которая заставляет метод выполняться перед каждым запросом, а его
//возвращаемое значение автоматически помещается в модель
import org.springframework.web.bind.annotation.ModelAttribute;

//Объявляем класс глобальным советником (Advice).
//Класс помещается в отдельный пакет advice. Это стандартная практика в Spring-приложениях.
//Сюда выносятся сквозные задачи (cross-cutting concerns): обработка ошибок (@ExceptionHandler),
//глобальная инициализация модели (@ModelAttribute), настройка биндинга данных (@InitBinder).
//Это помогает не засорять контроллеры повторяющимся кодом.
//Spring будет применять методы этого класса ко всем контроллерам, отмеченным
//аннотацией @Controller. Нам больше не нужно в каждом методе каждого контроллера писать
//model.addAttribute("currentUser", ...) – этот советник сделает это автоматически.
@ControllerAdvice

//метод, получающий текущего пользователя из сессии и добавляющий его в модель.
//Без него нам пришлось бы в каждом методе каждого контроллера вручную добавлять
//пользователя в модель: model.addAttribute("currentUser", session.getAttribute("currentUser"))
public class GlobalControllerAdvice {

    //Добавляем в модель текущего пользователя для отображения в layout
    //Аннотация @ModelAttribute «вешает» метод на каждый HTTP-запрос, обрабатываемый любым
    //контроллером. Возвращаемое значение метода будет помещено в модель под ключом "currentUser".
    //Spring MVC вызывает этот метод перед вызовом метода контроллера. Результат добавляется
    //в Model, после чего управление передается контроллеру. Контроллер может дополнить
    //модель своими данными, но атрибут currentUser уже будет там.
    @ModelAttribute("currentUser")
    public User getCurrentUser(HttpSession session) {
        return (User) session.getAttribute("currentUser");
    }
}