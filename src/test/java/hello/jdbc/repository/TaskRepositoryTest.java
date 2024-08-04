package hello.jdbc.repository;

import hello.jdbc.domain.Category;
import hello.jdbc.domain.Task;
import hello.jdbc.repository.task.TaskRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class TaskRepositoryTest {
    TaskRepository taskRepository;


    @Test
    void crudTest() {
        taskRepository = new TaskRepository();
        Task task = new Task("1", Category.STUDY, 1);
        taskRepository.save(task);

        Task findTask = taskRepository.findById("1");
        log.info("findTask={}", findTask); // @Data 가 toString() 을 적절히 오버라이딩 해서 보여준다
        assertThat(findTask).isEqualTo(task); // @Data 가 toString() 을 적절히 오버라이딩 해서 보여주기 때문

        taskRepository.updatePriority(task.getTaskId(), 2);
        Task updateTask = taskRepository.findById("1");
        assertThat(updateTask.getPriority()).isEqualTo(2);

    }

    @Test
    void deleteTest() {
        taskRepository = new TaskRepository();
        taskRepository.delete("1");
        assertThatThrownBy(() -> taskRepository.findById("1")).isInstanceOf(NoSuchElementException.class);
    }



}

/*
DROP TABLE IF EXISTS task CASCADE;

CREATE TABLE task (
    task_id VARCHAR(10),
    category ENUM('STUDY', 'SELF_DEVELOPMENT', 'LEISURE') NOT NULL,
    priority INTEGER NOT NULL DEFAULT 0,
    PRIMARY KEY (task_id)
);


 */