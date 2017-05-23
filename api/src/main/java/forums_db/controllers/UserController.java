package forums_db.controllers;

import forums_db.models.UserModel;
import forums_db.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by SvetaZlobina on 20.03.2017.
 */
@RestController
@RequestMapping(path = "api/user/{nickname}")
public class UserController {
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserService userService;

    public UserController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        this.userService = new UserService(jdbcTemplate);
    }

    @PostMapping(path = "/create")
    public ResponseEntity<?> createUserProfile(@RequestBody UserModel body,
                                        @PathVariable(value = "nickname") String nickname) {
        body.setNickname(nickname);
        List<UserModel> duplicates = userService.getUser(body);
        if (duplicates.isEmpty()) {
            userService.createUser(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(body);
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(duplicates);
        }
    }

    @GetMapping(path = "/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable(value = "nickname") String nickname) {
        final UserModel userForCheck = new UserModel(null, null, null, nickname);
        final List<UserModel> allSuchUsers = userService.getUser(userForCheck);
        if (allSuchUsers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        } else {
            return ResponseEntity.status(HttpStatus.OK).body(allSuchUsers.get(0));
        }
    }

    @PostMapping(path = "/profile")
    public ResponseEntity<?> updateUserProfile(@RequestBody UserModel body,
                                               @PathVariable(value = "nickname") String nickname) {
        body.setNickname(nickname);
        UserModel userForCheck = new UserModel(null, null, null, nickname);
        List<UserModel> allSuchUsers = userService.getUser(userForCheck);
        if (allSuchUsers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("{}");
        }

        if (body.getAbout() == null) {
            body.setAbout(allSuchUsers.get(0).getAbout());
        }
        if (body.getFullname() == null) {

            body.setFullname(allSuchUsers.get(0).getFullname());
        }
        if (body.getEmail() == null) {
            body.setEmail(allSuchUsers.get(0).getEmail());
        } else {
            userForCheck = new UserModel(null, body.getEmail(), null, null);
            allSuchUsers.clear();
            allSuchUsers = userService.getUser(userForCheck);
            if (!allSuchUsers.isEmpty()) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(allSuchUsers);
            }
        }

        userService.updateUser(body);
        return ResponseEntity.status(HttpStatus.OK).body(body);
    }

}
