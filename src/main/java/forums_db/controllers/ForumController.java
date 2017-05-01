package forums_db.controllers;

import forums_db.models.ForumModel;
import forums_db.models.ThreadModel;
import forums_db.services.ForumService;
import forums_db.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
//import java.sql.Timestamp;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@RestController
@RequestMapping(path = "api/forum")
public class ForumController {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ForumService forumService;

    @Autowired
    private UserService userService;

    public ForumController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.forumService = new ForumService(jdbcTemplate);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> createForum(@RequestBody ForumModel body) {
        List<ForumModel> duplicates = forumService.getForum(body.getSlug());
        if (duplicates.isEmpty()) {
            try {
                forumService.createForum(body);
            } catch (DataAccessException e) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
            }
            duplicates = forumService.getForum(body.getSlug());
            return ResponseEntity.status(HttpStatus.CREATED).body(duplicates.get(0));
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicates.get(0));
        }
    }

    @PostMapping(path = "/{slug}/create")
    public ResponseEntity<?> createThread(@RequestBody ThreadModel body,
                                          @PathVariable("slug") String slug) {
        if (body.getSlug() == null) {
            body.setSlug(slug);
        }
        if (body.getCreated() == null) {
            body.setCreated(LocalDateTime.now().toString());
        }

        final List<ThreadModel> threads;
        try {
            threads = forumService.createThread(body);
            if (threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(null);
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (threads.get(0).getSlug().equals(threads.get(0).getForum())) {
            threads.get(0).setSlug(null);
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(threads.get(0));
    }

    @GetMapping(path = "/{slug}/details")
    public ResponseEntity<?> getForumDetailes(@PathVariable("slug") String slug) {
        final List<ForumModel> duplicates = forumService.getForum(slug);
        if (duplicates.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(duplicates.get(0));
        }
    }

    @GetMapping(path = "/{slug}/threads")
    public ResponseEntity<?> getThreads(
            @RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
            @RequestParam(value = "since", required = false) String since,
            @RequestParam(value = "desc", required = false) Boolean desc,
            @PathVariable("slug") String slug) {
        try {
            final List<ForumModel> forums = forumService.getForum(slug);
            if (forums.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (EmptyResultDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(forumService.getThreads(slug, limit, since, desc));
    }
}
