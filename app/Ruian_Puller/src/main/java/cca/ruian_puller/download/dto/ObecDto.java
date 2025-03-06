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
@Table(name = "obec")
@ToString
public class ObecDto {
    @Id
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer statuskod;
    private Integer okres;
    private Integer pou;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    @JdbcTypeCode(SqlTypes.JSON)
    private String mluvnickecharakteristiky;    // JSON
    private String vlajkatext;
    private byte[] vlajkaobrazek;
    private String znaktext;
    private byte[] znakobrazek;
    private Integer clenenismrozsahkod;
    private Integer clenenismtypkod;
    private String nutslau;
    private Geometry geometrie;   // Geometry
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;  // JSON
    private LocalDateTime datumvzniku;
}