package hei.devweb.projetit.dao.impl;

import hei.devweb.projetit.dao.UtilisateurDao;
import hei.devweb.projetit.entities.Club;
import hei.devweb.projetit.entities.Utilisateur;
import hei.devweb.projetit.exception.PasswordDontMatchException;
import hei.devweb.projetit.exception.PseudoAlreadyExistException;
import hei.devweb.projetit.exception.UtilisateurNotFoundException;
import hei.devweb.projetit.service.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UtilisateurDaoImpl implements UtilisateurDao {


    static final Logger LOGGER = LogManager.getLogger();


    @Override
    public Utilisateur getUtilisateur(Integer id) {
        LOGGER.debug("method getUtilisateur called");
        String sqlQuery = "SELECT * FROM utilisateur JOIN club ON utilisateur.club_id = club.club_id  WHERE utilisateur_id=?";
        try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setInt(1, id);
                try (ResultSet resultSet = statement.executeQuery()) {
                    if (resultSet.next()) {
                        Club club = new Club(
                                resultSet.getInt("club_id"),
                                resultSet.getString("name"),
                                resultSet.getString("lien")
                        );
                        Utilisateur utilisateur = new Utilisateur(
                                resultSet.getInt("utilisateur_id"),
                                resultSet.getString("pseudo"),
                                resultSet.getString("motdepasse"),
                                resultSet.getString("mail"),
                                resultSet.getBoolean("president"),
                                club.getId()
                        );
                        LOGGER.info("Getting user id=" + utilisateur.getIdutilisateur() + " | pseudo=" + utilisateur.getPseudo() + " | club=" + utilisateur.getClub());
                        return utilisateur;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Utilisateur addUtilisateur(Utilisateur user) {
        LOGGER.debug("method addUtilisateur called");
        String sqlQuery = "INSERT INTO utilisateur(pseudo, motdepasse, mail, president, club_id) " +
                "VALUES(?, ?, ?, ?, ?)";
        try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, user.getPseudo());
                statement.setString(2, user.getMotdepasse());
                statement.setString(3, user.getMail());
                statement.setBoolean(4, user.getPresident());
                statement.setInt(5, user.getClub());

                statement.executeUpdate();

                try (ResultSet ids = statement.getGeneratedKeys()) {
                    if (ids.next()) {
                        int userId = ids.getInt("utilisateur_id");
                        user.setIdutilisateur(userId);
                        LOGGER.info("Adding in data base user id=" + user.getIdutilisateur() + " | pseudo=" + user.getPseudo() + " | club=" + user.getClub());
                        return user;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Request Execution Problem");
    }

    @Override
    public List<Utilisateur> listUtilisateur() {
        LOGGER.debug("method listUtilisateur called");
        List<Utilisateur> users = new ArrayList<>();
        String sqlQuery = "SELECT * FROM utilisateur ORDER BY pseudo";
        try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
            try (Statement statement = connection.createStatement()) {
                try (ResultSet resultSet = statement.executeQuery(sqlQuery)) {
                    while (resultSet.next()) {
                        Utilisateur user = new Utilisateur(
                                resultSet.getInt("utilisateur_id"),
                                resultSet.getString("pseudo"),
                                resultSet.getString("motdepasse"),
                                resultSet.getString("mail"),
                                resultSet.getBoolean("president"),
                                resultSet.getInt("club_id")
                        );
                        LOGGER.debug("Adding to users list user id=" + user.getIdutilisateur());
                        users.add(user);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        LOGGER.debug("listUtilisateur completed");
        return users;
    }

    @Override
    public void setPassword(String pseudo, String newPassword) {
        LOGGER.debug("method setPassword called");
        String sqlQuery = "UPDATE utilisateur SET motdepasse = ? WHERE pseudo = ?";
        try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery)) {
                statement.setString(1, newPassword);
                statement.setString(2, pseudo);
                LOGGER.info("Modifying password of user pseudo=" + pseudo);

                statement.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void deleteUtilisateur(Integer id) {
            LOGGER.debug("method deleteUtilisateur called");
        String sqlQuery = "DELETE FROM utilisateur WHERE utilisateur_id=?";
        try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                statement.setInt(1, id);
                System.out.println(sqlQuery);
                LOGGER.info("Deleting user from database id=" + id + " | pseudo=" + getUtilisateur(id).getPseudo());
                statement.executeUpdate();
            } catch (UtilisateurNotFoundException UtilNotFound){
                UtilNotFound.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void isPres(Integer id) {
    LOGGER.debug("method isPres called");
    String sqlQuery = "UPDATE utilisateur SET president = ? WHERE utilisateur_id=? ";

    Boolean pres = UserService.getInstance().getUser(id).getPresident();
    System.out.println("is Pres ? : " + pres);
    try (Connection connection = DataSourceProvider.getDataSource().getConnection()) {
        try (PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)) {
                statement.setBoolean(1, !pres);
                statement.setInt(2, id);
                LOGGER.info("Modifying access for user id=" + id + " | pseudo=" + getUtilisateur(Integer.valueOf(id)).getPseudo() + ", user is now president=" + !pres);

                statement.executeUpdate();
            } catch (UtilisateurNotFoundException UserException){
                UserException.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pseudoAlreadyExist(String pseudo) {
        LOGGER.debug("Test de l'unicité du pseudo");
        for(Utilisateur user : listUtilisateur()){
            if(pseudo.equals(user.getPseudo())){
                throw new PseudoAlreadyExistException();
            }
        }
    }

    @Override
    public void passwordMatch(String password1, String password2) {
        LOGGER.debug("Test password matching");
        if(password1 != password2){
            throw new PasswordDontMatchException();
        }
    }

}
