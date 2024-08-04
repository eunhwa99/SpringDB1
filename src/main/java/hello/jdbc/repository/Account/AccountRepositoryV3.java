package hello.jdbc.repository.Account;

import hello.jdbc.domain.Account;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

import static hello.jdbc.common.Constants.ACCOUNT_A;


@Slf4j
@AllArgsConstructor
public class AccountRepositoryV3 implements AccountRepository {
    private DataSource dataSource;
    private final SQLExceptionTranslator exTranslator;

    @Override
    public Account save(Account account) {
        String sql = "insert into account(account_id, money) value(?,?)";
        Connection con = null;
        PreparedStatement pstmt = null;


        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, ACCOUNT_A);
            pstmt.setInt(2, 5000);
            pstmt.executeUpdate();
            return account;
        } catch (SQLException e) {
            // 파라미터 1 : 읽을 수 있는 설명
            // 파라미터 2 : 실행한 sql
            // 파라미터 3 : 발생된 SQLException
            throw exTranslator.translate("save", sql, e);
        } finally {
            close(con, pstmt, null);
        }

    }

    @Override
    public Account findById(String accountId) {
        String sql = "select * from account where account_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, accountId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Account(accountId, rs.getInt("money"));
            } else {
                throw new NoSuchElementException("Not found accountId = " + accountId);
            }
        } catch (SQLException e) {
            // 예외를 변환할 때는 기존 예외를 꼭! 포함하자.
            // 장애가 발생하고 로그에서 진짜 원인이 남지 않는 심각한 문제가 발생할 수 있다!!!!
           // throw new MyDbException(e);
            throw exTranslator.translate("findById", sql, e);

        } finally {
            close(con, pstmt, rs);
        }
    }

    @Override
    public void update(String accountId, int money) {
        String sql = "update account set money=? where account_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setInt(1, money);
            pstmt.setString(2, accountId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            throw exTranslator.translate("update", sql, e);

        } finally {
            close(con, pstmt, null);
        }
    }

    @Override
    public void delete(String accountId) {
        String sql = "delete from account where account_id=?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw exTranslator.translate("delete", sql, e);

        } finally {
            close(con, pstmt, null);
        }
    }

    private Connection getConnection() {
        return DataSourceUtils.getConnection(dataSource);
    }

    private void close(Connection con, Statement stmt, ResultSet rs) {
        JdbcUtils.closeResultSet(rs);
        JdbcUtils.closeStatement(stmt);
        DataSourceUtils.releaseConnection(con, dataSource);
    }
}
