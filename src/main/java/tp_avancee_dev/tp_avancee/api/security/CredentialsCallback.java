package tp_avancee_dev.tp_avancee.api.security;

import javax.security.auth.callback.Callback;

public class CredentialsCallback implements Callback {

    private String login;
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
