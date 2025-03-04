package cca.ruian_puller.download.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "zaniklyprvek")
@ToString
public class ZaniklyPrvekDto {
    @Id
    private String TypPrvkuKod;
    private Long PrvekId;
    private Long IdTransakce;
}