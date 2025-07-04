# Ruian Puller Manual

## Co je to Ruian Puller?
Tato aplikace stahuje data z [VDP](https://vdp.cuzk.cz/vdp/ruian/vymennyformat) a ukládá je to SQL Databáze.

Podporované databáze: 
- **Microsoft SQL**
- **PostgreSQL**
- **Oracle**

Data jsou stahována ve formátu XML a převáděna do Dto (Data Transfer Object), které jsou následně ukládány do databáze pomocí JPA (Java Persistence API).
Aplikace je napsána v Javě 21 a používá Spring Boot pro snadné nastavení a konfiguraci. Všechny závislosti jsou spravovány pomocí Maven.
Aplikace dokáže stahovat a ukládat standardní datové sady. Přesněji se jedná o:
  - Stát až ZSJ (základní)
  - Všechny kraje a obce (základní)
  - Přírůstková data (základní)

# Před prvním spuštěním
Je třeba mít zprovozněné SQL databáze pro ukládání dat.
Databáze může běžet podle vlastních nastavení a nebo je zde Docker varianta.
V adresáři database se nachází podadresáře pro každou z podporovaných databází.
Každý podadresář obsahuje docker-compose.yml a init.sql, které vytvoří databázi a tabulky pro ukládání dat. Jediná databáze, která neobsahuje docker-compose.yml je Oracle, protože Oracle byla vytvořena mimo Docker.

## Docker MS SQL a PostgreSQL
Pro spuštění v Docker je zde adresář db, kde jsou dva podadresáře
PostgreSQL a MS SQL. V každém je příslušný docker-compose.yml a init.sql

Pro vytvoření těchto instancí je třeba v příslušném adresáři:
```bash
docker-compose up -d
```
Pro případné vyčištění:
```bash
docker-compose down -v
```

První příkaz automaticky stáhne docker image pro PostgresSQL(latest) nebo MS SQL(2017:latest) databázi.
Po stažení image a spuštění kontejneru se automaticky spustí init.sql, který vytvoří databázi a tabulky.

## Oracle
Oracle databáze je vytvořena mimo Docker. Je třeba mít nainstalovaný Oracle Database 19c nebo vyšší.
Následně pro vytvoření instance je třeba použít init.sql pro vytvoření tabulek.
Dále je chování databáze stejné jako u předchozích dvou.

## Aplikace
Aplikace se nachází v adresáři app, kde je umístěn zdrojový kód a Maven projekt.
Aplikace je napsána v Javě 21 a používá Spring Boot pro snadné nastavení a konfiguraci. 
Všechny závislosti jsou spravovány pomocí Maven.
Projekt je zároveň nastaven na JDK 21 a Maven 3.8.6 a je spustitelný v IntelliJ IDEA nebo Eclipse.

# Konfigurace
Konfigurace neboli **config.json** je soubor, který obsahuje nastavení pro připojení k databázi, jaké tabulky se mají ukládat a další. Nachází se v Projektu aplikace v podadresáři src/main/resources.

Pro úspěšné spuštění aplikace je třeba mít správně nastavený config.json.
Co je se nachází a je potřeba mít v config.json:
```json
{
  "database": {
    "type": "<databse_type>",
    "url": "<connection_string>",
    "dbname": "<database_name>",
    "username": "<username>",
    "password": "<password>"
  },
  "quartz": {
    "cron": "0 0 2 * * ?",
    "skipInitialRunStat": false,
    "skipInitialRunRegion": true
  },
  "vuscCodes": {
    "<kod>": "<kraj_nazev>",
    "<kod>": "<kraj_nazev>",
    "<kod>": "<kraj_nazev>"
  },
  "additionalOptions": {
    "includeGeometry": true,
    "commitSize": 1000
  },
  "dataToProcess": {
    "howToProcess": "<all/selected>",
    "tables": {
        "<tablename>": {
        "howToProcess": "<all,include,exclude>",
        "columns": ["<column_name>", ..., "<column_name>"]
        },
        "<tablename>": {
            "howToProcess": "<all,include,exclude>",
            "columns": ["<column_name>", ..., "<column_name>"]
        }
    }
  }
}
```
## Popis jednotlivých částí
- **database**: nastavení pro připojení k databázi
  - **type**: typ databáze (postgresql, mssql, oracle)
  - **url**: connection string pro připojení k databázi
  - **dbname**: název databáze
  - **username**: uživatelské jméno pro připojení k databázi
  - **password**: heslo pro připojení k databázi

- **quartz**: nastavení pro Quartz Scheduler
    - **cron**: cron expression pro plánování stahování dat (např. "0 0 1 * * ?") - defaultně je nastaveno na "0 0 1 * * ?"
    - **skipInitialRunStat**: zda přeskočit počáteční stažení Stáru až ZSJ (true/false)
    - **skipInitialRunRegion**: zda přeskočit počáteční stažení vybraných kraje (true/false)

- **vuscCodes**: nastavení pro vybrané kraje a obce (pokud je prázdné, nestahují se žádné kraje a obce)
    - **kod**: kód kraje
    - **kraj_nazev**: název kraje nebo obce (např. "Vysočina")
    List kraj; a jmen:

    | kod   | kraj_nazev            |
    |:-----:|:---------------------:|
    | 19    | Hlavní město Praha    |
    | 27    | Jihočeský kraj        |
    | 35    | Jihomoravský kraj     |
    | 43    | Karlovarský kraj      |
    | 51    | Kraj Vysočina         |
    | 60    | Královéhradecký kraj  |
    | 78    | Liberecký kraj        |
    | 86    | Moravskoslezský kraj  |
    | 94    | Olomoucký kraj        |
    | 108   | Pardubický kraj       |
    | 116   | Plzeňský kraj         |
    | 124   | Středočeský kraj      |
    | 132   | Ústecký kraj          |
    | 141   | Zlínský kraj          |

- **additionalOptions**: další možnosti nastavení
    - **includeGeometry**: zda zahrnout geometrii (true/false) - defaultně je nastaveno na false
    - **commitSize**: velikost transakce pro ukládání dat (defaultně 1000)

- **dataToProcess**: nastavení pro zpracování dat
    - **howToProcess**: jak zpracovat data (all/selected)
        - **all**: stáhnout všechna data a nefiltrovat
        - **selected**: stáhnout pouze vybrané tabulky a sloupce
    - **tables**: tabulky pro zpracování dat (ignorováno pokud je nastaveno all)
        - **tablename**: název tabulky
            - **howToProcess**: jak zpracovat tabulku (all, include, exclude) - defaultně je nastaveno na all
            - **columns**: sloupce pro zpracování tabulky (pokud je nastaveno all u tabulky, ignorováno)
                - **all**: stáhnout všechny sloupce
                - **include**: stáhnout pouze vybrané sloupce
                - **exclude**: stáhnout všechny sloupce kromě vybraných

    Seznam tabulek a sloupců pro zpracování dat:
    | Tabulka | Columns |
    |:-------:|:------- |
    | stat | nazev, nespravny, platiod, platido, idtransakce, globalniidnavrhuzmeny, nutslau, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | regionSoudrznosti | nazev, nespravny, stat, platiod, platido, idtransakce, globalniidnavrhuzmeny, nutslau, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | vusc | nazev, nespravny, regionsoudrznosti, platiod, platido, idtransakce, globalniidnavrhuzmeny, nutslau, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | okres | nazev, nespravny, kraj, vusc, platiod, platido, idtransakce, globalniidnavrhuzmeny, nutslau, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | orp | nazev, nespravny, spravniobeckod, vusc, okres, platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | pou | nazev, nespravny, spravniobeckod, orp, platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | obec | nazev, nespravny, statuskod, okres, pou, platiod, platido, idtransakce, globalniidnavrhuzmeny, mluvnickecharakteristiky, vlajkatext, vlajkaobrazek, znaktext, znakobrazek, clenenismrozsahkod, clenenismtypkod, nutslau, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | castObce | nazev, nespravny, obec, platiod, platido, idtransakce, globalniidnavrhuzmeny, mluvnickecharakteristiky, geometriedefbod, nespravneudaje, datumvzniku |
    | mop | nazev, nespravny, obec, platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, geometrieorihranice, nespravneudaje, datumvzniku |
    | spravniObvod | nazev, nespravny, spravnimomckod, obec, platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, geometrieorihranice, nespravneudaje, datumvzniku |
    | momc | nazev, nespravny, spravniobvod, mop, obec, spravniobvod, platiod, platido, idtransakce, globalniidnavrhuzmeny, vlajkatext, vlajkaobrazek, znaktext, znakobrazek, mluvnickecharakteristiky, geometriedefbod, geometrieorihranice, nespravneudaje, datumvzniku |
    | katastralniUzemi | nazev, nespravny, existujedigitalnimapa, obec, platiod, platido, idtransakce, globalniidnavrhuzmeny, rizeniid, mluvnickecharakteristiky, geometriedefbod, geometriegenhranice, nespravneudaje, datumvzniku |
    | parcela | nespravny, kmenovecislo, pododdelenicisla, vymeraparcely, zpusobyvyuzitipozemku, druhcislovanikod, druhpozemkukod, katastralniuzemi, platiod, platido, idtransakce, rizeniid, bonitovanedily, zpusobyochranypozemku, geometriedefbod, geometrieorihranice, nespravneudaje |
    | ulice | nespravny, obec, platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, geometriedefcara, nespravneudaje |
    | stavebniObjekt | nazev, cislodomovni, identifikacniparcela, typstavebnihoobjektukod, castobce, momc, platiod, platido, idtransakce, globalniidnavrhuzmeny, isknbudovaid, dokonceni, druhkonstrukcekod, obestavenyprostor, pocetbytu, pocetpodlazi, podlahovaplocha, pripojenikanalizacekod, pripojeniplynkod, pripojenivodovodkod, vybavenivytahemkod, zastavenaplocha, zpusobvytapenikod, zpusobyochrany, detailnitea, geometriedefbod, geometrieorihranice, nespravneudaje |
    | adresniMisto | nespravny, cislodomovni, cisloorientacni, cisloorientacnipismeno, psc, stavebniobjekt, ulice, vokod, platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, nespravneudaje |
    | zsj | nazev, nespravny, katastralniuzemi, platiod, platido, idtransakce, globalniidnavrhuzmeny, mluvnickecharakteristiky, vymera, charakterzsjkod, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, datumvzniku |
    | vo | platiod, platido, idtransakce, globalniidnavrhuzmeny, geometriedefbod, geometriegenhranice, geometrieorihranice, nespravneudaje, cislo, nespravny, obec, momc, poznamka |
    | zaniklyPrvek | typprvkukod, idtransakce |

Poznámka: 
1) Quartz Scheduler obsahuje 2 nastavení s hodnotou boolean pro případné přeskočení jednotlivých kroků inicializace. Cron atribut neboli cron expresion slouží k nastavení frekvence stahování dat.
2) Všechny tabulky kromě vo a zaniklyPrvek byly testovány a je tam minimální šance na chybu.

# Průběh aplikace
1) Aplikace nastaví podle config.json připojení k databázi a načte nastavení pro stahování dat.
2) Pokud je nastaveno skipInitialRunStat na false, aplikace stáhne data Stát až ZSJ. Data přečte do DTO a uloží do databáze.
3) Pokud je nastaveno skipInitialRunRegion na false, aplikace stáhne data pro vybrané kraje. Data přečte do DTO a uloží do databáze.
4) Dále se stahují přírůstková data. Data přečte do DTO a uloží do databáze. 
5) Aplikace čeká na další spuštění podle nastavení v Quartz Scheduleru.

Aplikace nepřemazává data v databázi, ale přidává nové záznamy. Pokud je záznam již v databázi, aplikace ho aktualizuje (doplní pouze změny).
Při čtení je ošetřeno několik možných chyb, které mohou nastat při stahování dat z VDP. Například pokud pokud se u objektu vyskytne Foreign Key, který není v databázi, aplikace přeskočí tento objekt a pokračuje dál.