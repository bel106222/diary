package com.example.diary.interceptor;

import com.example.diary.entity.User;

//стандартные интерфейсы Java Servlet API для работы с запросом, ответом и сессией.
//Они нужны для доступа к сессии и для перенаправления.
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

//@Component - аннотация Spring, которая делает класс бином. Это обязательно, чтобы Spring
//обнаружил его и мы могли зарегистрировать перехватчик.
import org.springframework.stereotype.Component;

//интерфейс Spring MVC, который мы будем наследовать. Он содержит три метода: preHandle,
//postHandle, afterCompletion. Нам нужен только preHandle, чтобы выполнить проверку до того,
//как запрос дойдёт до контроллера.
import org.springframework.web.servlet.HandlerInterceptor;

//определяем наш класс как бин
@Component

//Это ключевой компонент безопасности: он автоматически проверяет, авторизован ли пользователь,
//перед тем как пустить его к любому защищённому URL. Здесь мы реализуем интерфейс
//HandlerInterceptor. Это стандартный способ создания перехватчиков в Spring MVC.
public class LoginInterceptor implements HandlerInterceptor {

    //Переопределяем метод из родительского интерфейса. Он вызывается перед выполнением
    //метода контроллера. Именно здесь мы можем прервать обработку и вернуть false,
    //если условия не выполнены.
    @Override

    //Аргументы, передаваемые в качестве параметров:
    //HttpServletRequest request – объект запроса. Мы используем его для получения сессии (request.getSession()).
    //HttpServletResponse response – объект ответа. Мы используем его для перенаправления (response.sendRedirect(...)).
    //Object handler – ссылка на обработчик (метод контроллера), который будет вызван. Мы не используем его, но он доступен.
    //throws Exception – позволяет выбрасывать исключения (например, если что-то пошло не так при перенаправлении).
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        //Получаем текущую сессию, связанную с запросом. Если сессии не было, она будет создана,
        //но атрибут currentUser будет отсутствовать. Достаём из сессии атрибут с ключом
        //"currentUser", который мы положили туда в AuthController при входе или регистрации
        //и приводим к типу User. Если пользователь не входил в систему, атрибут будет отсутствовать
        //и user == null, тогда response.sendRedirect("/login"); – отправляет HTTP-редирект (302)
        //на страницу входа. Браузер автоматически перейдёт по новому URL.
        //return false; – прерывает цепочку обработки запроса. Контроллер не будет вызван,
        //представление не будет отрендерено. Так мы полностью блокируем доступ.
        //return true; – если пользователь найден, разрешаем обработку. Запрос продолжает
        //идти к намеченному контроллеру.
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("currentUser");
        if (user == null) {
            response.sendRedirect("/login");
            return false;
        }
        return true;
    }
}
//Это стандартная практика в Spring MVC – разделять перехватчики (interceptors) в отдельный
//пакет. Они не являются контроллерами, а служат для сквозной функциональности (безопасность,
//логирование, обработка сессий).