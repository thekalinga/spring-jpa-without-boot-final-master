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
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.EnableTransactionManagement;

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
import static org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType.H2;

public class SpringJpaWithoutBootApplication {
  public static void main(String[] args) {
    AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
    context.register(DataAccessConfiguration.class, UserServiceImpl.class);
    context.refresh();
    context.registerShutdownHook();
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
