package forums_db.services;

import forums_db.models.PostModel;
import forums_db.models.ThreadModel;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@Service
public class ThreadService {
    private JdbcTemplate jdbcTemplate;

    public ThreadService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static Logger log = Logger.getLogger(ThreadService.class.getName());

    public List<PostModel> createPosts(List<PostModel> posts,
                                       String slug) {
        final String created = OffsetDateTime.now().toString();
        final Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(created, DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        final StringBuilder queryInsert = new StringBuilder("INSERT INTO post(user_id, created, forum_id, is_edited, " +
                "\"message\", parent_id, thread_id) ");
        final StringBuilder queryGetParents = new StringBuilder("SELECT p.id FROM post p WHERE p.thread_id = ");
        final StringBuilder queryUpdateForum = new StringBuilder("UPDATE forum f SET posts = posts + ? " +
                "WHERE f.id = (SELECT t.forum_id FROM thread t WHERE ");
        final  StringBuilder queryGetPosts = new StringBuilder("SELECT  p.id, u.nickname, p.created, f.slug, " +
                "p.is_edited, p.message, p.parent_id, p.thread_id " +
                "FROM post p JOIN \"user\" u ON (p.user_id = u.id) " +
                "JOIN forum f ON (p.forum_id = f.id) " +
                "JOIN thread t ON (p.thread_id = t.id) " +
                "WHERE p.thread_id = ");

        Boolean isNumber = Boolean.TRUE;
        Integer id = null;

        try {
            id = Integer.valueOf(slug);
            queryGetParents.append(" ?"); //{id}
            queryInsert.append("VALUES(" +
                    "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), " +
                    "?, " +
                    "(SELECT forum_id FROM thread WHERE id = ?), " +
                    "?, ?, ?, ?)");
            queryUpdateForum.append("t.id = ?)");
            queryGetPosts.append(" ?");
        } catch (NumberFormatException e) { //{slug}
            isNumber = Boolean.FALSE;
            queryGetParents.append("(SELECT t.id FROM thread t WHERE LOWER(t.slug) = LOWER(?))");
            queryInsert.append("VALUES(" +
                    "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), " +
                    "?, " +
                    "(SELECT t.forum_id FROM forum f JOIN thread t ON (f.id = t.forum_id) WHERE LOWER(t.slug) = LOWER(?)), " +
                    "?, ?, ?, " +
                    "(SELECT id FROM thread WHERE LOWER(slug) = LOWER(?)))");
            queryUpdateForum.append("t.slug = ?)");
            queryGetPosts.append("(SELECT th.id FROM thread th WHERE LOWER(th.slug) = LOWER(?))");
        }

        for (PostModel post: posts) {
            if(post.getParent() != 0) {
                final List<Integer> postsDB = jdbcTemplate.queryForList(queryGetParents.toString(), Integer.class, isNumber ? id : slug);
                if(!postsDB.contains(post.getParent())) {
                    throw new DuplicateKeyException(null);
                }
            }

            jdbcTemplate.update(queryInsert.toString(), post.getAuthor(), timestamp, isNumber ? id : slug,
                    post.getIsEdited(), post.getMessage(), post.getParent(), isNumber ? id : slug);
        }

        jdbcTemplate.update(queryUpdateForum.toString(), posts.size(), isNumber ? id : slug);

        final List<PostModel> postsDB = jdbcTemplate.query(queryGetPosts.toString(),
                isNumber ? new Object[]{id} : new Object[]{slug}, PostService::rowMapper);
        return postsDB;

    }

    public static ThreadModel rowMapper(ResultSet set, int rowNumber) throws SQLException {
        final Timestamp timestamp = set.getTimestamp("created");
        final SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

        return new ThreadModel(
                set.getString("nickname"),
                date.format(timestamp),
                set.getString("fSlug"),
                set.getInt("id"),
                set.getString("message"),
                set.getString("slug"),
                set.getString("title")
        );
    }
}
