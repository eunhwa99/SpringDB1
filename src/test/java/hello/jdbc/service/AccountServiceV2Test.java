package hello.jdbc.service;

import hello.jdbc.domain.Account;
import hello.jdbc.repository.Account.AccountRepositoryV2;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.SQLException;

import static hello.jdbc.common.Constants.*;
import static hello.jdbc.connection.ConnectionConst.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest // Integration test
class AccountServiceV2Test {
    @Autowired
    private AccountRepositoryV2 repository;
    @Autowired
    private AccountServiceV2 service;

    @AfterEach
    void after() throws SQLException {
        repository.delete(ACCOUNT_A);
        repository.delete(ACCOUNT_B);
        repository.delete(TEST_ID_FOR_EXCEPTION);
    }

    @TestConfiguration
    @RequiredArgsConstructor
    static class TestConfig {
        private final DataSource dataSource;
       /* @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(URL, USERNAME, PASSWORD);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }*/

        @Bean
        AccountRepositoryV2 accountRepositoryV2(DataSource dataSource) {
            return new AccountRepositoryV2(dataSource);
        }

        @Bean
        AccountServiceV2 accountServiceV2(AccountRepositoryV2 accountRepositoryV2) {
            return new AccountServiceV2(accountRepositoryV2);
        }


    }

    @Test
    void AopCheck() {
        log.info("accountService class={}", service.getClass());
        log.info("accountRepository class={}", repository.getClass());
        assertThat(AopUtils.isAopProxy(service)).isTrue();
        assertThat(AopUtils.isAopProxy(repository)).isFalse();
    }


    @Test
    @DisplayName("정상 이체")
    void transferMoney() throws SQLException {
        Account accountA = new Account(ACCOUNT_A, 50000);
        Account accountB = new Account(ACCOUNT_B, 10000);
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
        Account accountA = new Account(ACCOUNT_A, 50000);
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