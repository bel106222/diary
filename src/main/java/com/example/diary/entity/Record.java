package com.example.diary.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "records")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class Record {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //Автор записи
    //Указываем на связь "многие записи могут принадлежать одному пользователю".
    //fetch = FetchType.LAZY — означает, что объект User не будет загружен из
    //базы сразу при загрузке записи. Вместо этого Hibernate подставит временный
    //прокси-объект, а настоящий запрос к БД выполнится только при первом обращении
    //к полю user (например, record.getUser().getUsername()).
    //Это экономит ресурсы и ускоряет запросы. Если нам нужно просто вывести список записей,
    //но не показывать имя автора, мы не тратим время на лишний JOIN и загрузку данных пользователя.
    //Важный нюанс: Ленивая загрузка работает только при открытой Hibernate-сессии.
    //По умолчанию сессия закрывается после того, как данные ушли из контроллера,
    //и Thymeleaf не сможет достучаться до user. Эта проблема решается
    //параметром spring.jpa.open-in-view=true в application.properties
    //(он держит сессию открытой на время рендеринга страницы).
    //@JoinColumn(name = "userid", nullable = false) — Настраиваем колонку внешнего ключа в таблице records.
    //name = "userid" — Имя колонки, которая будет хранить id пользователя.
    //nullable = false — Гарантирует, что у каждой записи обязательно есть автор (связь не может быть пустой).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userid", nullable = false)
    private User user;

    // Дневник, в котором находится запись
    //Полностью аналогично связи с пользователем - каждая запись принадлежит
    //ровно одному дневнику. Ленивая загрузка экономит ресурсы, когда сам дневник не нужен.
    //Колонка внешнего ключа называется diaryid и она - обязательна (nullable = false).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diaryid", nullable = false)
    private Diary diary;

    //Задаём параметры колонки recorddescription.
    //nullable = false — Описание не может быть пустым на уровне БД.
    //length = 5000 — Максимальная длина строки (в символах).
    //В SQLite тип будет преобразован в TEXT, но ограничение длины может быть
    //полезно в других СУБД (например, для создания столбца VARCHAR(5000)).
    @Column(nullable = false, length = 5000)
    private String recorddescription;

    @Column(nullable = false)
    private LocalDateTime created;

    //Прикреплённые файлы
    //Одна запись может иметь много файлов.
    //mappedBy = "record" — указывает, что связь управляется полем record в
    //сущности FileAttachment. То есть в таблице files есть колонка recordid,
    //которая и обеспечивает эту связь. Сам Record не владеет связью (у него нет колонки с файлами).
    //Это способ организации двунаправленной связи OneToMany.
    //cascade = CascadeType.ALL — когда мы удаляем запись, будут удалены и все её файлы (записи в таблице files).
    //orphanRemoval = true — Дополнительная страховка. Если мы удалим файл из
    //коллекции record.getFiles() (просто уберём объект из списка), а затем сохраним запись,
    //Hibernate автоматически удалит этот "осиротевший" файл из БД.
    //Без этого флага файл остался бы в базе, но без привязки к записи
    //(значение recordid стало бы NULL или запись была бы удалена,
    //если бы не было ограничения nullable).
    @OneToMany(mappedBy = "record", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FileAttachment> files;

    //метод, устанавливающий текущую дату в поле даты создания текущей записи
    @PrePersist
    public void prePersist() {
        this.created = LocalDateTime.now();
    }
}