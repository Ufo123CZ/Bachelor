package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class KatastrUzemiDto {
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Boolean existujedigitalnimapa;
    private Integer obec;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrzmeny;
    private Long rizeniid;
    private String mluvnickecharakteristiky;
    private String geometrie;
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}