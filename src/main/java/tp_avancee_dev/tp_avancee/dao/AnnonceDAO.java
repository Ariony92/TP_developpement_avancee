package tp_avancee_dev.tp_avancee.dao;

import tp_avancee_dev.tp_avancee.model.Annonce;
import tp_avancee_dev.tp_avancee.db.ConnectionDB;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AnnonceDAO extends DAO<Annonce> {

    @Override
    public Annonce find(int id) throws Exception {
        Connection c = ConnectionDB.getInstance();

        String sql = "SELECT id, title, description, adress, mail, date FROM annonce WHERE id = ?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);

            try (ResultSet rs = ps.executeQuery()) {
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
            }
        }

        return null;
    }

    @Override
    public List<Annonce> findAll() throws Exception {
        Connection c = ConnectionDB.getInstance();

        String sql = "SELECT id, title, description, adress, mail, date FROM annonce ORDER BY date DESC";

        List<Annonce> list = new ArrayList<>();

        try (PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

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
        }

        return list;
    }

    @Override
    public boolean create(Annonce a) throws Exception {
        Connection c = ConnectionDB.getInstance();

        String sql = "INSERT INTO annonce(title, description, adress, mail, date) VALUES (?,?,?,?,now())";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getAdress());
            ps.setString(4, a.getMail());

            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean update(Annonce a) throws Exception {
        Connection c = ConnectionDB.getInstance();

        String sql = "UPDATE annonce SET title=?, description=?, adress=?, mail=? WHERE id=?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, a.getTitle());
            ps.setString(2, a.getDescription());
            ps.setString(3, a.getAdress());
            ps.setString(4, a.getMail());
            ps.setInt(5, a.getId());

            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public boolean delete(int id) throws Exception {
        Connection c = ConnectionDB.getInstance();

        String sql = "DELETE FROM annonce WHERE id=?";

        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() == 1;
        }
    }
}
