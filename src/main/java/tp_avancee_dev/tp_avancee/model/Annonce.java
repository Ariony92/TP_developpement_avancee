package tp_avancee_dev.tp_avancee.model;

import java.sql.Timestamp;

public class Annonce {

    private int id;
    private String title;
    private String description;
    private String adress;
    private String mail;
    private Timestamp date;

    public Annonce() {}

    public Annonce(String title, String description, String adress, String mail, Timestamp date) {
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
        this.date = date;
    }


    public Annonce(int id, String title, String description, String adress, String mail, Timestamp date) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.adress = adress;
        this.mail = mail;
        this.date = date;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAdress() { return adress; }
    public void setAdress(String adress) { this.adress = adress; }

    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }

    public Timestamp getDate() { return date; }
    public void setDate(Timestamp date) { this.date = date; }
}
