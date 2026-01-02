package utils;

public class _global {

    public static final String PREFIX_FILE_CONFIG = "SMLConfig";

    public static final boolean USE_LOCAL = false; // true = use config, false = use provider file config

    public static String _providerCode = "";
    public static String _databaseServer = "cloud2.smlsoft.com"; // "192.168.64.47"; //   "smldata3.smldatacenter.com"; "127.0.0.1";   cloud2.smlsoft.com;
    public static String _databaseName = "";
    public static String _databaseUserCode = "postgres";
    public static String _databaseUserPassword = "sml"; // "sml"; //  "sml"; // "smlP@ssw0rd19682511"; 
    public static String _databasePort = "5432"; // "sml"; //  "sml"; // "smlP@ssw0rd19682511"; 
    
    public static final int _WEB_FLAG = 1;

    public static String FILE_CONFIG(String providerCode) {
        if (USE_LOCAL) {
            return "";
        }
        return PREFIX_FILE_CONFIG + providerCode.toUpperCase() + ".xml";
    }
}
