package hello.jdbc.repository.Account;

import hello.jdbc.domain.Account;

public interface AccountRepository {
    Account save(Account account);
    Account findById(String accountId);
    void update(String accountId, int money);
    void delete(String accountId);
}
