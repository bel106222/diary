package com.example.diary.controller;

//импортируем все сущности (модели, классы с описанием полей таблиц БД)
import com.example.diary.entity.*;
import com.example.diary.entity.Record;

//импортируем все репозитории, через которые мы общаемся с БД.
import com.example.diary.repository.*;

//серверная сессия, в которой мы храним текущего пользователя (currentUser)
//после входа, для авторизованных действий.
import jakarta.servlet.http.HttpSession;

//позволяет читать значения из application.properties, в нашем случае путь
//к папке для загрузки файлов.
import org.springframework.beans.factory.annotation.Value;

//объявляет класс как Spring-контроллер.
import org.springframework.stereotype.Controller;

//контейнер для передачи данных из контроллера в представление (HTML-шаблон).
import org.springframework.ui.Model;

//аннотации для маппинга URL-путей (@RequestMapping, @GetMapping, @PostMapping),
//получения переменных из URL (@PathVariable) и параметров запроса (@RequestParam).
import org.springframework.web.bind.annotation.*;

//интерфейс Spring для работы с загруженными файлами (из формы
//с enctype="multipart/form-data").
import org.springframework.web.multipart.MultipartFile;

//специальный контейнер для передачи сообщений или данных при редиректе
//(через flash-атрибуты). Это позволяет показать ошибку на следующей странице.
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;

//исключение ввода-вывода, которое может возникнуть при сохранении файлов на диск.
import java.io.IOException;

//современные классы для работы с файловой системой (Paths, Files, StandardCopyOption).
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

//контейнер, который заставляет нас явно проверять, есть ли значение
//(например, найден ли дневник в БД).
import java.util.Optional;

//генератор уникальных идентификаторов, который мы используем для создания
//безопасных имён загружаемых файлов.
import java.util.UUID;

//Объявляем класс контроллером Spring MVC, предназначенным для создания, просмотра,
//редактирования, удаление записей, загрузки файлов. Все методы внутри будут
//обрабатывать веб-запросы и возвращать имена шаблонов или редиректы.
@Controller

//базовый URL для всех методов контроллера. {diaryId} — это переменная пути,
//которая будет извлекаться и передаваться в методы через @PathVariable Long diaryId.
//Благодаря этому мы всегда знаем, с каким дневником работаем.
@RequestMapping("/diaries/{diaryId}/records")

public class RecordController {

    //Три репозитория — для управления записями, дневниками и файлами.
    //Все они private final, чтобы гарантировать неизменяемость после инициализации.
    private final RecordRepository recordRepository;
    private final DiaryRepository diaryRepository;
    private final FileAttachmentRepository fileAttachmentRepository;

    //uploadDir — путь к папке для сохранения файлов, читается из application.properties
    //(строка file.upload-dir=./uploads). @Value внедряет это значение прямо в конструктор.
    private final String uploadDir;

    //Constructor Injection — зависимости передаются через конструктор.
    //Объект сразу готов к работе, его легко тестировать, а поля можно сделать final.
    public RecordController(RecordRepository recordRepository,
                            DiaryRepository diaryRepository,
                            FileAttachmentRepository fileAttachmentRepository,
                            @Value("${file.upload-dir}") String uploadDir) {
        this.recordRepository = recordRepository;
        this.diaryRepository = diaryRepository;
        this.fileAttachmentRepository = fileAttachmentRepository;
        this.uploadDir = uploadDir;
    }

    //Форма создания новой записи в дневнике
    //Будет обрабатываться GET-запрос по пути /diaries/{diaryId}/records/new
    //Где отобразится пустая форма.
    @GetMapping("/new")

    //@PathVariable Long diaryId — извлекает diaryId из URL и передаёт в метод.
    public String newRecordForm(@PathVariable Long diaryId, Model model) {

        //diaryRepository.findById(diaryId) — ищет по id дневник в БД и возвращает Optional<Diary>.
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);

        //Если дневник не найден (например, пользователь подставил несуществующий ID в адресной строке),
        //мы делаем редирект на список дневников. Это безопасно и не даёт упасть приложению.
        if (diaryOpt.isEmpty()) return "redirect:/diaries";

        //кладём найденный дневник в модель. В шаблоне record-form он нужен, чтобы построить URL
        //для отправки формы (th:action использует diary.id).
        model.addAttribute("diary", diaryOpt.get());

        //создаём пустой объект Record. Это нужно, чтобы Thymeleaf мог привязать поля формы
        //(например, th:text="${record.recorddescription}" не упадёт с null).
        //При создании записи этот объект не содержит данных.
        model.addAttribute("record", new Record());

