//файл, в котором мы вручную настраиваем и отдаем под управление Spring объекты,
//необходимые всему приложению. Это чистая Java-конфигурация, пришедшая на смену
//громоздким XML-файлам.
package com.example.diary.config;

//@Value — аннотация для внедрения значений из файлов свойств (application.properties)
//прямо в поля Spring-бинов.
import org.springframework.beans.factory.annotation.Value;

//@Bean — аннотация, которая говорит Spring: «Результат работы этого метода — объект,
//который нужно поместить в контейнер как бин».
import org.springframework.context.annotation.Bean;

//@Configuration — аннотация, которая указывает, что данный класс содержит определения
//бинов (методы с @Bean). Spring обработает его особым образом, гарантируя, что вызовы
//методов не будут создавать новые объекты, а вернут уже существующие синглтоны.
import org.springframework.context.annotation.Configuration;

//конкретная реализация интерфейса PasswordEncoder, использующая адаптивный криптографический
//алгоритм BCrypt. Это лучший выбор для хеширования паролей.
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

//PasswordEncoder — интерфейс, который абстрагирует процесс хеширования и проверки паролей.
//Мы объявляем бин с типом интерфейса (программирование на уровне интерфейсов).
import org.springframework.security.crypto.password.PasswordEncoder;

//@Configuration — определяет класс как конфигурационный. Spring создаст его экземпляр
//и будет использовать для получения бинов. Данная аннотация — это специализированная
//форма @Component. Она гарантирует, что вызовы методов с @Bean внутри этого класса
//будут перехвачены (проксированы), чтобы каждый бин создавался ровно один раз. Если бы
//мы использовали просто @Component, могло бы возникнуть дублирование объектов при
//внутриклассовых вызовах.
@Configuration

//Здесь создаём бин BCryptPasswordEncoder и определяем путь загрузки файлов.
public class AppConfig {

    //извлекаем значение свойства file.upload-dir из файла application.properties
    //и присваивает его полю uploadDir.
    @Value("${file.upload-dir}")
    private String uploadDir;

    //Аннотация @Bean — объявляет, что возвращаемое значение метода должно быть
    //зарегистрировано как бин в контексте Spring. Имя бина по умолчанию совпадает
    //с именем метода — passwordEncoder.
    @Bean

    //PasswordEncoder как возвращаемый тип — мы возвращаем интерфейс, а не конкретную
    //реализацию. Это позволяет в будущем легко заменить BCrypt на другой алгоритм
    //(например, SCrypt или Pbkdf2), изменив только этот метод. Весь остальной код
    //зависит только от интерфейса. new BCryptPasswordEncoder() — создает экземпляр кодера.
    //BCrypt автоматически генерирует случайную «соль» (salt) для каждого пароля и включает
    //её в результирующий хеш. Поэтому даже одинаковые пароли у разных пользователей будут
    //выглядеть в базе по-разному. Это защищает от атак по радужным таблицам.
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //Создаём бин типа String с именем uploadDir. Теперь в любом месте приложения можно
    //внедрить строку с путём к папке загрузки по имени бина.
    @Bean
    public String uploadDir() {
        return uploadDir;
    }
}
//Т.о. при старте приложения Spring видит @Configuration и обрабатывает класс AppConfig.
//Вызывает метод passwordEncoder(), регистрирует полученный BCryptPasswordEncoder как бин
//с именем passwordEncoder, затем вызывает метод uploadDir(), регистрирует строку "./uploads",
//и поле uploadDir заполняется значением из файла application.properties.
//Далее, когда AuthController запрашивает через конструктор PasswordEncoder, Spring находит бин,
//созданный в AppConfig, и внедряет его. Аналогично используется uploadDir.