package cca.ruian_puller.config;

public class NodeConst {
    // Supported Database Types
    public static final String POSTGRESQL = "postgresql";
    public static final String MSSQL = "mssql";
    public static final String CERTIFICATE = ";trustServerCertificate=true";
    public static final String DB_NAME_MSSQL = ";databaseName=";
    public static final String ORACLE = "oracle";

    // Database Nodes
    public static final String DATABASE_NODE = "database";
    public static final String DATABASE_TYPE_NODE = "type";
    public static final String DATABASE_URL_NODE = "url";
    public static final String DATABASE_NAME_NODE = "dbname";
    public static final String DATABASE_USERNAME_NODE = "username";
    public static final String DATABASE_PASSWORD_NODE = "password";

    // Additional Options Nodes
    public static final String ADDITIONAL_OPTIONS_NODE = "additionalOptions";
    public static final String INCLUDE_GEOMETRY_NODE = "includeGeometry";
    public static final String COMMIT_SIZE_NODE = "commitSize";

    // Required Tables Nodes
    public static final String DATA_TO_PROCESS = "dataToProcess";
    public static final String TABLES_NODE = "tables";
    public static final String HOW_TO_PROCESS_NODE = "howToProcess";
    public static final String HOW_OF_PROCESS_TABLES_VAL_1 = "all";
    public static final String HOW_OF_PROCESS_TABLES_VAL_2 = "selected";
    public static final String HOW_OF_PROCESS_ELEMENT_VAL_1 = "all";
    public static final String HOW_OF_PROCESS_ELEMENT_VAL_2 = "include";
    public static final String HOW_OF_PROCESS_ELEMENT_VAL_3 = "exclude";
    public static final String COLUMN_NODE = "columns";

    public static final String TABLE_STAT_NODE = "stat";
    public static final String TABLE_REGION_SOUDRZNOSTI_NODE = "regionSoudrznosti";
    public static final String TABLE_VUSC_NODE = "vusc";
    public static final String TABLE_OKRES_NODE = "okres";
    public static final String TABLE_ORP_NODE = "orp";
    public static final String TABLE_POU_NODE = "pou";
    public static final String TABLE_OBEC_NODE = "obec";
    public static final String TABLE_CAST_OBCE_NODE = "castObce";
    public static final String TABLE_MOP_NODE = "mop";
    public static final String TABLE_SPRAVNI_OBVOD_NODE = "spravniObvod";
    public static final String TABLE_MOMC_NODE = "momc";
    public static final String TABLE_KATASTRALNI_UZEMI_NODE = "katastralniUzemi";
    public static final String TABLE_PARCELA_NODE = "parcela";
    public static final String TABLE_ULICE_NODE = "ulice";
    public static final String TABLE_STAVEBNI_OBJEKT_NODE = "stavebniObjekt";
    public static final String TABLE_ADRESNI_MISTO_NODE = "adresniMisto";
    public static final String TABLE_ZSJ_NODE = "zsj";
    public static final String TABLE_ZANIKLY_PRVEK_NODE = "zaniklyPrvek";

}
