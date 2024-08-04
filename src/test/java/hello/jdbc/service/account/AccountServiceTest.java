package hello.jdbc.service.account;

import hello.jdbc.domain.Account;
import hello.jdbc.repository.Account.AccountRepositoryV0;
import hello.jdbc.service.AccountService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.sql.SQLException;

import static hello.jdbc.common.Constants.TEST_ID_FOR_EXCEPTION;
import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AccountServiceTest {

    private AccountRepositoryV0 repository;
    private AccountService service;

    @BeforeEach
    void setUp() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        repository = new AccountRepositoryV0(dataSource);
        service = new AccountService(dataSource, repository);
    }

    @AfterEach
    void tearDown() throws SQLException {
        repository.delete("accountA");
        repository.delete("accountB");
        repository.delete(TEST_ID_FOR_EXCEPTION);
    }

    @Test
    @DisplayName("정상 이체")
    void transferMoney() throws SQLException {
        Account accountA = new Account("accountA", 50000);
        Account accountB = new Account("accountB", 10000);
        repository.save(accountA);
        repository.save(accountB);

        service.transferMoney(accountA.getAccountId(), accountB.getAccountId(), 10000);

        Account findAccountA = repository.findById(accountA.getAccountId());
        Account findAccountB = repository.findById(accountB.getAccountId());
        assertThat(findAccountA.getMoney()).isEqualTo(40000);
        assertThat(findAccountB.getMoney()).isEqualTo(20000);
    }


    @Test
    @DisplayName("이체중 예외 발생")
    void transferException() throws SQLException {
        Account accountA = new Account("accountA", 50000);
        Account accountEx = new Account(TEST_ID_FOR_EXCEPTION, 10000);
        repository.save(accountA);
        repository.save(accountEx);

        assertThatThrownBy(() -> service.transferMoney(accountA.getAccountId(), accountEx.getAccountId(), 10000)).isInstanceOf(IllegalStateException.class);

        Account findAccountA = repository.findById(accountA.getAccountId());
        Account findAccountEx = repository.findById(accountEx.getAccountId());
        assertThat(findAccountA.getMoney()).isEqualTo(50000);
        assertThat(findAccountEx.getMoney()).isEqualTo(10000);
    }
}