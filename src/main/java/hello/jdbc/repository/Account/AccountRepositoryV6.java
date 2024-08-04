package hello.jdbc.repository.Account;

import hello.jdbc.domain.Account;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

@Slf4j
@AllArgsConstructor
public class AccountRepositoryV6 implements AccountRepository {
    private final JdbcTemplate template;


    @Override
    public Account save(Account account) {
        String sql = "insert into account(account_id, money) value(?,?)";
        template.update(sql, account.getAccountId(), account.getMoney());
        return account;
    }

    @Override
    public Account findById(String accountId) {
        String sql = "select * from account where account_id = ?";
        return template.queryForObject(sql, accountRowMapper(), accountId);
    }

    @Override
    public void update(String accountId, int money) {
        String sql = "update account set money=? where account_id = ?";
        template.update(sql, money, accountId);
    }

    @Override
    public void delete(String accountId) {
        String sql = "delete from account where account_id=?";
        template.update(sql, accountId);
    }

    private RowMapper<Account> accountRowMapper() {
        return (rs, rowNum) ->
                new Account(rs.getString("account_id"), rs.getInt("money"));

    }
}
