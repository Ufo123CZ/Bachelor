package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class VODto {
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String geometrie;
    private String nespravneudaje;
    private Integer kod;
    private Integer cislo;
    private Boolean nespravny;
    private Integer obec;
    private Integer momc;
    private String poznamka;
}

