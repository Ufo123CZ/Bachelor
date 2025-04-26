package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UliceBoolean {
    // Elements from config file
    public static final String NAZEV = "nazev";
    public static final String NESPRAVNY = "nespravny";
    public static final String OBEC = "obec";
    public static final String PLATIOD = "platiod";
    public static final String PLATIDO = "platido";
    public static final String IDTRANSAKCE = "idtransakce";
    public static final String GLOBALNIIDNAVRHUZMENY = "globalniidnavrhuzmeny";
    public static final String GEOMETRIEDEFBOD = "geometriedefbod";
    public static final String GEOMETRIEDEFCARA = "geometriedefcara";
    public static final String NESPRAVNEUDAJE = "nespravneudaje";

    // Values of elements
    private boolean nazev;
    private boolean nespravny;
    private boolean obec;
    private boolean platiod;
    private boolean platido;
    private boolean idtransakce;
    private boolean globalniidnavrhuzmeny;
    private boolean geometriedefbod;
    private boolean geometriedefcara;
    private boolean nespravneudaje;

    // How to process
    private final String howToProcess;

    /**
     * Constructor for UliceBoolean.
     *
     * @param howToProcess the processing method
     */
    public UliceBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.nazev = true;
            this.nespravny = true;
            this.obec = true;
            this.platiod = true;
            this.platido = true;
            this.idtransakce = true;
            this.globalniidnavrhuzmeny = true;
            this.geometriedefbod = true;
            this.geometriedefcara = true;
            this.nespravneudaje = true;
        }
    }
}
