package erdem.ExcelOku.Services;

import erdem.ExcelOku.Configs.RedisCacheStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
@Slf4j
public class UploadService {

    public String uploadFile(MultipartFile file) {

        SimpleDateFormat formatterYear = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat formatterHour = new SimpleDateFormat("HH-mm-ss");

        Date date = new Date(System.currentTimeMillis());
        String dateMerged = formatterYear.format(date) + " AT " + formatterHour.format(date);
        String path = System.getProperty("user.dir") + "/";
        Path saveTO = Paths.get(path, dateMerged);

        try {
            Files.copy(file.getInputStream(), saveTO);
        } catch (IOException e) {
            log.error("IOException Happened");
        }

        return dateMerged;
    }
}
