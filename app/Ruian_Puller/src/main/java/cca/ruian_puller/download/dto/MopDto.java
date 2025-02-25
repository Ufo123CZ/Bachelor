package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class MopDto {
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer obec;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrzmeny;
    private String geometrie;
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}