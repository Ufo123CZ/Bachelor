package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class UliceDto {
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer obec;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrhuzmeny;
    private String geometrie;
    private String nespravneudaje;
}
