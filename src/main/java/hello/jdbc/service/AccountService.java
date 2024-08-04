package hello.jdbc.service;

import hello.jdbc.domain.Account;
import hello.jdbc.repository.Account.AccountRepositoryV0;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

import static hello.jdbc.common.Constants.TEST_ID_FOR_EXCEPTION;

/*
create table account(
account_id varchar(20) not null,
money number not null
);

 */
@Slf4j
@AllArgsConstructor
public class AccountService {

    private final DataSource dataSource;
    private final AccountRepositoryV0 repository;

    public void transferMoney(String fromId, String toId, int money) throws SQLException {
        Connection con = dataSource.getConnection(); // 커넥션 얻기

        try {
            con.setAutoCommit(false); // 트랜잭션 시작

            // business logic
            businessLogic(con, fromId, toId, money);

            con.commit();
        } catch (Exception e) {
            con.rollback(); // 실패시 롤백
            throw new IllegalStateException(e);
        } finally {
            release(con);
        }
    }

    private void businessLogic(Connection con, String fromId, String toId, int money) throws SQLException {
        Account fromAccount = repository.findById(con, fromId);
        Account toAccount = repository.findById(con, toId);

        repository.update(con, fromId, fromAccount.getMoney() - money);
        // exception occur
        validation(toAccount);
        repository.update(con, toId, toAccount.getMoney() + money);
    }

    private void validation(Account account) {
        if (account.getAccountId().equals(TEST_ID_FOR_EXCEPTION)) {
            throw new IllegalStateException("Exception while transferring!!");
        }
    }

    private void release(Connection con) {
        if (con != null) {
            try {
                con.setAutoCommit(true); // 풀에 반납하기 전에 default 값이 자동커밋모드로 바꿔주는 것이 좋다!!
                con.close(); // 커넥션 종료가 아니라 풀에 반납한다는 의미!
            } catch (SQLException e) {
                log.error("error", e);
            }
        }
    }

}
