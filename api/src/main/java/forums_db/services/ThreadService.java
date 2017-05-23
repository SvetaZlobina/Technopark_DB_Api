package forums_db.services;

import forums_db.models.PostModel;
import forums_db.models.ThreadModel;
import forums_db.models.VoteModel;
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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
        final StringBuilder queryGetPosts = new StringBuilder("SELECT  p.id, u.nickname, p.created, f.slug, " +
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

        for (PostModel post : posts) {
            if (post.getParent() != 0) {
                final List<Integer> postsDB = jdbcTemplate.queryForList(queryGetParents.toString(), Integer.class, isNumber ? id : slug);
                if (!postsDB.contains(post.getParent())) {
                    throw new DuplicateKeyException(null);
                }
            }

            jdbcTemplate.update(queryInsert.toString(), post.getAuthor(), timestamp, isNumber ? id : slug,
                    post.getIsEdited(), post.getMessage(), post.getParent(), isNumber ? id : slug);
        }

        queryGetPosts.append(" ORDER BY p.id");

        jdbcTemplate.update(queryUpdateForum.toString(), posts.size(), isNumber ? id : slug);

        final List<PostModel> postsDB = jdbcTemplate.query(queryGetPosts.toString(),
                isNumber ? new Object[]{id} : new Object[]{slug}, PostService::rowMapper);
        final Integer startIndex = postsDB.size() - posts.size();
        final Integer endIndex = postsDB.size();
        return postsDB.subList(startIndex, endIndex);

    }

    public List<ThreadModel> createVote(VoteModel vote, String slug) {
        final StringBuilder query = new StringBuilder("UPDATE thread SET votes = votes + ? WHERE ");
        final List<Object> arguments = new ArrayList<>();
        final Integer id;

        final List<VoteModel> usersVotes = jdbcTemplate.query("SELECT u.nickname, v.voice " +
                "FROM vote v JOIN \"user\" u ON (v.user_id = u.id) " +
                "WHERE LOWER(u.nickname) = LOWER(?)", new Object[]{vote.getNickname()}, (rs, rowNum) ->
                new VoteModel(
                        rs.getString("nickname"),
                        rs.getInt("voice")));
        final Map<String, Integer> usersMap = new LinkedHashMap<>();

        for(VoteModel userVote : usersVotes) {
            usersMap.put(userVote.getNickname(), userVote.getVoice());
        }

        if(usersMap.containsKey(vote.getNickname())) {

            if(usersMap.get(vote.getNickname()) < 0 && vote.getVoice() < 0) {
                vote.setVoice(0);
            } else if(usersMap.get(vote.getNickname()) < 0 && vote.getVoice() > 0) {
                vote.setVoice(2);
            } else if(usersMap.get(vote.getNickname()) > 0 && vote.getVoice() < 0) {
                vote.setVoice(-2);
            } else {
                vote.setVoice(0);
            }
            jdbcTemplate.update("UPDATE vote SET voice = voice + ? " +
                    "WHERE user_id = " +
                    "(SELECT id FROM \"user\" WHERE LOWER(nickname) = LOWER(?))", vote.getVoice(), vote.getNickname());
        } else {
            jdbcTemplate.update("INSERT INTO vote (user_id, voice)" +
                    "VALUES(" +
                    "(SELECT id FROM \"user\" u WHERE u.nickname = ?), ?)", vote.getNickname(), vote.getVoice());
        }

        arguments.add(vote.getVoice());

        try {
            id = Integer.valueOf(slug);

        } catch (NumberFormatException e) {
            arguments.add(slug);
            jdbcTemplate.update(query.append("LOWER(slug) = LOWER(?)").toString(), arguments.toArray());
            return jdbcTemplate.query("SELECT t.id, u.nickname, t.created, f.slug fSlug, t.message, t.slug, t.title, t.votes " +
                    "FROM thread t JOIN \"user\"  u ON (t.user_id = u.id) " +
                    "JOIN forum f ON (t.forum_id = f.id) " +
                    "WHERE LOWER(t.slug) = LOWER(?)", new Object[]{slug}, ThreadService::rowMapper);
        }

        arguments.add(id);
        jdbcTemplate.update(query.append("id = ?").toString(), arguments.toArray());

        return jdbcTemplate.query("SELECT t.id, u.nickname, t.created, f.slug fSlug, t.message, t.slug, t.title, t.votes " +
                "FROM thread t JOIN \"user\"  u ON (t.user_id = u.id) " +
                "JOIN forum f ON (t.forum_id = f.id) " +
                "WHERE t.id = ?", new Object[]{id}, ThreadService::rowMapper);
    }

    public List<ThreadModel> getThread(String slug) {
        final StringBuilder query = new StringBuilder("SELECT t.id, u.nickname, t.created, f.slug fSlug, " +
                "t.message, t.slug, t.title, t.votes " +
                "FROM thread t JOIN \"user\"  u ON (t.user_id = u.id) " +
                "JOIN forum f ON (t.forum_id = f.id) WHERE ");
        final Integer id;

        try {
            id = Integer.valueOf(slug);

        } catch (NumberFormatException e) {
            return jdbcTemplate.query(query.append("LOWER(t.slug) = LOWER(?)").toString(),
                    new Object[]{slug}, ThreadService::rowMapper);
        }

        return jdbcTemplate.query(query.append("t.id = ?").toString(),
                new Object[]{id}, ThreadService::rowMapper);
    }

    public void updateThread(ThreadModel thread, String slug) {
        final StringBuilder query = new StringBuilder("UPDATE thread SET");
        final List<Object> arguments = new ArrayList<>();

        if(thread.getMessage() != null && !thread.getMessage().isEmpty()) {
            query.append(" message = ?,");
            arguments.add(thread.getMessage());
        }
        if(thread.getTitle() != null && !thread.getTitle().isEmpty()) {
            query.append(" title = ?,");
            arguments.add(thread.getTitle());
        }
        if(arguments.isEmpty()) {
            return;
        }

        query.delete(query.length()-1, query.length());
        final Integer id;
         try {
             id = Integer.valueOf(slug);
             query.append(" WHERE  id = ?");
             arguments.add(id);

         } catch (NumberFormatException e) {
             query.append(" WHERE LOWER(slug) = LOWER(?)");
             arguments.add(slug);
         }

         jdbcTemplate.update(query.toString(), arguments.toArray());
    }

    public List<PostModel> getSortedPosts(String sort, Boolean desc, String slug) {

        final String template = " tree AS " +
                "(SELECT *, array[id] AS path FROM got_threads WHERE parent_id = 0 " +
                "UNION SELECT g.*, tree.path || g.id AS path FROM tree JOIN got_threads g ON (g.parent_id = tree.id)) " +
                "SELECT * FROM tree ORDER BY path"; //TODO дописать
        final StringBuilder query = new StringBuilder();
        Integer id = null;
        Boolean isNumber = false;

        try {
            final String queryTemplate = "SELECT p.id, u.nickname, p.created, f.slug, p.is_edited, p.message, p.parent_id, p.thread_id " +
                    "FROM post p JOIN \"user\" u ON (p.user_id = u.id) " +
                    "JOIN thread t ON(p.thread_id = t.id) JOIN forum f ON(p.forum_id = f.id) " +
                    "WHERE t.id = ?";
            id = Integer.valueOf(slug);
            isNumber = Boolean.TRUE;

            if (sort.equals("flat")) {
                query.append(queryTemplate + " ORDER BY p.created");

            } else {
                query.append("WITH RECURSIVE got_threads AS (" + queryTemplate + "), " + template);
            }
        } catch (NumberFormatException e ) {
            final String queryTemplate = "SELECT p.id, u.nickname, p.created, f.slug, p.is_edited, p.message, p.parent_id, p.thread_id " +
                    "FROM post p JOIN \"user\" u ON (p.user_id = u.id) " +
                    "JOIN thread t ON(p.thread_id = t.id) JOIN forum f ON(p.forum_id = f.id) " +
                    "WHERE LOWER(t.slug) = LOWER(?) ";

            if (sort.equals("flat")) {
                query.append(queryTemplate + " ORDER BY p.created");

            } else {
                query.append("WITH RECURSIVE got_threads AS (" + queryTemplate + "), " + template);
            }
        }

        if (desc == Boolean.TRUE) {
            query.append(" DESC");
        }

        if (sort.equals("flat")) {
            query.append(", p.id");

            if (desc == Boolean.TRUE) {
                query.append(" DESC");
            }
        }

        return jdbcTemplate.query(query.toString(),
                isNumber ? new Object[]{id} : new Object[]{slug},
                PostService::rowMapper);
        //Integer id = slug.matches("\\d+") ? Integer.valueOf(slug) : null;



        /*switch(sort) {
            case "flat": {
                return jdbcTemplate.query(QueriesForThreadService.postsFlatSort(slug, desc),
                        new Object[]{id == null ? slug : id, limit, offset}, PostService::rowMapper);
            }
//            case "tree": {
//                return jdbcTemplate.query(QueriesForThreadService.postsTreeSort(slug, desc),
//                        new Object[]{id == null ? slug : id, limit, offset}, PostService::rowMapper);
//            }
//            case "parent_tree": {
//                return jdbcTemplate.query(QueriesForThreadService.postsParentTreeSort(slug, desc),
//                        new Object[]{id == null ? slug : id, limit, offset}, PostService::rowMapper);
//            }
            default: {
                throw new NullPointerException();
            }
        }*/
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
                set.getString("title"),
                set.getInt("votes")
        );
    }
}
