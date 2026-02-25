package tp_avancee_dev.tp_avancee.mapper;

import org.mapstruct.Mapper;
import tp_avancee_dev.tp_avancee.model.Category;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    default Long toId(Category category) {
        return category == null ? null : category.getId();
    }

    default String toLabel(Category category) {
        return category == null ? null : category.getLabel();
    }
}
