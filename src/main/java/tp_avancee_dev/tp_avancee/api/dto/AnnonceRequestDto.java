package tp_avancee_dev.tp_avancee.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AnnonceRequestDto {
    @NotBlank(message = "title is required")
    @Size(max = 64, message = "title must be <= 64 characters")
    private String title;

    @NotBlank(message = "description is required")
    @Size(max = 256, message = "description must be <= 256 characters")
    private String description;

    @NotBlank(message = "adress is required")
    @Size(max = 64, message = "adress must be <= 64 characters")
    private String adress;

    @NotBlank(message = "mail is required")
    @Email(message = "mail must be a valid email")
    @Size(max = 64, message = "mail must be <= 64 characters")
    private String mail;

    private Long authorId;

    @NotNull(message = "categoryId is required")
    private Long categoryId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdress() {
        return adress;
    }

    public void setAdress(String adress) {
        this.adress = adress;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
