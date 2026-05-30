package com.example.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

//Говорим Hibernate, что это не просто класс, а отображение таблицы в БД.
//Это обязательное условие для работы JPA, без нё аннотации Spring Data JPA
//не будет управлять этим классом.
@Entity

//Явно указываем имя таблицы в БД — users.
@Table(name = "users")

//Lombok автоматически генерирует методы getId(), setId(...),
//getUsername(), setUsername(...) и так для всех полей класса.
//Так как для JPA геттеры и сеттеры обязательны, чтобы фреймворк мог
//читать и записывать данные из/в объект.
@Getter @Setter

//Генерируем пустой конструктор public User() {}.
//Это критически важно для JPA/Hibernate!
//Когда Hibernate достает сущность из базы данных, он сначала создает пустой объект,
//а потом через сеттеры или прямо в поля заполняет его данными.
//Без этого конструктора приложение упадет с ошибкой.
//Генерируем конструктор со всеми полями: public User(Long id, String username, String email,
//String password, ...).
@NoArgsConstructor @AllArgsConstructor

public class User {

    //Эта аннотация указывает, что это поле — первичный ключ таблицы.
    @Id

    //Говорим БД автоматически генерировать уникальное значение для этого
    //поля при вставке новой строки.
    //GenerationType.IDENTITY означает, что за генерацию отвечает сама БД
    //с помощью столбца типа AUTOINCREMENT (SQLite).
    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;

    //@Column - эта аннотация позволяет настроить свойства колонки в таблице.
    //Если ее не указать, Hibernate создаст колонки с параметрами по умолчанию.
    //nullable = false: На уровне базы данных делает колонку NOT NULL, запрещая пустые значения.
    //unique = true: На уровне базы данных создает уникальный индекс.

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;   // хранится в виде хеша BCrypt

    @Column(nullable = false)
    private LocalDateTime created;

    // Связь с дневниками (по автору)
    //@OneToMany: Описывает тип связи - один пользователь может иметь много дневников.
    //mappedBy = "user": Связью управляет поле user, которое находится в классе Diary.
    //cascade = CascadeType.ALL: Определяет правило каскадных операций, если мы удалим
    //пользователя (userRepository.delete(user)), Hibernate автоматически пройдет
    //по этой связи и удалит все его дневники.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Diary> diaries;

    // Связь с записями (автор)
    //Всё то же: Один пользователь может написать много записей.
    //Удаление пользователя приведет к каскадному удалению всех его записей.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Record> records;

    //Это аннотация из JPA, которая отмечает метод, вызываемый автоматически
    //прямо перед самым первым сохранением (persist/INSERT) объекта в базу данных.
    //this.created = LocalDateTime.now(); В этот момент, в поле created этой таблицы
    //устанавливается текущая дата и время.
    @PrePersist
    public void prePersist() {
        this.created = LocalDateTime.now();
    }
}