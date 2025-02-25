package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@ToString
public class ObecDto {
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
    private String mluvnickecharakteristiky;
    private String vlajkatext;
    private byte[] vlajkaobrazek;
    private String znaktext;
    private byte[] znakobrazek;
    private Integer clenenisrozsahtypkod;
    private Integer clenenismtykod;
    private String nutslau;
    private String geometrie;
    private String nespravneudaje;
    private LocalDateTime datumvzniku;
}