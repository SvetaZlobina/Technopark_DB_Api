package forums_db.controllers;

import forums_db.models.ServiceModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Created by SvetaZlobina on 21.03.2017.
 */
@RestController
@RequestMapping(path = "/api/service")
public class ServiceController {

    private final JdbcTemplate jdbcTemplate;

    public ServiceController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping("/status")
    public ResponseEntity<ServiceModel> getStatus() {
        final Integer forumsNumber = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM forum", Integer.class);
        final Integer postsNumber = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM post", Integer.class);
        final Integer threadsNumber = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM thread", Integer.class);
        final Integer usersNumber = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM \"user\"", Integer.class);

        return ResponseEntity.status(HttpStatus.OK).body(
                new ServiceModel(forumsNumber, postsNumber, threadsNumber, usersNumber));
    }

    @PostMapping("/clear")
    public ResponseEntity<?> deleteAllInfo() {
        jdbcTemplate.execute("DELETE FROM vote");
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.execute("DELETE FROM thread");
        jdbcTemplate.execute("DELETE FROM forum");
        jdbcTemplate.execute("DELETE FROM \"user\"");

        return ResponseEntity.status(HttpStatus.OK).body(null);
    }
}
