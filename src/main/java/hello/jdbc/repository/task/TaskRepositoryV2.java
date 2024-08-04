package hello.jdbc.repository.task;

import hello.jdbc.domain.Task;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
public class TaskRepositoryV2 {
    private final DataSource dataSource;

    public Task save(Task task) {
        // DB에 전달할 SQL 정의
        String sql = "insert into task(task_id, category, priority) values(?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            // DB에 전달할 SQL과 파라미터((?,?,?) 부분)로 전달할 데이터 준비
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, task.getTaskId()); // 첫번째 ? 값에 지정  (String 형)
            pstmt.setObject(2, task.getCategory()); // 두번째 ? 값에 지정 (Enum 형)
            pstmt.setInt(3, task.getPriority()); // 세번째 ? 값에 지정 (int 형)
            pstmt.executeUpdate(); // 실제 DB에 전달
            // executeUpdate() 은 int 를 반환하는데 영향받은 DB row 수를 반환한다 (여기서는 1)
            return task;
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        } finally {
            // 리소스 정리!! 꼭!! (메모리 누수로 이어질 수 있음)
            close(con, pstmt, null);
        }
    }

    public Task findById(Connection con, String taskId) {
        String sql = "select * from task where task_id = ?";

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, taskId);
            rs = pstmt.executeQuery(); // 조회할 때는 executeQuery!! ->  ResultSet에 결과 반환

            if (rs.next()) {
                return Task.create(rs.getString("task_id"), rs.getString("category"), rs.getInt("priority"));
            } else throw new NoSuchElementException("task not found task_id = " + taskId);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            //connection은 여기서 닫지 않는다.
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(pstmt);
        }
    }

    public void updatePriority(Connection con, String taskId, int priority) {
        String sql = "update task set priority = ? where task_id = ?";

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, priority);
            pstmt.setString(2, taskId);
            int resultSize = pstmt.executeUpdate();
            // executeUpdate() 는 쿼리 실행하고 영향받은 row 수 반환
            // 하나의 데이터만 변경하므로 1 반환
            log.info("resultSize={}", resultSize);
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        } finally {
            //connection은 여기서 닫지 않는다.
            JdbcUtils.closeStatement(pstmt);
        }
    }


    public void delete(String taskId) {
        String sql = "delete from task where task_Id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, taskId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            log.error("db error", e);
            throw new RuntimeException(e);
        } finally {
            close(con, pstmt, null);
        }
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        JdbcUtils.closeConnection(con);
    }

    private Connection getConnection() throws SQLException {
        Connection con = dataSource.getConnection();
        log.info("get connection={}, class={}", con, con.getClass());
        return con;
    }
}
