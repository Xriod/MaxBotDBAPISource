package ru.repositories;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import ru.entities.User;
import ru.entities.UserQuestion;

import java.time.LocalDateTime;
import java.util.List;

@ApplicationScoped
public class UserQuestionsRepository {

    //Добавить новый вопрос от пользователя
    public Uni<UserQuestion> addNewQuestion(Long maxId, String question) throws PersistenceException,
            IllegalArgumentException,
            NotFoundException
    {
        return Panache.withTransaction(()->
                User.<User>findById(maxId).onItem()
                .ifNull().failWith(()->new NotFoundException("Target user not found"))
                .chain(user->{
                    UserQuestion userQuestion = new UserQuestion();
                    userQuestion.setQuestion(question);
                    userQuestion.setUser(user);
                    userQuestion.setQuestionCreationDate(LocalDateTime.now());
                    return userQuestion.persistAndFlush();
                })
        );
    }

    //Вернуть все вопросы пользователя
    public Uni<List<UserQuestion>> getAllQuestions(Long maxId) throws PersistenceException,
            IllegalArgumentException,
            NotFoundException
    {
        if (maxId == null) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId cannot be null"));
        }
        if(maxId <=0) {
           return Uni.createFrom().failure(()->new IllegalArgumentException("UserId cannot be negative"));
        }

        return Panache.withTransaction(()->
                User.<User>findById(maxId).onItem()
                .ifNull().failWith(()->new NotFoundException("User with id = "+maxId+" not found"))
                .chain(user-> UserQuestion.find("user.id=?1",Sort.by("id"),maxId).list())
        );
    }

    //Удалить все вопросы пользователя
    public Uni<Void> removeAllQuestionsByUser(Long userId) {
        if(userId == null) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId cannot be null"));
        }
        if (userId <= 0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId cannot be negative"));
        }

        return Panache.withTransaction(()->
                User.<User>findById(userId).onItem()
                        .ifNull().failWith(()->new NotFoundException("Target user not found"))
                        .chain(user->
                            UserQuestion.delete("user",user)
                        )).replaceWithVoid();
    }
}
