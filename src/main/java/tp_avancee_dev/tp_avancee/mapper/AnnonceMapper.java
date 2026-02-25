package tp_avancee_dev.tp_avancee.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import tp_avancee_dev.tp_avancee.dto.AnnonceRequestDto;
import tp_avancee_dev.tp_avancee.dto.AnnonceResponseDto;
import tp_avancee_dev.tp_avancee.model.Annonce;

@Mapper(componentModel = "spring", uses = {UserMapper.class, CategoryMapper.class})
public interface AnnonceMapper {

    @Mapping(source = "author.id", target = "authorId")
    @Mapping(source = "author.username", target = "authorUsername")
    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.label", target = "categoryLabel")
    AnnonceResponseDto toDto(Annonce annonce);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    Annonce toEntity(AnnonceRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "author", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateEntity(AnnonceRequestDto dto, @MappingTarget Annonce annonce);
}
