package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.locationtech.jts.geom.Geometry;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
public class ObecBoolean {
    // Elements from config file
    public static final String NAZEV = "nazev";
    public static final String NESPRAVNY = "nespravny";
    public static final String STATUSKOD = "statuskod";
    public static final String OKRES = "okres";
    public static final String POU = "pou";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String GLOBALNIIDNAVRHUZMENY = "globalniidnavrhuzmeny";
    public static final String MLUVNICKECHARAKTERISTIKY = "mluvnickecharakteristiky";
    public static final String VLAJKATEXT = "vlajkatext";
    public static final String VLAJKAOBRAZEK = "vlajkaobrazek";
    public static final String ZNAKTEXT = "znaktext";
    public static final String ZNAKOBRAZEK = "znakobrazek";
    public static final String CLENENISMROZSAHKOD = "clenenismrozsahkod";
    public static final String CLENENISMTYPKOD = "clenenismtypkod";
    public static final String NUTSLAU = "nutslau";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String GEOMETRIEGENHRANICE = "geometriegenhranice";
    public static final String GEOMETRIEORIHRANICE = "geometrieorihranice";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";
    public static final String DATUMVZNIKU = "datumvzniku";

    // Values of elements
    private boolean nazev;
    private boolean nespravny;
    private boolean statuskod;
    private boolean okres;
    private boolean pou;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean globalniidnavrhuzmeny;
    private boolean mluvnickecharakteristiky;
    private boolean vlajkatext;
    private boolean vlajkaobrazek;
    private boolean znaktext;
    private boolean znakobrazek;
    private boolean clenenismrozsahkod;
    private boolean clenenismtypkod;
    private boolean nutslau;
    private boolean geometriedefbod;
    private boolean geometriegenhranice;
    private boolean geometrieorihranice;
    private boolean nespravneudaje;
    private boolean datumvzniku;

    // How to process
    private final String howToProcess;

    /**
     * Constructor for ObecBoolean.
     *
     * @param howToProcess the processing method
     */
    public ObecBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nazev = true;
            this.nespravny = true;
            this.statuskod = true;
            this.okres = true;
            this.pou = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.globalniidnavrhuzmeny = true;
            this.mluvnickecharakteristiky = true;
            this.vlajkatext = true;
            this.vlajkaobrazek = true;
            this.znaktext = true;
            this.znakobrazek = true;
            this.clenenismrozsahkod = true;
            this.clenenismtypkod = true;
            this.nutslau = true;
            this.geometriedefbod = true;
            this.geometriegenhranice = true;
            this.geometrieorihranice = true;
            this.nespravneudaje = true;
            this.datumvzniku = true;
        }
    }
}