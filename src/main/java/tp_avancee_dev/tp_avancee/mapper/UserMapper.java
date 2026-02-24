package tp_avancee_dev.tp_avancee.mapper;

import org.mapstruct.Mapper;
import tp_avancee_dev.tp_avancee.model.User;

@Mapper(componentModel = "spring")
public interface UserMapper {

    default Long toId(User user) {
        return user == null ? null : user.getId();
    }

    default String toUsername(User user) {
        return user == null ? null : user.getUsername();
    }
}
