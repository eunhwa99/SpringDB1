package hello.jdbc.service;

import hello.jdbc.domain.Account;
import hello.jdbc.repository.Account.AccountRepositoryV1;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.sql.SQLException;

import static hello.jdbc.common.Constants.TEST_ID_FOR_EXCEPTION;

@Slf4j
@AllArgsConstructor
public class AccountServiceV1 {
    private final PlatformTransactionManager transactionManager;
    private final AccountRepositoryV1 repository;

    public void transferMoney(String fromId, String toId, int money) {
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            // business logic
            businessLogic(fromId, toId, money);
            transactionManager.commit(status);
        } catch (Exception e) {
            transactionManager.rollback(status); // 실패 시 롤백
            throw new IllegalStateException(e);
        }
    }

    private void businessLogic(String fromId, String toId, int money) throws SQLException {
        Account fromAccount = repository.findById(fromId);
        Account toAccount = repository.findById(toId);

        repository.update(fromId, fromAccount.getMoney() - money);
        // exception occur
        validation(toAccount);
        repository.update(toId, toAccount.getMoney() + money);
    }

    private void validation(Account account) {
        if (account.getAccountId().equals(TEST_ID_FOR_EXCEPTION)) {
            throw new IllegalStateException("Exception while transferring!!");
        }
    }
}
