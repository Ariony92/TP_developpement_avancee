package tp_avancee_dev.tp_avancee.api.security;

import tp_avancee_dev.tp_avancee.service.ApiTokenService;

import javax.security.auth.callback.Callback;

public class ApiTokenServiceCallback implements Callback {

    private ApiTokenService apiTokenService;

    public ApiTokenService getApiTokenService() {
        return apiTokenService;
    }

    public void setApiTokenService(ApiTokenService apiTokenService) {
        this.apiTokenService = apiTokenService;
    }
}
