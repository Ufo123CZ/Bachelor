package cca.ruian_puller.download.dto;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ZaniklyPrvekDto {
    private String TypPrvkuKod;
    private Long PrvekId;
    private Long IdTransakce;
}