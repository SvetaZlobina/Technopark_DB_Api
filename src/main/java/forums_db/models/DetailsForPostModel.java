package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SvetaZlobina on 23.05.2017.
 */
public class DetailsForPostModel {
    private PostModel post;
    private UserModel author;
    private ForumModel forum;
    private ThreadModel thread;

    @JsonCreator
    public DetailsForPostModel(@JsonProperty("post") PostModel post,
                               @JsonProperty("author") UserModel author,
                               @JsonProperty("forum") ForumModel forum,
                               @JsonProperty("thread") ThreadModel thread){
        this.post = post;
        this.author = author;
        this.forum = forum;
        this.thread = thread;
    }

    public PostModel getPost() {
        return this.post;
    }
    public UserModel getAuthor() {
        return this.author;
    }
    public ForumModel getForum() {
        return this.forum;
    }
    public ThreadModel getThread() {
        return this.thread;
    }

    public void setPost(PostModel post) {
        this.post = post;
    }
    public void setAuthor(UserModel author) {
        this.author = author;
    }
    public void setForum(ForumModel forum) {
        this.forum = forum;
    }
    public void setThread(ThreadModel thread) {
        this.thread = thread;
    }
}
