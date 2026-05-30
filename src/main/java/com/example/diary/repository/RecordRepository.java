package com.example.diary.repository;

import com.example.diary.entity.Record;
import org.springframework.data.jpa.repository.JpaRepository;

//Query — аннотация для написания собственного JPQL-запроса (или нативного SQL).
//Она будет использоваться, когда автоматического метода по имени недостаточно.
import org.springframework.data.jpa.repository.Query;

//Param — аннотация, связывающая параметры запроса с аргументами метода (указываем имена).
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface RecordRepository extends JpaRepository<Record, Long> {

    //Все записи, отсортированные по дате создания (сначала новые):
    //Как работает Spring Data JPA при анализе имени метода:
    //findAll → выбрать все записи.
    //OrderBy → указание сортировки.
    //Created → поле сущности Record.created (дата создания).
    //Desc → по убыванию (DESC), т.е. сначала самые свежие.
    List<Record> findAllByOrderByCreatedDesc();

    //Записи конкретного дневника:
    //Как работает Spring Data JPA при анализе имени метода:
    //find → операция SELECT.
    //ByDiaryId → фильтрация по полю diary.id в сущности Record.
    //(Точнее, по внешнему ключу diaryid). У нас связь ManyToOne,
    //поэтому Spring автоматически создаст условие WHERE r.diary.id = ?1.
    //OrderByCreatedDesc — сортировка по дате создания по убыванию.
    List<Record> findByDiaryIdOrderByCreatedDesc(Long diaryId);

    //Поиск по тексту описания (LIKE):
    //@Query(...) — Здесь мы пишем собственный JPQL-запрос, потому что автоматический
    //парсинг по имени метода не может создать поиск по подстроке без дополнительных ухищрений.
    //JPQL-запрос:
    //SELECT r FROM Record r — выборка всех записей. r — псевдоним сущности, как в SQL.
    //WHERE LOWER(r.recorddescription) LIKE LOWER(CONCAT('%', :query, '%')) — фильтр по описанию записи.
    //LOWER(...) — приводит и поле, и поисковый запрос к нижнему регистру. Это делает поиск
    //регистронезависимым (неважно, большие или маленькие буквы ввёл пользователь).
    //CONCAT('%', :query, '%') — оборачивает поисковый запрос в символы %, что означает
    //"любая последовательность символов до и после". Таким образом, ищется вхождение
    //подстроки в описание, а не точное совпадение.
    //:query — именованный параметр, значение которого будет подставлено из метода.
    //ORDER BY r.created DESC — сортировка результатов по дате создания, сначала новые.
    //Почему использована функция CONCAT, а не просто '%' || :query || '%'? В JPQL функция
    //CONCAT более стандартна и корректно работает в различных СУБД (включая SQLite через Hibernate).
    //Для SQLite диалект community-dialects транслирует её в нужный синтаксис.
    //@Param("query") — связывает аргумент метода String query с именованным параметром :query
    //в JPQL-запросе. Имена должны совпадать.
    @Query("SELECT r FROM Record r WHERE LOWER(r.recorddescription) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY r.created DESC")
    List<Record> searchByDescription(@Param("query") String query);
}