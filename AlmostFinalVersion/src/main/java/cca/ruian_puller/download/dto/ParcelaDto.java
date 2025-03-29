package cca.ruian_puller.download.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "parcela")
@ToString
public class ParcelaDto {
    @Id
    private Long id;
    private Boolean nespravny;
    private Integer kmenovecislo;
    private Integer pododdelenicisla;
    private Long vymeraparcely;
    private Integer zpusobyvyuzitipozemku;
    private Integer druhcislovanikod;
    private Integer druhpozemkukod;
    private Integer katastralniuzemi;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long rizeniid;
    @JdbcTypeCode(SqlTypes.JSON)
    private String bonitovanedily;
    @JdbcTypeCode(SqlTypes.JSON)
    private String zpusobyochranypozemku;
    private Geometry geometriedefbod;
    private Geometry geometrieorihranice;
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;
}