package forums_db.services;

import forums_db.models.PostModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@Service
public class PostService {
    private JdbcTemplate jdbcTemplate;

    public PostService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static PostModel rowMapper(ResultSet set, int rowNumber) throws SQLException {
        final Timestamp timestamp = set.getTimestamp("created");
        final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        return new PostModel(
                set.getInt("id"),
                set.getString("nickname"),
                date.format(timestamp),
                set.getString("slug"),
                set.getBoolean("is_edited"),
                set.getString("message"),
                set.getInt("parent_id"),
                set.getInt("thread_id")
        );
    }
}
