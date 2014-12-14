package badcoders.database;

import badcoders.model.Film;
import badcoders.model.FilmStats;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.lang.annotation.*;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public interface FilmDao {
    @SqlQuery("SELECT * FROM film")
    List<Film> getFilms();

    @SqlQuery("SELECT * FROM film WHERE id = :id")
    Film getFilm(@Bind("id") long id);

    @SqlUpdate("INSERT INTO film(name, director, actors, genre, description) VALUES(:name, :director, :actors, :genre, :description)")
    @GetGeneratedKeys
    long addFilm(@BindFilm Film film);

    void close();

    public static class FilmMapper implements ResultSetMapper<Film> {
        private final Database db;

        public FilmMapper(Database db) {
            this.db = db;
        }

        @Override
        public Film map(int i, ResultSet rs, StatementContext statementContext) throws SQLException {
            FilmStats stats = db.getFilmStats(rs.getLong("id"));
            return new Film(
                    rs.getLong("id"),
                    rs.getString("name"),
                    rs.getString("director"),
                    rs.getString("actors"),
                    rs.getString("genre"),
                    rs.getString("description"),
                    stats.mean_score,
                    stats.vote_count);
        }
    }
}

@BindingAnnotation(BindFilm.FilmBinderFactory.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PARAMETER})
@interface BindFilm {
    public static class FilmBinderFactory implements BinderFactory {
        @Override
        public Binder build(Annotation annotation) {
            return new Binder<BindFilm, Film>() {
                @Override
                public void bind(SQLStatement<?> q, BindFilm bindFilm, Film film) {
                    q.bind("name", film.name);
                    q.bind("director", film.director);
                    q.bind("actors", film.actors);
                    q.bind("genre", film.genre);
                    q.bind("description", film.description);
                }
            };
        }
    }

}
