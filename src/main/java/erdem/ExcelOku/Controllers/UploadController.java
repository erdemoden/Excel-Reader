package erdem.ExcelOku.Controllers;

import erdem.ExcelOku.Entities.BankMidTid;
import erdem.ExcelOku.Services.BankMidTidService;
import erdem.ExcelOku.Services.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@CrossOrigin
public class UploadController {

    private final UploadService uploadService;
    private final BankMidTidService bankMidTidService;

    public static final String RedisKey = "ISPROCESSED";


    @PostMapping("/midtid")
    public ResponseEntity uploadExcel(@RequestParam("file") MultipartFile file) {
        bankMidTidService.returnReadingExcelAsList(uploadService.uploadFile(file));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/checkcache")
    public boolean checkRedis(){
        return bankMidTidService.isExist(RedisKey);
    }

    @GetMapping
    public Page<BankMidTid> listByPagination(@RequestParam Integer pageSize, @RequestParam Integer page){
        return bankMidTidService.findAll(pageSize,page);
    }
}


