package tp_avancee_dev.tp_avancee.api.security;

import java.security.Principal;
import java.util.Objects;

public class RolePrincipal implements Principal {

    private final String role;

    public RolePrincipal(String role) {
        this.role = role;
    }

    @Override
    public String getName() {
        return role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RolePrincipal)) {
            return false;
        }
        RolePrincipal that = (RolePrincipal) o;
        return Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role);
    }
}
