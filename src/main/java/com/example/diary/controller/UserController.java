package com.example.diary.controller;

import com.example.diary.repository.UserRepository;

//аннотация, которая сообщает Spring: «Этот класс – контроллер, найди его
//при сканировании и создай из него бин».
import org.springframework.stereotype.Controller;

//специальный объект-«рюкзак», в который можно положить данные, чтобы
// они потом попали в HTML-шаблон.
import org.springframework.ui.Model;

//аннотация, которая «привязывает» HTTP-метод GET и конкретный URL к нашему Java-методу.
import org.springframework.web.bind.annotation.GetMapping;

//Список всех пользователей.
//@Controller – эта аннотация делает класс контроллером Spring MVC.
//Это значит, что Spring автоматически обнаружит этот класс и зарегистрирует его
//как обработчик веб-запросов.
//Почему не @RestController? @RestController = @Controller + @ResponseBody. Он подходит,
//когда мы возвращаем данные (JSON/XML). А здесь мы возвращаем имя HTML-шаблона,
//поэтому нужен именно @Controller. Без этой аннотации наш метод listUsers никогда
//не был бы вызван для URL /users.
@Controller
public class UserController {

    //Поле для хранения ссылки на репозиторий. Объявлено как final, чтобы его нельзя
    //было изменить после инициализации (повышает надёжность).
    //Репозиторий — это наш «шлюз» к таблице users в базе данных.
    private final UserRepository userRepository;

    //Объявляем конструктор с параметром. Spring, создавая контроллер, видит, что ему
    //нужен UserRepository, находит этот бин в своём контексте и передаёт его сюда.
    //Это называется Constructor Injection (внедрение через конструктор). Это самый
    //рекомендуемый способ в Spring. Объект сразу создаётся полностью готовым к работе.
    //Поле final гарантирует, что зависимость не потеряется. Класс можно тестировать,
    //просто передав в конструктор mock-объект репозитория.
    public UserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //Связываем HTTP GET-запрос по пути /users с нашим методом listUsers().
    //Когда пользователь в браузере открывает http://localhost:8080/users, Spring вызывает
    //именно этот метод. Аннотация @GetMapping говорит: «только GET-запросы».
    @GetMapping("/users")

    //Метод listUsers() возвращает String – это имя представления (view name), которое мы
    //хотим показать. Model model говорит Spring автоматически создать пустой объект Model и
    //передаёт его нам, а мы можем наполнять его данными (атрибутами), которые затем
    //будут доступны в HTML-шаблоне.
    public String listUsers(Model model) {

        //userRepository.findAll() – вызывает стандартный метод из JpaRepository
        //и возвращает список всех пользователей в виде List<User>.
        //model.addAttribute("users", ...) – кладёт полученный список в модель под ключом "users".
        //Потом Spring передаст эту модель в шаблонизатор Thymeleaf и в HTML-шаблоне
        //мы сможем написать th:each="user : ${users}", и Thymeleaf «увидит» наш список
        //и отобразит каждого пользователя.
        //Имя атрибута ("users") должно точно совпадать с тем, что используется в шаблоне (users-list.html).
        model.addAttribute("users", userRepository.findAll());

        //"users-list" – строка, которую возвращает метод. Spring передаёт её в ViewResolver
        //(в нашем случае ThymeleafViewResolver). Тот по соглашению ищет файл
        //src/main/resources/templates/users-list.html. Thymeleaf обрабатывает этот файл,
        //подставляет данные из модели (список пользователей) и рендерит готовую HTML-страницу,
        //которую браузер и получает. Эта строка "users-list" обязательно должна соответствовать
        //имени файла шаблона, который был создан. Spring автоматически добавляет префикс
        //templates/ и суффикс .html благодаря настройкам по умолчанию Spring Boot.
        return "users-list";
    }
}
//Таким образом, когда браузер запрашивает http://localhost:8080/users, Spring видит
//@GetMapping("/users") и вызывает метод UserController.listUsers().
//Контроллер через userRepository.findAll() идёт в базу данных SQLite и получает всех
//пользователей. Полученный список кладётся в Model под именем "users" и контроллер возвращает
//строку "users-list". Thymeleaf загружает шаблон users-list.html, извлекает список
//пользователей из модели и генерирует HTML-таблицу, затем готовая страница отправляется
//обратно в браузер.