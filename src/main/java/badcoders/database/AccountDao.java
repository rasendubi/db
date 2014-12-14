package badcoders.database;

import badcoders.model.Account;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;

@RegisterMapper(AccountDao.AccountMapper.class)
public interface AccountDao {
    @SqlUpdate("INSERT INTO user(login, password, is_admin, email) VALUES (:login, :password, :is_admin, :email)")
    @GetGeneratedKeys
    long addUser(@Bind("login") String login, @Bind("password") String password,
                 @Bind("is_admin") boolean isAdmin, @Bind("email") String email);

    @SqlQuery("SELECT id, login, is_admin FROM user WHERE login = :login AND password = :password")
    Account getUser(@Bind("login") String name, @Bind("password") String password);

    @SqlQuery("SELECT * FROM user WHERE id = :id")
    Account getUser(@Bind("id") long id);

    void close();

    public static class AccountMapper implements ResultSetMapper<Account> {
        @Override
        public Account map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            return new Account(resultSet.getLong("id"), resultSet.getString("login"), resultSet.getBoolean("is_admin"));
        }
    }
}


