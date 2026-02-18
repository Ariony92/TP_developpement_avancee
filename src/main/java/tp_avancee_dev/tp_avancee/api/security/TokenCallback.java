package tp_avancee_dev.tp_avancee.api.security;

import javax.security.auth.callback.Callback;

public class TokenCallback implements Callback {

    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
