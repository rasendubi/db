package badcoders.logic.util;

/**
 * Constants used by the system.
 */
public class Constants {

    public static final String API_BASE = "/bad";

    public static final String DATABASE_NAME = "badbase";

    public static final String LOGIN_HEADER = "User-Login";
    public static final String PASSWORD_HEADER = "User-Password";

    public static final String WWW_PATH = System.getProperty("user.dir") + "/src/main/resources/badcoders/www/";
    public static final String INDEX_HTML_PATH = WWW_PATH + "index.html";
    public static final String JS_PARENT_PATH = WWW_PATH + "js/";
    public static final String CSS_PARENT_PATH = WWW_PATH + "css/";
    public static final String TEMPLATE_PATH = WWW_PATH + "template/";
}
