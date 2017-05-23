package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
public class ForumModel {
    private Integer posts;
    private String slug;
    private Integer threads;
    private String title;
    private String user;

    @JsonCreator
    public ForumModel(@JsonProperty("posts") Integer posts,
                      @JsonProperty("slug") String slug,
                      @JsonProperty("threads") Integer threads,
                      @JsonProperty("title") String title,
                      @JsonProperty("user") String user) {
        this.posts = posts;
        this.slug = slug;
        this.threads = threads;
        this.title = title;
        this.user = user;
    }

    public Integer getPosts() {
        return this.posts;
    }

    public String getSlug() {
        return this.slug;
    }

    public Integer getThreads() {
        return this.threads;
    }

    public String getTitle() {
        return this.title;
    }

    public String getUser() {
        return this.user;
    }

    public void setPosts(Integer posts) {
        this.posts = posts;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUser(String user) {
        this.user = user;
    }
}
