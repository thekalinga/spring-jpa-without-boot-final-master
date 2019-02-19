package com.acme.jpa.without.boot;

import jdk.nashorn.internal.runtime.options.Option;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.query.Param;
import org.springframework.http.MediaType;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import static org.hibernate.cfg.AvailableSettings.FORMAT_SQL;
import static org.hibernate.cfg.AvailableSettings.HBM2DDL_AUTO;
import static org.hibernate.cfg.AvailableSettings.SHOW_SQL;
import static org.springframework.http.MediaType.APPLICATION_JSON_UTF8_VALUE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

@SpringBootApplication
public class SpringJpaWithoutBootApplication {
  public static void main(String[] args) {
    SpringApplication.run(SpringJpaWithoutBootApplication.class);
  }
}

@RestController
@RequestMapping("/users")
class UserResource {
  private final UserService service;

  public UserResource(UserService service) {
    this.service = service;
  }

  @GetMapping(produces = APPLICATION_JSON_UTF8_VALUE)
  List<User> users(@RequestParam String prefix) {
    return service.getAllUsersWithNamePrefix(prefix);
  }
}

interface UserService {
  List<User> getAllUsersWithNamePrefix(String prefix);
}


@Service
class UserServiceImpl implements UserService {
  public final UserRepository userRepository;

  public UserServiceImpl(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  public List<User> getAllUsersWithNamePrefix(String prefix) {
    return userRepository.getAllUsersWithNamePrefix(prefix + "%");
  }
}


@Configuration
@EnableTransactionManagement
@EnableJpaRepositories
class DataAccessConfiguration {
  @Bean
  DataSource dataSource() {
    return new EmbeddedDatabaseBuilder().setType(H2).build();
  }

  @Bean
  LocalContainerEntityManagerFactoryBean entityManagerFactory(DataSource dataSource) {
    LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
    factoryBean.setPackagesToScan(User.class.getPackage().getName());
    factoryBean.setDataSource(dataSource);
    factoryBean.setJpaVendorAdapter(new HibernateJpaVendorAdapter());

    Properties jpaProperties = new Properties();
    jpaProperties.setProperty(SHOW_SQL, "true");
    jpaProperties.setProperty(FORMAT_SQL, "true");
    jpaProperties.setProperty(HBM2DDL_AUTO, "create-drop");
    factoryBean.setJpaProperties(jpaProperties);

    return factoryBean;
  }


  @Bean
  JpaTransactionManager transactionManager() {
    return new JpaTransactionManager();
  }
}


interface UserRepository extends JpaRepository<User, Integer> {
  @Query("select u from User as u where u.name like :name")
  List<User> getAllUsersWithNamePrefix(@Param("name") String prefix);
}


@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "name")
@ToString
class User {
  @Id
  @GeneratedValue
  private int id;
  private String name;
}


// uncomment if you want the application to be loaded with dummy data
//@Component
//class Loader {
//  private final TransactionTemplate transactionTemplate;
//  private final EntityManager entityManager;
//
//  public Loader(TransactionTemplate transactionTemplate, EntityManager entityManager) {
//    this.transactionTemplate = transactionTemplate;
//    this.entityManager = entityManager;
//  }
//
//  @PostConstruct
//  void init() {
//    transactionTemplate.execute(status -> {
//      entityManager.persist(User.builder().name("Swasthik A").build());
//      entityManager.persist(User.builder().name("Swasthik B").build());
//      return null;
//    });
//  }
//}
