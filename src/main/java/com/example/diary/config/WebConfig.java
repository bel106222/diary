//Этот файл — «пульт управления» для веб-части нашего приложения.
//Он не содержит бизнес-логики, но настраивает критически важные элементы:
//защиту от неавторизованного доступа и раздачу загруженных пользователями файлов.
package com.example.diary.config;

//наш перехватчик, который будет защищать страницы.
import com.example.diary.interceptor.LoginInterceptor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

//специальный объект, через который мы добавляем перехватчики.
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;

//позволяет настраивать, как Spring-у отдавать статические файлы.
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

//интерфейс, реализуя который, мы можем тонко настроить Spring MVC без полного
//переопределения стандартных настроек
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//объявляем класс как источник бинов и конфигураций. Spring обработает его при запуске.
@Configuration

//implements WebMvcConfigurer — мы расширяем стандартные настройки Spring MVC.
//Не наследуемся от какого-то адаптера, а имплементируем интерфейс, что дает
//гибкость: переопределяем только нужные методы. Другие методы остаются по умолчанию.
public class WebConfig implements WebMvcConfigurer {

    //Поля final — зависимости, внедренные через конструктор:
    //LoginInterceptor — экземпляр нашего перехватчика (биин, созданный благодаря @Component).
    //Мы получаем его через DI (Dependency Injection).
    //uploadDir — значение свойства file.upload-dir.
    private final LoginInterceptor loginInterceptor;
    private final String uploadDir;

    //все зависимости передаются в конструкторе. Это делает класс удобным для тестирования и
    //гарантирует, что объект готов к использованию после создания.
    public WebConfig(LoginInterceptor loginInterceptor,
                     @Value("${file.upload-dir}") String uploadDir) {
        this.loginInterceptor = loginInterceptor;
        this.uploadDir = uploadDir;
    }

    //переопределяем метод из WebMvcConfigurer, который Spring вызывает для добавления перехватчиков.
    @Override

    //InterceptorRegistry — это специальный класс-реестр, предоставляемый Spring MVC.
    //Его единственная цель — собрать все перехватчики (наши HandlerInterceptor),
    //которые мы хотим применить, вместе с правилами, по которым они должны срабатывать.
    //Мы не создаём его вручную, а получаем готовый пустой объект и «наполняем».
    //После того как метод addInterceptors завершится, Spring забирает этот заполненный
    //реестр и использует его для настройки цепочки вызовов при обработке каждого HTTP-запроса.
    public void addInterceptors(InterceptorRegistry registry) {

        //добавляем наш перехватчик в реестр.
        registry.addInterceptor(loginInterceptor)

                //указываем URL-шаблоны, для которых этот перехватчик будет применяться.
                .addPathPatterns("/**")                     // все запросы

                //указываем URL-шаблоны, которые нужно исключить.
                .excludePathPatterns("/login", "/register", // кроме страниц входа/регистрации
                        "/css/**", "/js/**", "/images/**"); // статика
    }

    //Переопределяем метод для настройки обработки статических ресурсов. По умолчанию Spring
    //раздает файлы из папок static, public, resources, но наши загруженные файлы лежат в
    //отдельной папке uploads, которая не является стандартной. Если мы хотим, чтобы они были
    //доступны через браузер по URL, например, http://localhost:8080/files/some-uuid.jpg,
    //то для этого нужно сопоставить виртуальный URL /files/... с реальной папкой на жестком диске.
    //registry.addResourceHandler("/files/**") — указывает, что все запросы, начинающиеся
    //с /files/, будут обрабатываться этим обработчиком ресурсов. ** означает любое количество
    //вложенных подпапок внутри /files/.
    //.addResourceLocations("file:" + uploadDir + "/") — указывает, где физически находятся файлы.
    //Префикс file: обязателен! Он говорит Spring искать файлы в файловой системе, а не в
    //classpath (jar-файле)". Без него Spring попытается найти папку внутри src/main/resources,
    //что неверно.
    //uploadDir + "/" — полный путь к папке загрузок. У нас это ./uploads (относительный путь
    //от рабочей директории приложения). Символ / в конце добавлен для корректного сопоставления
    //(чтобы file:./uploads/ работал как базовый каталог).
    //В результате, когда браузер запрашивает http://localhost:8080/files/abc123.txt, Spring
    //берет путь /abc123.txt относительно ./uploads/ и ищет файл на диске. Если находит,
    //возвращает его содержимое с правильным Content-Type. Если нет — возвращает 404.
    @Override

    //ResourceHandlerRegistry — это своего рода «пульт управления статическими файлами» в
    //Spring MVC. Когда Spring вызывает метод addResourceHandlers(ResourceHandlerRegistry registry),
    //он сам создаёт пустой экземпляр этого реестра и передаёт его нам. А наша задача — наполнить
    //его необходимыми правилами.
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Делаем загруженные файлы доступными по URL /files/...
        registry.addResourceHandler("/files/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}