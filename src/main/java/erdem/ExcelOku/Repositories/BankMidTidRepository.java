package erdem.ExcelOku.Repositories;

import erdem.ExcelOku.Entities.BankMidTid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BankMidTidRepository extends JpaRepository<BankMidTid, Long> {

    @Query(value = "SELECT CASE when count(*) > 0 then 'false' else 'true' end from bank_mid_tid  where mid = :mid and tid =:tid and serial_no = :serialNo", nativeQuery = true)
    Boolean existsByMidAndTidAndSerial(@Param("mid") String mid, @Param("tid") String tid, @Param("serialNo") String serialNo);

}