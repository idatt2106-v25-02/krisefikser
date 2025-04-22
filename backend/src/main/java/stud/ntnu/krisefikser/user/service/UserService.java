package stud.ntnu.krisefikser.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import stud.ntnu.krisefikser.user.repository.UserRepo;

@Service
@RequiredArgsConstructor
public class UserService {
  private final UserRepo userRepo;
}
