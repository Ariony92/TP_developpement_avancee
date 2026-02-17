package tp_avancee_dev.tp_avancee.api.exceptions;

import java.util.List;

public class ApiErrorResponse {
    private String error;
    private List<String> messages;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String error, List<String> messages) {
        this.error = error;
        this.messages = messages;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(List<String> messages) {
        this.messages = messages;
    }
}
