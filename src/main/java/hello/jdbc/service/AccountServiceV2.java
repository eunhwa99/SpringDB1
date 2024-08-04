package hello.jdbc.service;

import hello.jdbc.domain.Account;
import hello.jdbc.repository.Account.AccountRepositoryV2;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

import static hello.jdbc.common.Constants.TEST_ID_FOR_EXCEPTION;

@Slf4j
@AllArgsConstructor
public class AccountServiceV2 {
    // private final PlatformTransactionManager transactionManager;
    private final AccountRepositoryV2 repository;

    @Transactional
    public void transferMoney(String fromId, String toId, int money) {
        //   TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition());
        try {
            businessLogic(fromId, toId, money);
            //        transactionManager.commit(status);
        } catch (Exception e) {
            //       transactionManager.rollback(status); // 실패 시 롤백
            throw new IllegalStateException(e);
        }
    }

    private void businessLogic(String fromId, String toId, int money) throws SQLException {
        Account fromAccount = repository.findById(fromId);
        Account toAccount = repository.findById(toId);

        repository.update(fromId, fromAccount.getMoney() - money);
        validation(toAccount);
        repository.update(toId, toAccount.getMoney() + money);
    }

    private void validation(Account account) {
        if (account.getAccountId().equals(TEST_ID_FOR_EXCEPTION)) {
            throw new IllegalStateException("Exception while transferring!!");
        }
    }
}
