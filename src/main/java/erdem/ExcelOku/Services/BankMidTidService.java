package erdem.ExcelOku.Services;

import erdem.ExcelOku.Configs.RedisCacheStore;
import erdem.ExcelOku.Entities.BankMidTid;
import erdem.ExcelOku.Repositories.BankMidTidRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.util.CellUtil;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import org.springframework.data.domain.Pageable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
@Service
public class BankMidTidService {

    private final BankMidTidRepository bankMidTidRepo;
    private final  RedisCacheStore redisCacheStore;
    public static final String RedisKey = "ISPROCESSED";
    public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
        Map<Object, Boolean> map = new ConcurrentHashMap<>();
        return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }

    public void checkMidTidSerial(List<BankMidTid> midTids) {
        List<BankMidTid> batchList = new ArrayList<>();
        List<BankMidTid> willReturn = midTids.stream().filter(distinctByKey(BankMidTid::getMidTidSerial)).collect(Collectors.toList());

        willReturn.forEach(midTid -> {
            if (bankMidTidRepo.existsByMidAndTidAndSerial(midTid.getMid(), midTid.getTid(), midTid.getSerialNo())) {
                batchList.add(midTid);
            }
            if (batchList.size() > 10000) {
                bankMidTidRepo.saveAll(batchList);
                batchList.removeAll(batchList);
            }
        });
        if (!batchList.isEmpty()) {
            bankMidTidRepo.saveAll(batchList);
            batchList.removeAll(batchList);
        }
    }


    @Async
    public void returnReadingExcelAsList(String fileName) {

        /*if(isExist(RedisKey)){
            File excelToDelete = new File(System.getProperty("user.dir") + "\\" + fileName);

            if (excelToDelete.delete()) {
                log.info("excel file has deleted");
            } else {
                log.error("excel file couldn't delete");
            }
        }
        redisCacheStore.put(RedisKey,"processing",13,TimeUnit.MINUTES);*/
        if(!isExist(RedisKey)) {
            FileInputStream fis = null;
            XSSFWorkbook workBook = null;
            try {
                fis = new FileInputStream(System.getProperty("user.dir") + "/" + fileName);
                workBook = new XSSFWorkbook(fis);
            } catch (IOException e) {
                log.error("IOException happened");
            }

            assert workBook != null;

            List<BankMidTid> bankMidTidList = new ArrayList<>();

            DataFormatter formatter = new DataFormatter();

            XSSFSheet sheet = workBook.getSheetAt(0);

            for (Row row : sheet) {
                if (row.getRowNum() > 0) {
                    String midTidSerial = formatter.formatCellValue(CellUtil.getCell(row, 0)) + ":" + formatter.formatCellValue(CellUtil.getCell(row, 1)) + ":" + CellUtil.getCell(row, 3).toString();
                    bankMidTidList.add(bankMidTidBuilder(formatter.formatCellValue(CellUtil.getCell(row, 0)), formatter.formatCellValue(CellUtil.getCell(row, 1)), CellUtil.getCell(row, 2).toString(), CellUtil.getCell(row, 3).toString(), CellUtil.getCell(row, 4).toString(), CellUtil.getCell(row, 5).toString(), midTidSerial));
                }
            }

            checkMidTidSerial(bankMidTidList);

            try {
                fis.close();
            } catch (IOException e) {
                log.error("We couldn't close The Excel File");
            }

            File excelFile = new File(System.getProperty("user.dir") + "/" + fileName);

            if (excelFile.delete()) {
                log.info("excel file has deleted");
                redisCacheStore.evict(RedisKey);
            } else {
                log.error("excel file couldn't delete");
            }
        }
    }
    public BankMidTid bankMidTidBuilder(String tid, String mid, String merchantName, String serialNo, String deviceStatus, String bankName, String midTidSerial) {

        return BankMidTid.builder()
                .tid(tid)
                .mid(mid)
                .merchantName(merchantName)
                .serialNo(serialNo)
                .deviceStatus(deviceStatus)
                .bankName(bankName)
                .midTidSerial(midTidSerial)
                .build();
    }
    public boolean isExist(String redisKey) {
        String obj = redisCacheStore.get(redisKey, String.class);
        if(obj==null) {
            redisCacheStore.put(RedisKey, "processing", 5, TimeUnit.MINUTES);
        }
        return obj != null;
    }

    public Page<BankMidTid> findAll(int pageSize, int page){
        Pageable pageable = PageRequest.of(page,pageSize);
        return bankMidTidRepo.findAll(pageable);
    }
}
