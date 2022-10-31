package erdem.ExcelOku.Entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

    @Entity
    @Table(name = "bank_mid_tid")
    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public class BankMidTid {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private long id;

        @Column(name = "tid")
        private String tid;

        @Column(name = "mid")
        private String mid;

        @Column(name = "merchant_name")
        private String merchantName;

        @Column(name = "serial_no")
        private String serialNo;

        @Column(name = "device_status")
        private String deviceStatus;

        @Column(name = "bank_name")
        private String bankName;

        @Transient
        private String midTidSerial;
    }
