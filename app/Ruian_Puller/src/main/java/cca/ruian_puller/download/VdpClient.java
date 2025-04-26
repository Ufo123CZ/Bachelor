package cca.ruian_puller.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipFile;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class VdpClient {

    private static final Pattern linkDatePattern = Pattern.compile("/(\\d{8}_)");

    private static final int MAX_POCET_POKUSU_STAZENI = 100;

    @Value("${vdp.stat.vyhledej:https://vdp.cuzk.cz/vdp/ruian/vymennyformat?crKopie=on&casovyRozsah=U&upStatAzZsj=on&uzemniPrvky=ST&dsZakladni=on&datovaSada=Z&vyZakladni=on&vyber=vyZakladni&search=Vyhledat}")
    private String vdpStatUrlVyhledej;

    @Value("${vdp.stat.seznam:https://vdp.cuzk.cz/vdp/ruian/vymennyformat?crPrirustky&crKopie=true&page&casovyRozsah=U&datum&upStatAzZsj=true&upObecAPodrazene=false&uzemniPrvky=ST&dsZakladni=true&dsKompletni=false&datovaSada=Z&vyZakladni=true&vyZakladniAGenHranice=false&vyZakladniAOrigHranice=false&vyVlajkyAZnaky=false&vyber=vyZakladni&kodVusc&kodOrp&kodOb&mediaType=text&search=Vyhledat}")
    private String vdpStatUrlSeznam;

    @Value("${vdp.kraj.vyhledej:https://vdp.cuzk.cz/vdp/ruian/vymennyformat?crKopie=on&casovyRozsah=U&upObecAPodrazene=on&uzemniPrvky=OB&dsZakladni=on&datovaSada=Z&vyZakladni=on&vyber=vyZakladni&uzemniOmezeni=on&search=Vyhledat&kodVusc=}")
    private String vdpKrajUrlVyhledej;

    @Value("${vdp.kraj.seznam:https://vdp.cuzk.cz/vdp/ruian/vymennyformat?crPrirustky&crKopie=true&page&casovyRozsah=U&datum&upStatAzZsj=false&upObecAPodrazene=true&uzemniPrvky=OB&dsZakladni=true&dsKompletni=false&datovaSada=Z&vyZakladni=true&vyZakladniAGenHranice=false&vyZakladniAOrigHranice=false&vyVlajkyAZnaky=false&vyber=vyZakladni&kodOrp&kodOb&mediaType=text&search=Vyhledat&kodVusc=}")
    private String vdpKrajUrlSeznam;

    @Autowired
    private VdpDownload vdpDownload;


    /**
     * Downloads and unzips the data from the given URL.
     *
     * @param consumer the consumer for processing the input stream
     */
    public void zpracovatStatAzZsj(final Consumer<InputStream> consumer) {
        // First save filter to session
        saveFilter(vdpStatUrlVyhledej);

        final AtomicReference<String> odkaz = new AtomicReference<>();
        get(vdpStatUrlSeznam, inputStream -> {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                odkaz.set(bufferedReader.readLine());
                log.info("Stat az zsj link: {}", odkaz.get());
                bufferedReader.close();
            } catch (final IOException e) {
                log.error("Error while processing stat az zsj.", e);
            }
        });
        unzipContent(odkaz.get(), consumer);
    }

    //region Kraj and obce
    /**
     * Downloads and unzips the data for the given region code.
     *
     * @param krajKod  the region code
     */
    public List<String> getListLinksObce(final Integer krajKod) {
        saveFilter(vdpKrajUrlVyhledej + krajKod);
        final List<String> result = new ArrayList<>(1000);
        get(vdpKrajUrlSeznam + krajKod, inputStream -> {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                bufferedReader.lines().forEach(result::add);
                bufferedReader.close();
            } catch (final IOException e) {
                log.error("Error while getting list of links.", e);
            }
        });
        // since the links are returned for 3 months, we must filter them
        final Matcher matcher = linkDatePattern.matcher(result.get(0));
        matcher.find();
        final String filter = matcher.group(1);
        return result.stream().filter(s -> s.contains(filter)).toList();
    }

    /**
     * Downloads and unzips the data from the given links.
     *
     * @param links   the list of links to download
     * @param consumer the consumer for processing the input stream
     */
    public void downloadFilesFromLinks(List<String> links, Consumer<InputStream> consumer) {
        int iter = 1;
        for (String link : links) {
            log.info("Downloading {}. file from link: {}",iter, link);
            log.info("Data processing started.");
            unzipContent(link, consumer);
            log.info("Data processing for file {}. finished.", iter++);
        }
    }
    //endregion

    //region Prirustky
    /**
     * Downloads and unzips the data for additions.
     *
     * @param consumer the consumer for processing the input stream
     */
    public void getAdditions(final Consumer<InputStream> consumer) {
        String[] urlVyhledej = generateUrlWithPreviousDateVyhledej();
        String urlSeznam = generateUrlWithPreviousDateSeznam();
        // First save filter to session
        saveFilter(urlVyhledej[1]);

        final AtomicReference<String> odkaz = new AtomicReference<>();
        get(urlSeznam, inputStream -> {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                odkaz.set(bufferedReader.readLine());
                log.info("Prirustky za den {} : {}", urlVyhledej[0], odkaz.get());
                bufferedReader.close();
            } catch (final IOException e) {
                log.error("Error while processing prirustky.", e);
            }
        });
        unzipContent(odkaz.get(), consumer);
    }

    /**
     * Generates the URL for the previous day's data.
     *
     * @return the URL for the previous day's data
     */
    public String[] generateUrlWithPreviousDateVyhledej() {
        LocalDate previousDay = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = previousDay.format(formatter);
        return new String[] {formattedDate, "https://vdp.cuzk.gov.cz/vdp/ruian/vymennyformat?crPrirustky=on&datum=" + formattedDate + "&casovyRozsah=Z&upStatAzZsj=on&uzemniPrvky=ST&dsZakladni=on&datovaSada=Z&vyZakladni=on&vyber=vyZakladni&kodOrp=&search="};
    }

    /**
     * Generates the URL for the previous day's data.
     *
     * @return the URL for the previous day's data
     */
    public String generateUrlWithPreviousDateSeznam() {
        LocalDate previousDay = LocalDate.now().minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = previousDay.format(formatter);
        return "https://vdp.cuzk.gov.cz/vdp/ruian/vymennyformat?crPrirustky=true&crKopie&page&casovyRozsah=Z&datum=" + formattedDate + "&upStatAzZsj=true&upObecAPodrazene=false&uzemniPrvky=ST&dsZakladni=true&dsKompletni=false&datovaSada=Z&vyZakladni=true&vyZakladniAGenHranice=false&vyZakladniAOrigHranice=false&vyVlajkyAZnaky=false&vyber=vyZakladni&kodVusc&kodOrp&kodOb&mediaType=text";
    }
    //endregion

    /**
     * Downloads and unzips the content from the given URL.
     *
     * @param url      the URL to download from
     * @param consumer the consumer for processing the input stream
     */
    public void unzipContent(final String url, final Consumer<InputStream> consumer) {
        get(url, inputStream -> {
            final File tmpZip = saveZipToTemp(inputStream);
            try {
                unzipFile(tmpZip, consumer);
            } finally {
                tryDeleteTempFile(tmpZip);
            }
        });
    }

    /**
     * Unzips the given zip file and processes the input stream with the given consumer.
     *
     * @param zipFile  the zip file to unzip
     * @param consumer the consumer for processing the input stream
     */
    public void unzipFile(final File zipFile, final Consumer<InputStream> consumer) {
        try (final ZipFile zip = new ZipFile(zipFile)) {
            consumer.accept(zip.getInputStream(zip.entries().nextElement()));
        } catch (final IOException e) {
            log.error("Error while unzipping file.", e);
        }
    }

    /**
     * Saves the zip file to a temporary location.
     *
     * @param inputStream the input stream of the zip file
     * @return the temporary zip file
     */
    private File saveZipToTemp(final InputStream inputStream) {
        try {
            final File tmpZip = Files.createTempFile("ruian", ".zip").toFile();
            try (final FileOutputStream tmpZipOS = new FileOutputStream(tmpZip)) {
                IOUtils.copy(inputStream, tmpZipOS);
            }
            return tmpZip;
        } catch (final IOException e) {
            log.error("Error while saving zip file to temp.", e);
            return null;
        }
    }

    /**
     * Deletes the temporary zip file.
     *
     * @param tmpZip the temporary zip file to delete
     */
    private void tryDeleteTempFile(final File tmpZip) {
        try {
            Files.deleteIfExists(tmpZip.toPath());
        } catch (final IOException e) {
            log.error("Error while deleting temp file.", e);
        }
    }

    /**
     * Downloads the content from the given URL and processes it with the given consumer.
     *
     * @param url      the URL to download from
     * @param consumer the consumer for processing the input stream
     */
    private void get(final String url, final Consumer<InputStream> consumer) {
        for (int pocetPokusu = 0; pocetPokusu < MAX_POCET_POKUSU_STAZENI; pocetPokusu++) {
            try {
                vdpDownload.tryGet(url, consumer);
                if (Boolean.getBoolean("save.zips") && url.endsWith("zip")) {
                    vdpDownload.tryGet(url, is -> {
                        try (final FileOutputStream f = new FileOutputStream(url.substring(url.lastIndexOf('/') + 1))) {
                            IOUtils.copy(is, f);
                        } catch (final IOException e) {
                            log.error("Error while saving zip file.", e);
                        }
                    });
                }
                return;
            } catch (final IOException e) {
                log.error("Error while downloading file.", e);
            }
        }
    }

    /**
     * Saves the filter to the session.
     *
     * @param url the URL to save
     */
    private void saveFilter(final String url) {
        for (int pocetPokusu = 0; pocetPokusu < MAX_POCET_POKUSU_STAZENI; pocetPokusu++) {
            try {
                vdpDownload.trySaveFilter(url);
                return;
            } catch (final IOException e) {
                log.error("Error while saving filter.", e);
            }
        }
    }

}
