package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SvetaZlobina on 23.05.2017.
 */
public class ServiceModel {
    private Integer forum;
    private Integer post;
    private Integer thread;
    private Integer user;

    @JsonCreator
    public ServiceModel(@JsonProperty("forum") Integer forum,
                        @JsonProperty("post") Integer post,
                        @JsonProperty("thread") Integer thread,
                        @JsonProperty("user") Integer user) {

        this.forum = forum;
        this.post = post;
        this.thread = thread;
        this.user = user;
    }

    public Integer getForum() {
        return this.forum;
    }
    public Integer getPost() {
        return this.post;
    }
    public Integer getThread() {
        return this.thread;
    }
    public Integer getUser() {
        return this.user;
    }

    public void setForum(Integer forum) {
        this.forum = forum;
    }
    public void setPost(Integer post) {
        this.post = post;
    }
    public void setThread(Integer thread) {
        this.thread = thread;
    }
    public void setUser(Integer user) {
        this.user = user;
    }
}
