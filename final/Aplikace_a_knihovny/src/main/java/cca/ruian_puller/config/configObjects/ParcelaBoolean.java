package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ParcelaBoolean {
    // Elements from config file
    public static final String NESPRAVNY = "nespravny";
    public static final String KMENOVECISLO = "kmenovecislo";
    public static final String PODODDELENICISLA = "pododdelenicisla";
    public static final String VYMERAPARCELY = "vymeraparcely";
    public static final String ZPUSOBYVYUZITIPOZEMKU = "zpusobyvyuzitipozemku";
    public static final String DRUHCISLOVANIKOD = "druhcislovanikod";
    public static final String DRUHPOZEMKUKOD = "druhpozemkukod";
    public static final String KATASTRALNI_UZEMI = "katastralniuzemi";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String RIZENIID = "rizeniid";
    public static final String BONITOVANEDILY = "bonitovanedily";
    public static final String ZPUSOBYOCHRANYPOZEMKU = "zpusobyochranypozemku";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String GEOMETRIEORIHRANICE = "geometrieorihranice";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";

    // Values of elements
    private boolean nespravny;
    private boolean kmenovecislo;
    private boolean pododdelenicisla;
    private boolean vymeraparcely;
    private boolean zpusobyvyuzitipozemku;
    private boolean druhcislovanikod;
    private boolean druhpozemkukod;
    private boolean katastralniuzemi;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean rizeniid;
    private boolean bonitovanedily;
    private boolean zpusobyochranypozemku;
    private boolean geometriedefbod;
    private boolean geometrieorihranice;
    private boolean nespravneudaje;

    // How to process
    private final String howToProcess;

    /**
     * Constructor for ParcelaBoolean.
     *
     * @param howToProcess the processing method
     */
    public ParcelaBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nespravny = true;
            this.kmenovecislo = true;
            this.pododdelenicisla = true;
            this.vymeraparcely = true;
            this.zpusobyvyuzitipozemku = true;
            this.druhcislovanikod = true;
            this.druhpozemkukod = true;
            this.katastralniuzemi = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.rizeniid = true;
            this.bonitovanedily = true;
            this.zpusobyochranypozemku = true;
            this.geometriedefbod = true;
            this.geometrieorihranice = true;
            this.nespravneudaje = true;
        }
    }
}