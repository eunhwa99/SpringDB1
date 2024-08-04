package hello.jdbc.repository.task;

import com.zaxxer.hikari.HikariDataSource;
import hello.jdbc.domain.Category;
import hello.jdbc.domain.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.NoSuchElementException;

import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TaskRepositoryV1Test {

    TaskRepositoryV1 repository;

    @BeforeEach
    void setUp() {
        //기본 DriverManager - 항상 새로운 커넥션 획득
        DriverManagerDataSource dataSource1 =
        new DriverManagerDataSource(URL, USERNAME, PASSWORD);


        //커넥션 풀링: HikariProxyConnection -> JdbcConnectio
        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(URL);
        dataSource.setUsername(USERNAME);
        dataSource.setPassword(PASSWORD);

        // datasource 주입
        repository = new TaskRepositoryV1(dataSource);
    }

    @Test
    void crudTest() {
        // save
        Task task = new Task("v1", Category.LEISURE, 3);
        repository.save(task);

        // findById
        Task taskById = repository.findById(task.getTaskId());
        assertThat(taskById).isNotNull();

        // update
        repository.updatePriority(task.getTaskId(), 2);
        Task updatedTask = repository.findById(task.getTaskId());
        assertThat(updatedTask.getPriority()).isEqualTo(2);

        // delete
        repository.delete(task.getTaskId());
        assertThatThrownBy(() -> repository.findById(task.getTaskId())).isInstanceOf(NoSuchElementException.class);
    }

}