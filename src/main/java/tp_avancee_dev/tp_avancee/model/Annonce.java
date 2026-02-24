package tp_avancee_dev.tp_avancee.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "annonce")
public class Annonce {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(nullable = false)
    private Long version;

    @NotBlank
    @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String title;

    @NotBlank
    @Size(max = 256)
    @Column(nullable = false, length = 256)
    private String description;

    @NotBlank
    @Size(max = 64)
    @Column(name = "address", nullable = false, length = 64)
    private String adress;

    @NotBlank
    @Email
    @Size(max = 64)
    @Column(nullable = false, length = 64)
    private String mail;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private Status status = Status.DRAFT;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public Annonce() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Instant getDate() { return date; }
    public void setDate(Instant date) { this.date = date; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }
}
