package com.example.diary.controller;

import com.example.diary.entity.User;
import com.example.diary.repository.UserRepository;
import jakarta.servlet.http.HttpSession;

//PasswordEncoder – интерфейс для безопасного хеширования паролей. Мы используем
//его реализацию BCryptPasswordEncoder.
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

//Регистрация, вход, выход.
@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    //в конструкторе внедряем репозиторий пользователей и генератор паролей
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    //Страница входа GET-запрос по пути /login
    //Возвращает имя шаблона login. Модели не нужны – форма пуста. Ошибки будут переданы
    //через flash-атрибуты (после неудачной попытки входа).
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    //Обработка входа POST-запрос по пути /login
    //обрабатывает отправку формы входа.
    @PostMapping("/login")

    //получаем из формы шаблона username и password, и передаём их
    //в качестве параметров в метод login(), вместе с текущей сессией
    //для установки текущего пользователя и flash-атрибутами для
    //передачи ошибок авторизации
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userRepository.findByUsername(username);

        //Критически важная проверка. Мы не сравниваем пароли напрямую, потому что в базе хранится
        //только хеш BCrypt. Метод matches берёт введённый пароль, хеширует его по тому же алгоритму
        //и сравнивает с хешем из БД, таким образом сверяются введённый пароль с хранящимся в БД.
        //BCrypt – медленный адаптивный алгоритм хеширования, специально спроектированный для паролей.
        //Он автоматически включает "соль" (случайную добавку) в хеш, что защищает от радужных таблиц.
        if (userOpt.isPresent() && passwordEncoder.matches(password, userOpt.get().getPassword())) {
            session.setAttribute("currentUser", userOpt.get()); //пишем в сессию текущего пользователя
            return "redirect:/records";   // после входа – выводим список всех записей
        }
        //кладём сообщение об ошибке во flash-атрибуты. Они переживут редирект и отобразятся на странице входа.
        redirectAttributes.addFlashAttribute("error", "Неверное имя пользователя или пароль");
        return "redirect:/login"; //заново открываем login
    }

    // Выход
    @GetMapping("/logout")
    public String logout(HttpSession session) {

        //Уничтожаем сессию целиком. Все атрибуты, включая currentUser, удаляются.
        session.invalidate();

        //После этого перенаправляем на страницу входа.
        return "redirect:/login";
    }

    // Страница регистрации - аналогично странице входа, просто отдаём шаблон register.html.
    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    // Обработка регистрации
    @PostMapping("/register")

    //через @RequestParam – получаем три поля формы из шаблона register: username,
    //email, password. В месте с ними передаём в качестве параметров, сессию и flash-атрибуты
    public String register(@RequestParam String username,
                           @RequestParam String email,
                           @RequestParam String password,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        //Проверки уникальности
        if (userRepository.findByUsername(username).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Такой пользователь уже существует!");
            return "redirect:/register";
        }
        if (userRepository.findByEmail(email).isPresent()) {
            redirectAttributes.addFlashAttribute("error", "Этот Email уже используется!");
            return "redirect:/register";
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);

        //Мы ни в коем случае не сохраняем пароль в открытом виде! encode превращает его в BCrypt-хеш.
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);

        //сразу после регистрации "логиним" пользователя, помещая его в сессию.
        session.setAttribute("currentUser", user);
        return "redirect:/records";
    }}