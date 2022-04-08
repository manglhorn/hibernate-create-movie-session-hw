package mate.academy.dao.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import mate.academy.dao.MovieSessionDao;
import mate.academy.exception.DataProcessingException;
import mate.academy.lib.Dao;
import mate.academy.model.MovieSession;
import mate.academy.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

@Dao
public class MovieSessionDaoImpl implements MovieSessionDao {
    @Override
    public MovieSession add(MovieSession movieSession) {
        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            transaction = session.beginTransaction();
            session.save(movieSession);
            transaction.commit();
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            throw new DataProcessingException("Can't save movie session: "
                    + movieSession, e);
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return movieSession;
    }

    @Override
    public Optional<MovieSession> get(Long id) {
        try (Session session = HibernateUtil.getSessionFactory()
                .openSession()) {
            Query<MovieSession> getMovieSessionByIdQuery
                    = session.createQuery("from MovieSession ms "
                    + "left join fetch ms.cinemaHall "
                    + "left join fetch ms.movie "
                    + "where ms.id = :id", MovieSession.class);
            getMovieSessionByIdQuery.setParameter("id", id);
            return getMovieSessionByIdQuery.uniqueResultOptional();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get movie session by id: "
                    + id, e);
        }
    }

    @Override
    public List<MovieSession> findAvailableSessions(Long movieId, LocalDate date) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<MovieSession> findMovieSessionQuery = session
                    .createQuery("from MovieSession ms "
                            + "left join fetch ms.movie movie "
                            + "left join fetch ms.cinemaHall "
                            + "where movie.id = :id "
                            + "and ms.showTime between :startDay and :endDay", MovieSession.class);
            findMovieSessionQuery.setParameter("id", movieId);
            findMovieSessionQuery.setParameter("startDay", date.atTime(LocalTime.MIN));
            findMovieSessionQuery.setParameter("endDay", date.atTime(LocalTime.MAX));
            return findMovieSessionQuery.getResultList();
        } catch (Exception e) {
            throw new DataProcessingException("Can't get movie sessions by movie_id: "
                    + movieId
                    + " and localdate: " + date, e);
        }
    }
}