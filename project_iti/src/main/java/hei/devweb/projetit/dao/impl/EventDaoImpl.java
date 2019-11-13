package hei.devweb.projetit.dao.impl;

import hei.devweb.projetit.dao.EventDao;
import hei.devweb.projetit.entities.Club;
import hei.devweb.projetit.entities.Event;

import org.mariadb.jdbc.internal.com.read.resultset.UpdatableResultSet;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EventDaoImpl implements EventDao {

    @Override
    public List<Event> listEvents() {
        List<Event> events = new ArrayList<>();
        String sqlQuery = "SELECT * FROM event   JOIN club ON event.club_id = club.club_id  ORDER BY event_date";
        try(Connection connection = DataSourceProvider.getDataSource().getConnection()){
            try (Statement statement = connection.createStatement()){
                try(ResultSet resultSet = statement.executeQuery(sqlQuery)){
                    while(resultSet.next()){
                        Club club = new Club(
                                resultSet.getInt("club_id"),
                                resultSet.getString("name")
                        );
                        Event event = new Event(
                                resultSet.getInt("event_id"),
                                resultSet.getString("title"),
                                club,
                                resultSet.getDate("event_date").toLocalDate(),
                                resultSet.getString("resume"),
                                resultSet.getString("details")
                        );
                        events.add(event);
                    }

                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return events;
    }


    @Override
    public Event getEvent(Integer id) {
        String sqlQuery = "SELECT * FROM event JOIN club ON event.club_id = club.club_id  WHERE event_id=?";
        try (Connection connection = DataSourceProvider.getDataSource().getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sqlQuery)){
                statement.setInt(1, id);
                try(ResultSet resultSet = statement.executeQuery()){
                    if(resultSet.next()){
                        Club club = new Club(
                                resultSet.getInt("club_id"),
                                resultSet.getString("name")
                        );
                        Event event = new Event(
                                resultSet.getInt("event_id"),
                                resultSet.getString("title"),
                                club,
                                resultSet.getDate("event_date").toLocalDate(),
                                resultSet.getString("resume"),
                                resultSet.getString("details")
                        );
                        return event;
                    }
                }
            }
        }catch(SQLException e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Event addEvent(Event event){
        String sqlQuery = "INSERT INTO event(title, club_id, event_date, resume, details) " +
                "VALUES(?, ?, ?, ?, ?)";
        try(Connection connection = DataSourceProvider.getDataSource().getConnection()){
            try(PreparedStatement statement = connection.prepareStatement(sqlQuery, Statement.RETURN_GENERATED_KEYS)){
                statement.setString(1, event.getTitle());
                statement.setInt(2, event.getClub().getId());
                statement.setDate(3, Date.valueOf(event.getEventDate()));
                statement.setString(4, event.getResume());
                statement.setString(5, event.getDetails());

                statement.executeUpdate();

                try (ResultSet ids = statement.getGeneratedKeys()){
                    if(ids.next()){
                        int eventId = ids.getInt("event_id");
                        event.setId(eventId);
                        return event;
                    }
                }
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        throw new RuntimeException("Request Execution Problem");
    }
    //bonjour
}
