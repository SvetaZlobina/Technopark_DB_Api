package forums_db.controllers;

import forums_db.models.MarkerForPostModel;
import forums_db.models.PostModel;
import forums_db.models.ThreadModel;
import forums_db.models.VoteModel;
import forums_db.services.ThreadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@RestController
@RequestMapping(path = "api/thread/{slug}")
public class ThreadController {
    private JdbcTemplate jdbcTemplate;

    private static Integer pageMarker = 0;

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

        } catch (DataAccessException e) {
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

        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.status(HttpStatus.OK).body(threads.get(0));
    }

    @GetMapping(path = "/details")
    public ResponseEntity<?> getThread(@PathVariable("slug") String slug) {
        final List<ThreadModel> threads;

        try {
            threads = threadService.getThread(slug);
            if(threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(threads.get(0));
    }

    @PostMapping(path = "/details")
    public ResponseEntity<?> updateThread(@RequestBody ThreadModel body,
                                          @PathVariable("slug") String slug) {
        final List<ThreadModel> threads;

        try {
            threadService.updateThread(body, slug);
            threads = threadService.getThread(slug);

            if(threads.isEmpty()) {
                throw new EmptyResultDataAccessException(0);
            }
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadService.getThread(slug).get(0));

        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.status(HttpStatus.OK).body(threads.get(0));
    }

    @GetMapping(path = "/posts")
    public ResponseEntity<MarkerForPostModel> getPosts(@RequestParam(value = "limit", required = false, defaultValue = "100") Integer limit,
                                                       @RequestParam(value = "marker", required = false) String marker,
                                                       @RequestParam(value = "sort", required = false, defaultValue = "flat") String sort,
                                                       @RequestParam(value = "desc", required = false, defaultValue = "false") Boolean desc,
                                                       @PathVariable("slug") final String slug) {


        final List<PostModel> posts = threadService.getSortedPosts(sort, desc, slug);

        if (posts.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        if (marker != null && !sort.equals("parent_tree")) {
            pageMarker += limit;
        }

       if (sort.equals("parent_tree")) {

            if (pageMarker >= posts.size() && marker != null) {

                pageMarker = 0;
                return ResponseEntity.status(HttpStatus.OK).body(new MarkerForPostModel(marker, new ArrayList<>()));

            } else if (pageMarker == posts.size()){
                pageMarker = 0;
            }

            Integer start = 0;
            Integer number = 0;

            for (PostModel post: posts.subList(pageMarker, posts.size())) {

                if(start.equals(limit) && desc == Boolean.TRUE) {
                    break;

                } else if (start.equals(limit+1) && (desc == null || desc == Boolean.FALSE)) {
                    number--;
                    break;
                }

                start += post.getParent().equals(0) ? 1 : 0;
                number++;
            }

            return ResponseEntity.status(HttpStatus.OK).body(new MarkerForPostModel(
                    marker, posts.subList(pageMarker, pageMarker+=number)));
       }

       if (pageMarker > posts.size()) {
            pageMarker = 0;

            return ResponseEntity.status(HttpStatus.OK).body(new MarkerForPostModel(marker, new ArrayList<>()));
       } else if (pageMarker == posts.size()) {
            pageMarker = 0;
       }

        return ResponseEntity.status(HttpStatus.OK).body(new MarkerForPostModel(marker,
                posts.subList(pageMarker, limit + pageMarker > posts.size() ? posts.size() : limit + pageMarker)));
    }
}
