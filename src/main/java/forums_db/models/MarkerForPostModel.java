package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Created by SvetaZlobina on 03.05.2017.
 */
public class MarkerForPostModel {
    private List<PostModel> posts;
    private String marker;

    @JsonCreator
    public MarkerForPostModel(@JsonProperty("marker") String marker,
                              @JsonProperty("posts") List<PostModel> posts) {
        this.marker = marker == null ? "default marker" : marker;
        this.posts = posts;
    }

    public List<PostModel> getPosts() {
        return this.posts;
    }
    public String getMarker() {
        return this.marker;
    }

    public void setPosts(List<PostModel> posts) {
        this.posts = posts;
    }
    public void setMarker(String marker) {
        this.marker = marker;
    }
}
