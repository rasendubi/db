package badcoders.database;

import badcoders.model.*;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.tweak.HandleCallback;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.*;
import java.util.List;

public class Database {

    private final DBI dbi;

    public static final String filmSchema = "film (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "name tinytext NOT NULL," +
            "director tinytext NOT NULL," +
            "actors text NOT NULL," +
            "genre tinytext NOT NULL," +
            "description text NOT NULL);";

    private final static String commentSchema = "comment (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id int(11) NOT NULL," +
            "film_id int(11) NOT NULL," +
            "text text NOT NULL);";

    private final static String filmScoreSchema = "film_score (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "film_id int(11) NOT NULL," +
            "user_id int(11) NOT NULL," +
            "score tinyint(4) NOT NULL);";

    private final static String recommendationSchema = "recommendation (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id int(11) NOT NULL," +
            "film_id int(11) NOT NULL," +
            "score tinyint(4) NOT NULL);";

    private final static String registrationSchema = "registration (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "user_id int(11) NOT NULL," +
            "code bigint(20) NOT NULL UNIQUE);";

    private final static String userSchema = "user (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT," +
            "login tinytext NOT NULL," +
            "password tinytext NOT NULL," +
            "email tinytext NOT NULL," +
            "date_created timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP," +
            "is_admin BOOL);";

    public static final String[] SCHEMAS = new String[]{
            commentSchema,
            filmSchema,
            filmScoreSchema,
            recommendationSchema,
            registrationSchema,
            userSchema
    };

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static String getUrl(String name) {
        return "jdbc:sqlite:" + name + ".sdb";
    }

    public Database(String name) {
        dbi = new DBI(getUrl(name));
        /* Film mapper is not static. That's why we should create instance manually */
        dbi.registerMapper(new FilmDao.FilmMapper(this));
    }

    public void createModel() throws SQLException {
        for (final String schema : SCHEMAS) {
            dbi.withHandle(new HandleCallback<Object>() {
                @Override
                public Object withHandle(Handle handle) throws Exception {
                    handle.execute("CREATE TABLE IF NOT EXISTS " + schema);
                    return null;
                }
            });
        }
    }

    /**
     * @retval null if given user not exists.
     */
    public Account getUser(final String name, final String password) throws SQLException {
        AccountDao dao = dbi.open(AccountDao.class);
        Account result = dao.getUser(name, password);
        dao.close();
        return result;
    }

    public long addUser(final String name, final String password, final boolean isAdmin, final String email) throws SQLException {
        AccountDao dao = dbi.open(AccountDao.class);
        long result = dao.addUser(name, password, isAdmin, email);
        dao.close();
        return result;
    }

    public Account getUser(long id) throws SQLException {
        AccountDao dao = dbi.open(AccountDao.class);
        Account result = dao.getUser(id);
        dao.close();
        return result;
    }

    public static class FilmStatsMapper implements ResultSetMapper<FilmStats> {

        @Override
        public FilmStats map(int i, ResultSet resultSet, StatementContext statementContext) throws SQLException {
            FilmStats result = new FilmStats();
            result.mean_score = resultSet.getDouble("mean_score");
            result.vote_count = resultSet.getLong("vote_count");
            return result;
        }
    }

    public FilmStats getFilmStats(final long id) throws SQLException {
        return dbi.withHandle(new HandleCallback<FilmStats>() {
            @Override
            public FilmStats withHandle(Handle handle) throws Exception {
                return handle.createQuery("SELECT AVG(score) AS mean_score, COUNT(*) AS vote_count FROM film_score WHERE film_id = :id")
                        .bind("id", id).map(new FilmStatsMapper()).first();
            }
        });
    }


    /**
     * @return list of all films.
     */
    public List<Film> getFilms() throws SQLException {
        FilmDao dao = dbi.open(FilmDao.class);
        List<Film> result = dao.getFilms();
        dao.close();
        return result;
    }

    public long addFilm(Film film) throws SQLException {
        FilmDao dao = dbi.open(FilmDao.class);
        long result = dao.addFilm(film);
        dao.close();
        return result;
    }

    public Film getFilm(long id) throws SQLException {
        FilmDao dao = dbi.open(FilmDao.class);
        Film result = dao.getFilm(id);
        dao.close();
        return result;
    }

    public boolean canRateFilm(final Account account, final long filmId) throws SQLException {
        return dbi.withHandle(new HandleCallback<Boolean>() {
            @Override
            public Boolean withHandle(Handle handle) throws Exception {
                return handle.createQuery("SELECT id FROM film_score WHERE film_id = :film_id AND user_id = :user_id")
                        .bind("film_id", filmId)
                        .bind("user_id", account.id)
                        .first() != null;
            }
        });
    }

    public void rateFilm(final Account account, final long filmId, final int rate) throws SQLException {
        dbi.withHandle(new HandleCallback<Object>() {
            @Override
            public Object withHandle(Handle handle) throws Exception {
                handle.execute("INSERT INTO film_score(film_id, user_id, score) VALUES (?, ?, ?)", filmId, account.id, rate);
                return null;
            }
        });
    }

    public static class RecommendationMapper implements ResultSetMapper<Recommendation> {
        @Override
        public Recommendation map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
            return new Recommendation(rs.getLong("user_id"), rs.getLong("film_id"), rs.getDouble("score"));
        }
    }

    public List<Recommendation> getRecommendation(final Account account) throws SQLException {
        return dbi.withHandle(new HandleCallback<List<Recommendation>>() {
            @Override
            public List<Recommendation> withHandle(Handle handle) throws Exception {
                return handle.createQuery("SELECT * FROM recommendation WHERE user_id = :user_id")
                        .bind("user_id", account.id).map(new RecommendationMapper()).list();
            }
        });
    }

    public long addComment(Account account, long filmId, String text) throws SQLException {
        CommentDao dao = dbi.open(CommentDao.class);
        long result = dao.addComment(filmId, account.id, text);
        dao.close();
        return result;
    }

    public Comment getComment(long id) throws SQLException {
        CommentDao dao = dbi.open(CommentDao.class);
        Comment result = dao.getComment(id);
        dao.close();
        return result;
    }

    public List<Comment> getFilmComments(long filmId) throws SQLException {
        CommentDao dao = dbi.open(CommentDao.class);
        List<Comment> result = dao.getFilmComments(filmId);
        dao.close();
        return result;
    }

    public void deleteComment(long id) throws SQLException {
        CommentDao dao = dbi.open(CommentDao.class);
        dao.deleteComment(id);
        dao.close();
    }

}