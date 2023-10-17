import java.util.Map;

public class HttpRequest {
    private String url;
    private String method;
    private Map<String, String> header;

    HttpRequest(String method, String url, Map<String, String> header) {
        this.method = method;
        this.header = header;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }
}
