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
@Table(name = "adresnimisto")
@ToString
public class AdresniMistoDto {
    @Id
    private Integer kod;
    private Boolean nespravny;
    private Integer cislodomovni;
    private Integer cisloorientacni;
    private String cisloorientacnipismeno;
    private Integer psc;
    private Integer stavebniobjekt;
    private Integer ulice;
    private Integer vokod;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private Geometry geometriedefbod;
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;
}