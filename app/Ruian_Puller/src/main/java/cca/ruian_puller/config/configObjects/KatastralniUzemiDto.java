package cca.ruian_puller.config.configObjects;

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
@Table(name = "katastralniuzemi")
@ToString
public class KatastralniUzemiDto {
    @Id
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Boolean existujedigitalnimapa;
    private Integer obec;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private Long rizeniid;
    @JdbcTypeCode(SqlTypes.JSON)
    private String mluvnickecharakteristiky;    // JSON
    private Geometry geometriedefbod;  // Geometry 0
    private Geometry geometriegenhranice;  // Geometry 1
    private Geometry geometrieorihranice;  // Geometry 2
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}