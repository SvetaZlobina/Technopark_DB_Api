package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
public class PostModel {

    private Integer id;
    private String author;
    private String created;
    private String forum;
    private Boolean isEdited;
    private String message;
    private Integer parent;
    private Integer thread;

    @JsonCreator
    public PostModel(@JsonProperty("id") Integer id,
                     @JsonProperty("author") String author,
                     @JsonProperty("created") String created,
                     @JsonProperty("forum") String forum,
                     @JsonProperty("isEdited") Boolean isEdited,
                     @JsonProperty("message") String message,
                     @JsonProperty("parent") Integer parent,
                     @JsonProperty("thread") Integer thread) {
        this.id = id;
        this.author = author;
        this.created = created;
        this.forum = forum;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent == null ? 0 : parent;
        this.thread = thread;
    }

    public Integer getId() {
        return this.id;
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
    public Boolean getIsEdited() {
        return this.isEdited;
    }
    public String getMessage() {
        return this.message;
    }
    public Integer getParent() {
        return this.parent;
    }
    public Integer getThread() {
        return this.thread;
    }

    public void setId(Integer id) {
        this.id = id;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    public void setCreated(String created) {
        this.created = created;
    }
    public void setForum(String forum) {
        this.forum = forum;
    }
    public void setIsEdited(Boolean isEdited) {
        this.isEdited = isEdited;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public void setParent(Integer parent) {
        this.parent = parent;
    }
    public void setThread(Integer thread) {
        this.thread = thread;
    }
}
