package com.acme.jpa.without.boot;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

  @Test
  void getAllUsersWithNamePrefix_ShouldReturnAllUsersWithPrefix(@Mock UserRepository userRepository) {
    List<User> users = asList(User.builder().id(1).name("Swasthik A").build(), User.builder().id(2).name("Swasthik B").build());
    when(userRepository.getAllUsersWithNamePrefix("Swast%")).thenReturn(users);

    UserServiceImpl userService = new UserServiceImpl(userRepository);
    assertThat(userService.getAllUsersWithNamePrefix("Swast")).containsOnlyElementsOf(users);
  }

}
