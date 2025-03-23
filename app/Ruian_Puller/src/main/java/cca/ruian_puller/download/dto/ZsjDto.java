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
@Table(name = "zsj")
@ToString
public class ZsjDto {
    @Id
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer katastralniuzemi;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    @JdbcTypeCode(SqlTypes.JSON)
    private String mluvnickecharakteristiky;
    private Long vymera;
    private Integer charakterzsjkod;
    private Geometry geometriedefbod;
    private Geometry geometrieorihranice;
    @JdbcTypeCode(SqlTypes.JSON)
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}