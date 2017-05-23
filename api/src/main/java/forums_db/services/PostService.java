package forums_db.services;

import forums_db.models.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@Service
public class PostService {
    private JdbcTemplate jdbcTemplate;

    public PostService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<PostModel> getPost(Integer id) {
        return jdbcTemplate.query(
                "SELECT p.id, u.nickname, p.created, f.slug, p.is_edited, p.message, p.parent_id, p.thread_id " +
                "FROM post p JOIN \"user\" u ON (p.user_id = u.id) " +
                        "JOIN forum f ON (p.forum_id = f.id) " +
                        "WHERE p.id = ?",
                new Object[]{id}, PostService::rowMapper);
    }

    public DetailsForPostModel getPostDetailes(PostModel post, String[] related) {
        UserModel user = null;
        ForumModel forum = null;
        ThreadModel thread = null;

        if(related != null) {

            for (String relation: related) {

                if (relation.equals("user")) {
                    UserService userService = new UserService(jdbcTemplate);
                    List<UserModel> users = userService.getUser(
                            new UserModel(null, null, null, post.getAuthor()));

                    if (!users.isEmpty()) {
                        user = users.get(0);
                    }
                }

                if (relation.equals("forum")) {
                    ForumService forumService = new ForumService(jdbcTemplate);
                    List<ForumModel> forums = forumService.getForum(post.getForum());

                    if (!forums.isEmpty()) {
                        forum = forums.get(0);
                    }
                }

                if (relation.equals("thread")) {
                    ThreadService threadService = new ThreadService(jdbcTemplate);
                    List<ThreadModel> threads = threadService.getThread(post.getThread().toString());

                    if (!threads.isEmpty()) {
                        thread = threads.get(0);
                    }
                }
            }
        }
        return new DetailsForPostModel(post, user, forum, thread);
    }

    public List<PostModel> updatePost(PostModel post, Integer id) {

        final StringBuilder query = new StringBuilder("UPDATE post SET \"message\" = ?");
        List<PostModel> posts = getPost(id);

        if(posts.isEmpty()) {
            return posts;
        }

        if(!post.getMessage().equals(posts.get(0).getMessage())) {
            query.append(", is_edited = true");
        }

        query.append(" WHERE id = ?");
        jdbcTemplate.update(query.toString(), post.getMessage(), id);

        return getPost(id);
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
