package forums_db.services;

import forums_db.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by SvetaZlobina on 20.03.2017.
 */
@Service
public class UserService {
    private JdbcTemplate jdbcTemplate;

    public UserService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createUser(UserModel user) {
        final String query = "INSERT INTO \"user\"(about, email, fullname, nickname) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(query, user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname());
    }

    public List<UserModel> getUser(UserModel user) {
        final String query =
                "SELECT * FROM \"user\" " +
                        "WHERE LOWER(nickname) = LOWER(?) OR LOWER(email) = LOWER(?)";
        return jdbcTemplate.query(query, new Object[]{user.getNickname(), user.getEmail()}, UserService::rowMapper);
    }

    public void updateUser(UserModel user) {
        final String query =
                "UPDATE \"user\" " +
                        "SET about = ?, email = ?, fullname = ? WHERE nickname = ?";
        jdbcTemplate.update(query, new Object[]{user.getAbout(), user.getEmail(), user.getFullname(), user.getNickname()});
    }

    public static UserModel rowMapper(ResultSet set, int rowNumber) throws SQLException {
        return new UserModel(
                set.getString("about"),
                set.getString("email"),
                set.getString("fullname"),
                set.getString("nickname"));
    }
}
