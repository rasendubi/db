package badcoders.database;

import badcoders.model.Comment;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@RegisterMapper(CommentDao.CommentMapper.class)
public interface CommentDao {
    @SqlUpdate("INSERT INTO comment(film_id, user_id, text) VALUES (:film_id, :user_id, :text)")
    @GetGeneratedKeys
    long addComment(@Bind("film_id") long film_id, @Bind("user_id") long user_id, @Bind("text") String text);

    @SqlQuery("SELECT * FROM comment WHERE id = :id")
    Comment getComment(@Bind("id") long id);

    @SqlQuery("SELECT * FROM comment WHERE film_id = :film_id")
    List<Comment> getFilmComments(@Bind("film_id") long film_id);

    @SqlUpdate("DELETE FROM comment WHERE id = :id")
    void deleteComment(@Bind("id") long id);

    void close();

    class CommentMapper implements ResultSetMapper<Comment> {
        @Override
        public Comment map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
            return new Comment(rs.getLong("id"), rs.getLong("user_id"), rs.getLong("film_id"), rs.getString("text"));
        }
    }
}
