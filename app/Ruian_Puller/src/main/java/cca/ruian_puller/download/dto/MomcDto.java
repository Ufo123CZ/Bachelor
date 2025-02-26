package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class MomcDto {
    private Integer kod;
    private String nazev;
    private Boolean nespravny;
    private Integer mop;
    private Integer obec;
    private Integer spravniobvod;
    private LocalDateTime platiod;
    private LocalDateTime platido;
    private Long idtransakce;
    private Long globalniidnavrzmeny;
    private String vlajkatext;
    private byte[] vlajkaobrazek;
    private String znaktext;
    private String mluvnickecharakteristiky;
    private byte[] znakobrazek;
    private String geometrie;
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}