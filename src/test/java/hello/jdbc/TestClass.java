package hello.jdbc;

import hello.jdbc.domain.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.support.JdbcUtils;
import org.springframework.jdbc.support.SQLErrorCodeSQLExceptionTranslator;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

import static hello.jdbc.connection.ConnectionConst.*;

public class TestClass {
    Repository repository;
    Service service;

    @BeforeEach
    void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL,
                USERNAME, PASSWORD);
        repository = new Repository(dataSource, new SQLErrorCodeSQLExceptionTranslator(dataSource));
        service = new Service(repository);
    }

    @Test
    void duplicateKeySave() {
        service.create("myId");
        service.create("myId");//같은 ID 저장 시도
    }

    @RequiredArgsConstructor
    static class Repository {
        private final DataSource dataSource;
        private final SQLErrorCodeSQLExceptionTranslator translator;

        public Account save(Account account) {
            String sql = "insert into member(member_id, money) values(?,?)";
            Connection con = null;
            PreparedStatement pstmt = null;

            try {
                con = dataSource.getConnection();
                pstmt = con.prepareStatement(sql);
                pstmt.setString(1, account.getAccountId());
                pstmt.setInt(2, account.getMoney());
                pstmt.executeUpdate();
                return account;
            } catch (SQLException e) {
                //h2 db
               /* if (e.getErrorCode() == 23505) {
                    throw new MyDuplicateKeyException(e);
                }
               throw new MyDbException(e);*/
                throw translator.translate("insertTest", sql, e);
            } finally {
                JdbcUtils.closeStatement(pstmt);
                JdbcUtils.closeConnection(con);
            }
        }
    }

    @Slf4j
    @RequiredArgsConstructor
    static class Service {
        private final Repository repository;

        public void create(String accountId) {
            try {
                repository.save(new Account(accountId, 0));
                log.info("saveId={}", accountId);
            } catch (DuplicateKeyException e) {
                log.info("키 중복, 복구 시도");
                String retryId = generateNewId(accountId);
                log.info("retryId={}", retryId);
                repository.save(new Account(retryId, 0));
            } catch (DataAccessException e) {
                log.info("데이터 접근 계층 예외", e);
                throw e;
            }
        }

        private String generateNewId(String memberId) {
            return memberId + new Random().nextInt(10000);
        }
    }
}
