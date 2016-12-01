package job.fair.jobfair.jpa.entity;

public class Status {

    private String url;
    private String message;

    public Status() {
    }

    public Status(String errorUrl, String message) {
        this.url = errorUrl;
        this.message = message;
    }


    public String getUrl() {
        return url;
    }

    public void setUrl(String errorUrl) {
        this.url = errorUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
