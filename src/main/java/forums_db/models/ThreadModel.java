package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
public class ThreadModel {
    private String author;
    private String created;
    private String forum;
    private Integer id;
    private String message;
    private String slug;
    private String title;
    private Integer votes;

    @JsonCreator
    public ThreadModel(@JsonProperty("author") String author,
                       @JsonProperty("created") String created,
                       @JsonProperty("forum") String forum,
                       @JsonProperty("id") Integer id,
                       @JsonProperty("message") String message,
                       @JsonProperty("slug") String slug,
                       @JsonProperty("title") String title,
                       @JsonProperty("votes") Integer votes) {
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.id = id;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes;
    }

    public String getAuthor() {
        return this.author;
    }

    public String getCreated() {
        return this.created;
    }

    public String getForum() {
        return this.forum;
    }

    public Integer getId() {
        return this.id;
    }

    public String getMessage() {
        return this.message;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getTitle() {
        return this.title;
    }

    public Integer getVotes() {
        return this.votes;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCreated(String created) {this.created = created;}

    public void setForum(String forum) {
        this.forum = forum;
    }

    public void setId(Integer id) {this.id = id;}

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSlug(String slug) {this.slug = slug;}

    public void setTitle(String title) {
        this.title = title;
    }

    public void setVotes(Integer votes) {
        this.votes = votes;
    }
}
