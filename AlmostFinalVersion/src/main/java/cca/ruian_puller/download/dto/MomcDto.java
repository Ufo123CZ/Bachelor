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
@Table(name = "momc")
@ToString
public class MomcDto {
    @Id
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer mop;
    private Integer obec;
    private Integer spravniobvod;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String vlajkatext;
    private byte[] vlajkaobrazek;
    private String znaktext;
    private byte[] znakobrazek;
    @JdbcTypeCode(SqlTypes.JSON)
    private String mluvnickecharakteristiky;
    private Geometry geometriedefbod;
    private Geometry geometrieorihranice;
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}