        //Возвращаем "record-form" — это имя шаблона (record-form.html), который
        //отрендерится с переданными текущим diary и пустым record.
        return "record-form";
    }

    //Сохранение новой записи
    //@PostMapping — обрабатывает POST-запрос на тот же базовый URL (/diaries/{diaryId}/records).
    //Это действие при сохранения формы.
    @PostMapping

    //@PathVariable Long diaryId — извлекает diaryId из URL и передаёт в метод.
    //@RequestParam String recorddescription — получает текст описания из поля формы с именем recorddescription.
    //@RequestParam(required = false) MultipartFile[] files — получает массив загруженных файлов.
    //required = false означает, что поле необязательно (можно создать запись без файлов).
    //HttpSession session — сессия, из которой мы берём текущего пользователя (currentUser).
    //Мы не получаем пользователя из базы заново, чтобы не делать лишний запрос — он уже лежит в сессии после входа.
    //RedirectAttributes redirectAttributes - говорит, что этот метод делает редирект, и
    //здесь потенциально могут быть flash-атрибуты (например, для всплывающего сообщение об успехе операции).
    public String createRecord(@PathVariable Long diaryId,
                               @RequestParam String recorddescription,
                               @RequestParam(required = false) MultipartFile[] files,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        //Получаем по распарсенному id текущий дневник
        Optional<Diary> diaryOpt = diaryRepository.findById(diaryId);

        //Если не нашёлся - возвращаемся к списку дневников
        if (diaryOpt.isEmpty()) return "redirect:/diaries";

        //Получаем из сессии текущего пользователя
        User user = (User) session.getAttribute("currentUser");

        //Создание и наполнение объекта Record — вручную проставляем текущий дневник, автора
        //(текущего пользователя) и описание.
        Record record = new Record();
        record.setDiary(diaryOpt.get());
        record.setUser(user);
        record.setRecorddescription(recorddescription);

        //сохраняем запись в БД. После сохранения у объекта record появится id, который
        //нужен для привязки файлов.
        recordRepository.save(record);

        //проверяем, были ли загружены файлы. Если да, вызываем вспомогательный метод
        //saveFiles описанный ниже.
        if (files != null && files.length > 0) {
            saveFiles(record, files);
        }

        //После успешного создания перенаправляем пользователя на страницу дневника,
        //где теперь видна новая запись. Редирект нужен, чтобы избежать повторной
        //отправки формы при обновлении страницы (защита от дублирования).
        return "redirect:/diaries/" + diaryId;
    }

    //Просмотр одной записи
    //Обрабатываем GET-запрос для просмотра конкретной записи, например, /diaries/5/records/12.
    @GetMapping("/{recordId}")

    //распарсиваем diaryId и recordId из URL. diaryId используется только для редиректа,
    //если запись не была найдена, передаём модель для передачи объектов и сессию для
    //определения текущего пользователя
    public String viewRecord(@PathVariable Long diaryId,
                             @PathVariable Long recordId,
                             Model model,
                             HttpSession session) {

        //По id ищем нужную запись
        Optional<Record> recordOpt = recordRepository.findById(recordId);

        //Если не находим запись, редиректим на страницу текущего дневника
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;

        //Полученную из БД запись, приравниваем к новому объекту record для передачи в
        //представление (HTML-шаблон)
        Record record = recordOpt.get();

        //загружаем запись в модель для передачи в record-view.html.
        model.addAttribute("record", record);

        //в модель добавляем флаг, который показывает, является ли текущий пользователь
        //автором записи. Сравниваем ID автора записи и ID пользователя из сессии.
        //Это нужно, чтобы в шаблоне показать или скрыть кнопки «Редактировать» и «Удалить».
        model.addAttribute("isAuthor", record.getUser().getId()
                .equals(((User) session.getAttribute("currentUser")).getId()));

        //Возвращаем строку "record-view" с именем шаблона.
        return "record-view";
    }

    // Форма редактирования (только автор)
    //Путь /diaries/{diaryId}/records/{recordId}/edit для GET-запроса.
    @GetMapping("/{recordId}/edit")

    //передаём параметры как в предшествующих методах
    public String editRecordForm(@PathVariable Long diaryId,
                                 @PathVariable Long recordId,
                                 Model model, HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        //получаем текущую запись
        Optional<Record> recordOpt = recordRepository.findById(recordId);

        //Если запись не найдена возвращаемся к сранице текущего дневника
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;

        //заполняем record из полученной записи
        Record record = recordOpt.get();

        //получаем из сессии текущего пользователя и сравниваем с
        //полученным из записи, если не равны - используя flash-атрибуты
        //при редиректе сообщаем об ошибке и возвращаемся на страницу текущего дневника
        User user = (User) session.getAttribute("currentUser");
        if (!record.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав на редактирование");
            return "redirect:/diaries/" + diaryId;
        }

        //при совпадении - загружаем в модель текущую запись и текущий дневник
        //для передачи в представление (HTML-шаблон) ту же форму, что и для создания,
        //но уже с заполненными полями (благодаря th:value="${record.recorddescription}").
        //Наличие record.id != null в шаблоне определяет, что это редактирование,
        //и форма отправляется на POST /{recordId}.
        model.addAttribute("record", record);
        model.addAttribute("diary", record.getDiary());

        //Возвращаем строку "record-view" с именем шаблона.
        return "record-form";
    }

    //Сохранение изменений записи
    //Аналогичен созданию, но вместо нового объекта мы получаем существующий,
    //проверяем права и обновляем описание.
    @PostMapping("/{recordId}")
    public String updateRecord(@PathVariable Long diaryId,
                               @PathVariable Long recordId,
                               @RequestParam String recorddescription,
                               @RequestParam(required = false) MultipartFile[] files,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<Record> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;
        Record record = recordOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!record.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав");
            return "redirect:/diaries/" + diaryId;
        }

        //из поля формы с описанием записи, вставляем текст в соответствующее поле объекта record
        record.setRecorddescription(recorddescription);

        //для Hibernate это будет операция UPDATE, потому что объект уже имеет id и
        //привязан к контексту.
        recordRepository.save(record);

        //если добавлялись файлы, привязываем их к текущей записи
        if (files != null && files.length > 0) {
            saveFiles(record, files);
        }

        //возвращаемся на страницу просмотра этой же записи, чтобы увидеть изменения
        return "redirect:/diaries/" + diaryId + "/records/" + recordId;
    }

    // Удаление записи (только автор)
    @PostMapping("/{recordId}/delete")
    public String deleteRecord(@PathVariable Long diaryId,
                               @PathVariable Long recordId,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {
        Optional<Record> recordOpt = recordRepository.findById(recordId);
        if (recordOpt.isEmpty()) return "redirect:/diaries/" + diaryId;
        Record record = recordOpt.get();
        User user = (User) session.getAttribute("currentUser");
        if (!record.getUser().getId().equals(user.getId())) {
            redirectAttributes.addFlashAttribute("error", "Нет прав на удаление");
            return "redirect:/diaries/" + diaryId;
        }

        //Получаем привязанные к этой записи пути к файлам и перебираем их в цикле,
        //удаляя файлы с диска
        for (FileAttachment fa : record.getFiles()) {
            Path filePath = Paths.get(uploadDir, fa.getFilepath());
            try {
                //Files.deleteIfExists — безопасный метод, который не бросает исключение,
                //если файл отсутствует.
                Files.deleteIfExists(filePath);
            } catch (IOException ignored) {}
        }

        //удаляем запись из БД. Благодаря cascade = CascadeType.ALL и orphanRemoval = true на
        //связи с FileAttachment, все связанные файловые записи в таблице files тоже будут
        //удалены автоматически.
        recordRepository.delete(record);

        //возращаемся обратно в дневник, откуда была удалена запись.
        return "redirect:/diaries/" + diaryId;
    }

    //Вспомогательный метод сохранения файлов
    private void saveFiles(Record record, MultipartFile[] files) {
        try {
            Path uploadPath = Paths.get(uploadDir);

            //Создание папки, если её нет.
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            //Цикл по всем файлам для обработки каждого загруженного файла
            for (MultipartFile file : files) {
                if (file.isEmpty()) continue; //Пропуск пустых файлов

                //Генерация уникального имени, т.к. нельзя доверять оригинальному имени файла
                //(оно может содержать опасные символы или повторяться). Берём расширение
                //(.txt, .jpg) и добавляем случайный UUID. Так файлы не перезаписывают друг друга и безопасны.
                String originalFilename = file.getOriginalFilename();
                String ext = originalFilename != null && originalFilename.contains(".")
                        ? originalFilename.substring(originalFilename.lastIndexOf("."))
                        : "";
                String newFilename = UUID.randomUUID().toString() + ext;
                Path targetPath = uploadPath.resolve(newFilename);

                //Files.copy забирает содержимое загруженного файла и сохраняет в папку uploads.
                Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

                //Создаём новый объект для вложенного файла и заполняем его поля
                //связывая его с записью, записываем путь к файлу и сохраняем в репозиторий.
                FileAttachment fa = new FileAttachment();
                fa.setRecord(record);
                fa.setFilepath(newFilename);
                fileAttachmentRepository.save(fa);
            }
        } catch (IOException e) {
            //если что-то пошло не так (нет прав на запись, кончилось место),
            //оборачиваем IOException в RuntimeException, что приведёт к откату
            //транзакции и покажет ошибку пользователю.
            throw new RuntimeException("Ошибка сохранения файла", e);
        }
    }
}