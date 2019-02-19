package com.acme.jpa.without.boot;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {
  @Test
  void getAllUsersWithNamePrefix(@Autowired UserRepository userRepository, @Autowired EntityManager entityManager) {
    // given
    User swasthikA = User.builder().name("Swasthik A").build();
    entityManager.persist(swasthikA);
    User rashmi = User.builder().name("Rashmi").build();
    entityManager.persist(rashmi);
    User swasthikB = User.builder().name("Swasthik B").build();
    entityManager.persist(swasthikB);

    assertThat(userRepository.getAllUsersWithNamePrefix("Swast%")).containsOnlyOnce(swasthikA, swasthikB);
  }
}
