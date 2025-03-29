package cca.ruian_puller.config.configObjects;

import cca.ruian_puller.config.NodeConst;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ZaniklyPrvekBoolean {
    // Elements from config file
    public static final String TYPPRVKUKOD = "typprvkukod";
    public static final String IDTRANSAKCE = "idtransakce";

    // Values of elements
    public boolean typprvkukod;
    public boolean idtransakce;

    // How to process
    private final String howToProcess;

    public ZaniklyPrvekBoolean(String howToProcess) {
        this.howToProcess = howToProcess;

        if (howToProcess.equals(NodeConst.HOW_OF_PROCESS_ELEMENT_ALL)) {
            this.typprvkukod = true;
            this.idtransakce = true;
        }
    }
}