package tp_avancee_dev.tp_avancee.api.dto;

import jakarta.validation.constraints.NotNull;
import tp_avancee_dev.tp_avancee.model.Status;

public class StatusPatchDto {
    @NotNull(message = "status is required")
    private Status status;

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
