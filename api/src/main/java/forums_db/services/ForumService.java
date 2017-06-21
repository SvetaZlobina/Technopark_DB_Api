package forums_db.services;

import forums_db.models.ForumModel;
import forums_db.models.ThreadModel;
import forums_db.models.UserModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@Service
public class ForumService {
    private JdbcTemplate jdbcTemplate;
    private static Integer currentThreadId = 0;

    private static Logger log = Logger.getLogger(ForumService.class.getName());

    public ForumService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void createForum(ForumModel forum) {
        final String query = "INSERT INTO forum(slug, title, user_id) VALUES (?, ?," +
                "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)))";
        jdbcTemplate.update(query, forum.getSlug(), forum.getTitle(), forum.getUser());
    }

    public List<ForumModel> getForum(String slug) {
        final String query =
                "SELECT posts, slug, threads, threads, title, u.nickname " +
                        "FROM forum f JOIN \"user\" u ON f.user_id = u.id " +
                        "WHERE LOWER(slug) = LOWER(?)";
        return jdbcTemplate.query(query, new Object[]{slug}, ForumService::rowMapper);
    }

    public List<ThreadModel> createThread(ThreadModel thread) {

        currentThreadId++;
        //log.info("CurrentTreadId: " + currentThreadId);

        Timestamp timestamp = Timestamp.valueOf(LocalDateTime.parse(thread.getCreated(), DateTimeFormatter.ISO_DATE_TIME));

        if (!thread.getCreated().endsWith("Z")) {
            timestamp = Timestamp.from(timestamp.toInstant().plusSeconds(-10800));
        }

        Boolean hasSlug = Boolean.TRUE;

        if (thread.getSlug() == null) {
            hasSlug = Boolean.FALSE;
        }

        final String query = "INSERT INTO thread(user_id, created, forum_id, \"message\", slug, title) " +
                "VALUES (" +
                "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?)), " +
                "?, " +
                "(SELECT id FROM forum WHERE LOWER(slug) = LOWER(?)), " +
                "?, ?, ?) RETURNING thread.id";
        final Integer threadId = jdbcTemplate.queryForObject(query, new Object[]{thread.getAuthor(), timestamp, thread.getForum(),
                thread.getMessage(), hasSlug ? thread.getSlug() : null, thread.getTitle()}, Integer.class);



        if (hasSlug)
        {
            jdbcTemplate.update("UPDATE forum SET threads = threads + 1 WHERE LOWER(slug) = LOWER(?)",
                    thread.getForum());

            return jdbcTemplate.query(
                    "SELECT t.id, u.nickname, t.created, f.slug fSlug, t.message, t.title, t.slug, t.votes " +
                            "FROM thread t JOIN \"user\" u ON (t.user_id = u.id) JOIN forum f ON (t.forum_id = f.id) " +
                            "WHERE LOWER(t.slug) = LOWER(?)",
                    new Object[]{thread.getSlug()},
                    ThreadService::rowMapper);

        } else {
            jdbcTemplate.update("UPDATE forum SET threads = threads + 1 WHERE id = ?",
                    threadId);

            log.info("ThreadId before query: " + threadId);
            return jdbcTemplate.query(
                    "SELECT t.id, u.nickname, t.created, f.slug fSlug, t.message, t.title, t.slug, t.votes " +
                            "FROM thread t JOIN \"user\" u ON (t.user_id = u.id) JOIN forum f ON (t.forum_id = f.id) " +
                            "WHERE t.id = ?",
                    new Object[]{/*currentThreadId*/threadId},
                    ThreadService::rowMapper);
        }


    }

    public List<ThreadModel> getThreads(String slug, Integer limit, String since, Boolean desc) {
        final StringBuilder query = new StringBuilder("SELECT t.id, u.nickname, t.created, f.slug fSlug, t.message, t.slug, t.title, t.votes " +
                "FROM thread t JOIN \"user\" u ON (t.user_id = u.id) " +
                "JOIN forum f ON (t.forum_id = f.id) " +
                "WHERE LOWER(f.slug) = LOWER(?)");
        final List<Object> arguments = new ArrayList<>();
        arguments.add(slug);

        if (since != null) {
            query.append(" AND created ");
            if (desc == Boolean.TRUE) {
                query.append("<= ?");
            } else {
                query.append(">= ?");
            }
            System.out.println(since);
            arguments.add(Timestamp.valueOf(LocalDateTime.parse(since, DateTimeFormatter.ISO_OFFSET_DATE_TIME)));
        }

        query.append("ORDER BY created");

        if (desc == Boolean.TRUE) {
            query.append(" DESC");
        }

        query.append(" LIMIT ?");
        arguments.add(limit);

        return jdbcTemplate.query(query.toString(), arguments.toArray(new Object[arguments.size()]), ThreadService::rowMapper);

    }

    public List<ThreadModel> getThread(String slug) {
        final String query = "SELECT t.id, u.nickname, t.created, f.slug fSlug, t.message, t.slug, t.title, t.votes " +
                        "FROM thread t JOIN \"user\"  u ON (t.user_id = u.id) " +
                        "JOIN forum f ON (t.forum_id = f.id) " +
                        "WHERE LOWER(t.slug) = LOWER(?)";
        return jdbcTemplate.query(query, new Object[]{slug}, ThreadService::rowMapper);
    }

    public List<UserModel> getUsers(String slug, Integer limit, String since, Boolean desc) {
        final StringBuilder query = new StringBuilder("SELECT u3.id, u3.about, u3.email, u3.nickname, u3.fullname " +
                "FROM \"user\" u3 WHERE u3.id IN " +
                "(SELECT u1.id FROM thread t1 JOIN \"user\" u1 ON (t1.user_id = u1.id) JOIN forum f1 ON (t1.forum_id = f1.id) " +
                "WHERE LOWER(f1.slug) = LOWER(?) " +
                "UNION " +
                "SELECT u2.id FROM post p2 JOIN \"user\" u2 ON (p2.user_id = u2.id) JOIN forum f2 ON (p2.forum_id = f2.id) " +
                "WHERE LOWER(f2.slug) = LOWER(?))");

        final List<Object> arguments = new ArrayList<>();
        arguments.add(slug);
        arguments.add(slug);

        if (since != null) {
            query.append(" AND LOWER(u3.nickname) ");

            if (desc == Boolean.TRUE) {
                query.append("< LOWER(?)");

            } else {
                query.append("> LOWER(?)");
            }
            //System.out.println(since);
            arguments.add(since);
        }

        query.append(" ORDER BY LOWER(u3.nickname)");

        if (desc == Boolean.TRUE) {
            query.append(" DESC");
        }

        query.append(" LIMIT ?");
        arguments.add(limit);

        return jdbcTemplate.query(query.toString(), arguments.toArray(new Object[arguments.size()]), UserService::rowMapper);

    }


    private static ForumModel rowMapper(ResultSet set, int rowNumber) throws SQLException {
        return new ForumModel(
                set.getInt("posts"),
                set.getString("slug"),
                set.getInt("threads"),
                set.getString("title"),
                set.getString("nickname"));
    }
}
