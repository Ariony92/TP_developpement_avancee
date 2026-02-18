package tp_avancee_dev.tp_avancee.api.mapper;

import tp_avancee_dev.tp_avancee.api.dto.AnnonceResponseDto;
import tp_avancee_dev.tp_avancee.model.Annonce;

public final class AnnonceMapper {

    private AnnonceMapper() {
    }

    public static AnnonceResponseDto toBasicDto(Annonce annonce) {
        AnnonceResponseDto dto = new AnnonceResponseDto();
        dto.setId(annonce.getId());
        dto.setVersion(annonce.getVersion());
        dto.setTitle(annonce.getTitle());
        dto.setDescription(annonce.getDescription());
        dto.setAdress(annonce.getAdress());
        dto.setMail(annonce.getMail());
        dto.setDate(annonce.getDate());
        dto.setStatus(annonce.getStatus());
        return dto;
    }

    public static AnnonceResponseDto toDetailedDto(Annonce annonce) {
        AnnonceResponseDto dto = toBasicDto(annonce);

        if (annonce.getAuthor() != null) {
            dto.setAuthorId(annonce.getAuthor().getId());
            dto.setAuthorUsername(annonce.getAuthor().getUsername());
        }
        if (annonce.getCategory() != null) {
            dto.setCategoryId(annonce.getCategory().getId());
            dto.setCategoryLabel(annonce.getCategory().getLabel());
        }

        return dto;
    }
}
