package hello.jdbc.service;

import hello.jdbc.domain.Account;
import hello.jdbc.repository.Account.AccountRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@AllArgsConstructor
public class AccountServiceV3 {
    private final AccountRepository accountRepository;

    @Transactional
    public void transferMoney(String fromId, String toId, int money) {
        Account from = accountRepository.findById(fromId);
        Account to = accountRepository.findById(toId);

        accountRepository.update(fromId, from.getMoney() - money);
        validate(to);
        accountRepository.update(toId, to.getMoney() + money);
    }

    private void validate(Account toAccount) {
        if (toAccount.getAccountId().equals("ex")) {
            throw new IllegalStateException("이체 중 예외 발생");
        }
    }
}
