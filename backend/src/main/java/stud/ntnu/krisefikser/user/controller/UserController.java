package stud.ntnu.krisefikser.user.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import stud.ntnu.krisefikser.user.service.UserService;

@RestController
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;
}
