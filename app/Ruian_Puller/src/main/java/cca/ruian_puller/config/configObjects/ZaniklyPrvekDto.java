package cca.ruian_puller.config.configObjects;

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
    private String typprvkukod;
    @Id
    private Long prvekid;
    private Long idtransakce;
}