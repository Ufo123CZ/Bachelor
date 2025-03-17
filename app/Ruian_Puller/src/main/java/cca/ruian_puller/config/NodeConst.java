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

    // Source Nodes
    public static final String SOURCE_SEARCH_NODE = "sourceSearch";
    public static final String SOURCE_LIST_NODE = "sourceList";

}
