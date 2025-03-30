CREATE DATABASE ruian;
GO

USE ruian;
GO

-- RUIAN tables

CREATE TABLE Stat (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(32),
    Nespravny BIT,
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau NVARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE RegionSoudrznosti (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(32),
    Nespravny BIT,
    Stat INT FOREIGN KEY REFERENCES Stat(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau NVARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Vusc (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(32),
    Nespravny BIT,
    RegionSoudrznosti INT FOREIGN KEY REFERENCES RegionSoudrznosti(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau NVARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Okres (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(32),
    Nespravny BIT,
    Kraj INT,
    Vusc INT FOREIGN KEY REFERENCES Vusc(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    NutsLau NVARCHAR(6),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Orp (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    SpravniObecKod INT,
    Vusc INT FOREIGN KEY REFERENCES Vusc(Kod),
    Okres INT FOREIGN KEY REFERENCES Okres(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Pou (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    SpravniObecKod INT,
    Orp INT FOREIGN KEY REFERENCES Orp(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Obec (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    StatusKod INT,
    Okres INT FOREIGN KEY REFERENCES Okres(Kod),
    Pou INT FOREIGN KEY REFERENCES Pou(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky NVARCHAR(MAX),
    VlajkaText NVARCHAR(4000),
    VlajkaObrazek VARBINARY(MAX),
    ZnakText NVARCHAR(4000),
    ZnakObrazek VARBINARY(MAX),
    CleneniSMRozsahKod INT,
    CleneniSMTypKod INT,
    NutsLau NVARCHAR(12),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE CastObce (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky NVARCHAR(MAX),
    GeometrieDefBod GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Mop (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(32),
    Nespravny BIT,
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE SpravniObvod (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(32),
    Nespravny BIT,
    SpravniMomcKod INT,
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Momc (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    Mop INT FOREIGN KEY REFERENCES Mop(Kod),
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    SpravniObvod INT FOREIGN KEY REFERENCES SpravniObvod(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    VlajkaText NVARCHAR(4000),
    VlajkaObrazek VARBINARY(MAX),
    ZnakText NVARCHAR(4000),
    MluvnickeCharakteristiky NVARCHAR(MAX),
    ZnakObrazek VARBINARY(MAX),
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE KatastralniUzemi (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    ExistujeDigitalniMapa BIT,
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    RizeniID BIGINT,
    MluvnickeCharakteristiky NVARCHAR(MAX),
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE Parcela (
    Id BIGINT PRIMARY KEY,
    Nespravny BIT,
    KmenoveCislo INT,
    PododdeleniCisla INT,
    VymeraParcely BIGINT,
    ZpusobyVyuzitiPozemku INT,
    DruhCislovaniKod INT,
    DruhPozemkuKod INT,
    KatastralniUzemi INT FOREIGN KEY REFERENCES KatastralniUzemi(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    RizeniID BIGINT,
    BonitovaneDily NVARCHAR(MAX),
    ZpusobyOchranyPozemku NVARCHAR(MAX),
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX)
);

CREATE TABLE Ulice (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieDefCara GEOMETRY,
    NespravneUdaje NVARCHAR(MAX)
);

CREATE TABLE StavebniObjekt (
    Kod INT PRIMARY KEY,
    Nespravny BIT,
    CisloDomovni NVARCHAR(MAX),
    IdentifikacniParcela BIGINT FOREIGN KEY REFERENCES Parcela(Id),
    TypStavebnihoObjektuKod INT,
    CastObce INT FOREIGN KEY REFERENCES CastObce(Kod),
    Momc INT FOREIGN KEY REFERENCES Momc(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    IsknBudovaId BIGINT,
    Dokonceni DATETIME,
    DruhKonstrukceKod INT,
    ObestavenyProstor INT,
    PocetBytu INT,
    PocetPodlazi INT,
    PodlahovaPlocha INT,
    PripojeniKanalizaceKod INT,
    PripojeniPlynKod INT,
    PripojeniVodovodKod INT,
    VybaveniVytahemKod INT,
    ZastavenaPlocha INT,
    ZpusobVytapeniKod INT,
    ZpusobyOchrany NVARCHAR(MAX),
    DetailniTEA NVARCHAR(MAX),
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX)
);

CREATE TABLE AdresniMisto (
    Kod INT PRIMARY KEY,
    Nespravny BIT,
    CisloDomovni INT,
    CisloOrientacni INT,
    CisloOrientacniPismeno NVARCHAR(1),
    Psc INT,
    StavebniObjekt INT FOREIGN KEY REFERENCES StavebniObjekt(Kod),
    Ulice INT FOREIGN KEY REFERENCES Ulice(Kod),
    VOKod INT,
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    NespravneUdaje NVARCHAR(MAX)
);

CREATE TABLE Zsj (
    Kod INT PRIMARY KEY,
    Nazev NVARCHAR(48),
    Nespravny BIT,
    KatastralniUzemi INT FOREIGN KEY REFERENCES KatastralniUzemi(Kod),
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    MluvnickeCharakteristiky NVARCHAR(MAX),
    Vymera BIGINT,
    CharakterZsjKod INT,
    GeometrieDefBod GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    DatumVzniku DATETIME
);

CREATE TABLE VO (
    PlatiOd DATETIME,
    PlatiDo DATETIME,
    IdTransakce BIGINT,
    GlobalniIdNavrhuZmeny BIGINT,
    GeometrieDefBod GEOMETRY,
    GeometrieGenHranice GEOMETRY,
    GeometrieOriHranice GEOMETRY,
    NespravneUdaje NVARCHAR(MAX),
    Kod INT PRIMARY KEY,
    Cislo INT,
    Nespravny BIT,
    Obec INT FOREIGN KEY REFERENCES Obec(Kod),
    MOMC INT FOREIGN KEY REFERENCES Momc(Kod),
    Poznamka NVARCHAR(60)
);

CREATE TABLE ZaniklyPrvek (
    TypPrvkuKod NVARCHAR(3),
    PrvekId BIGINT PRIMARY KEY,
    IdTransakce BIGINT
);