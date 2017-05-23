package forums_db.controllers;

import forums_db.models.DetailsForPostModel;
import forums_db.models.PostModel;
import forums_db.services.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@RestController
@RequestMapping(path = "api/post")
public class PostController {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostService postService;

    public PostController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.postService = new PostService(jdbcTemplate);
    }

    @GetMapping(path = "/{id}/details")
    public ResponseEntity<DetailsForPostModel> getPostDetails(
            @RequestParam(value = "related", required = false) String[] related,
            @PathVariable("id") Integer id) {
        List<PostModel> posts;
        try {

            posts = postService.getPost(id);
            if(posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DataAccessException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(postService.getPostDetailes(posts.get(0), related));
    }

    @PostMapping(path = "/{id}/details")
    public ResponseEntity<PostModel> updatePost(
            @RequestBody PostModel post,
            @PathVariable("id") Integer id) {

        final List<PostModel> posts;

        try {
            if (post.getMessage() != null) {

                posts = postService.updatePost(post, id);
            } else {
                posts = postService.getPost(id);
            }

            if(posts.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (EmptyResultDataAccessException e) {

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(posts.get(0));
    }
}
