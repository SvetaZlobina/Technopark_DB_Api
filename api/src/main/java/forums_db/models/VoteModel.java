package forums_db.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
public class VoteModel {
    private String nickname;
    private Integer voice;

    @JsonCreator
    public VoteModel(@JsonProperty("nickname") String nickname,
                     @JsonProperty("voice") Integer voice) {
        this.nickname = nickname;
        this.voice = voice;
    }

    public String getNickname() {
        return this.nickname;
    }
    public Integer getVoice() {
        return this.voice;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }
    public void setVoice(Integer voice) {
        this.voice = voice;
    }
}
