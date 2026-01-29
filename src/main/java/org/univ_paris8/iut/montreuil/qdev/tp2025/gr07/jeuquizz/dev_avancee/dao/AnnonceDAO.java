package org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.dao;

import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.db.ConnectionDB;
import org.univ_paris8.iut.montreuil.qdev.tp2025.gr07.jeuquizz.dev_avancee.model.Annonce;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnonceDAO extends dao<Annonce> {

    @Override
    public Annonce find(int id) throws Exception {
        Connection c = ConnectionDB.getInstance();

        PreparedStatement ps = c.prepareStatement(
                "SELECT id, title, description, adress, mail, date FROM annonce WHERE id = ?"
        );
        ps.setInt(1, id);

        ResultSet rs = ps.executeQuery();
        if (rs.next()) {
            return new Annonce(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("adress"),
                    rs.getString("mail"),
                    rs.getTimestamp("date")
            );
        }
        return null;
    }

    @Override
    public List<Annonce> findAll() throws Exception {
        Connection c = ConnectionDB.getInstance();

        Statement st = c.createStatement();
        ResultSet rs = st.executeQuery(
                "SELECT id, title, description, adress, mail, date FROM annonce ORDER BY date DESC"
        );

        List<Annonce> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Annonce(
                    rs.getInt("id"),
                    rs.getString("title"),
                    rs.getString("description"),
                    rs.getString("adress"),
                    rs.getString("mail"),
                    rs.getTimestamp("date")
            ));
        }
        return list;
    }

    @Override
    public boolean create(Annonce a) throws Exception {
        Connection c = ConnectionDB.getInstance();

        PreparedStatement ps = c.prepareStatement(
                "INSERT INTO annonce(title, description, adress, mail, date) VALUES (?,?,?,?,now())"
        );
        ps.setString(1, a.getTitle());
        ps.setString(2, a.getDescription());
        ps.setString(3, a.getAdress());
        ps.setString(4, a.getMail());

        return ps.executeUpdate() == 1;
    }

    @Override
    public boolean update(Annonce a) throws Exception {
        Connection c = ConnectionDB.getInstance();

        PreparedStatement ps = c.prepareStatement(
                "UPDATE annonce SET title=?, description=?, adress=?, mail=? WHERE id=?"
        );
        ps.setString(1, a.getTitle());
        ps.setString(2, a.getDescription());
        ps.setString(3, a.getAdress());
        ps.setString(4, a.getMail());
        ps.setInt(5, a.getId());

        return ps.executeUpdate() == 1;
    }

    @Override
    public boolean delete(int id) throws Exception {
        Connection c = ConnectionDB.getInstance();

        PreparedStatement ps = c.prepareStatement(
                "DELETE FROM annonce WHERE id=?"
        );
        ps.setInt(1, id);

        return ps.executeUpdate() == 1;
    }
}
