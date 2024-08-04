package hello.jdbc.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Task {
    String taskId;
    Category category;
    int priority;

    public static Task create(String taskId, String categoryValue, int priority) {

        Category category = Category.getCategory(categoryValue);
        if (category == null) {
            throw new IllegalArgumentException("Invalid Category");
        }
        return new Task(taskId, category, priority);

    }

    public String getCategory() {
        return category.getValue();
    }
}
