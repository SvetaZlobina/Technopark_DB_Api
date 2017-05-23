package forums_db.services;

/**
 * Created by SvetaZlobina on 03.05.2017.
 */
public class QueriesForThreadService {
    public static String postsFlatSort(String slug, Boolean desc) {
        return "SELECT p.id, u.nickname, p.created, f.slug, p.is_edited, p.message, p.parent_id, p.thread_id " +
                "FROM post p JOIN \"user\" u ON (p.user_id = u.id) " +
                "JOIN thread t ON(p.thread_id = t.id) JOIN forum f ON(p.forum_id = f.id)" +
                (slug.matches("\\d+") ? "WHERE t.id = ?" : "WHERE LOWER(t.slug) = LOWER(?) ") +
                        "ORDER BY p.created " + (desc ? "DESC" : "ASC") +
                ", p.id " + (desc? "DESC" : "ASC") + " LIMIT ? OFFSET ?";
    }
}
