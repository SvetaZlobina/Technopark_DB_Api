package forums_db.controllers;

import forums_db.models.PostModel;
import forums_db.models.ThreadModel;
import forums_db.models.VoteModel;
import forums_db.services.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
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
@RequestMapping(path = "api/thread/{slug}")
public class ThreadController {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ThreadService threadService;

    public ThreadController(final JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.threadService = new ThreadService(jdbcTemplate);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> createPosts(@RequestBody List<PostModel> body,
                                         @PathVariable("slug") String slug) {
        final List<PostModel> posts;
        try {
            if(body.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }
            posts = threadService.createPosts(body, slug);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(posts);
    }

    @PostMapping(path = "/vote")
    public ResponseEntity<?> createVote(@RequestBody VoteModel body,
                                         @PathVariable("slug") String slug) {
        final List<ThreadModel> threads;
        try {
            threads = threadService.createVote(body, slug);

            if(threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }

        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadService.getThread(slug).get(0));

        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(threads.get(0));
    }

}
