package ru.repositories;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.logging.Logger;
import ru.entities.FAQ;
import ru.entities.Theme;

import java.util.List;
import java.util.Objects;

@ApplicationScoped
public class FAQRepository {

    private static final Log log = LogFactory.getLog(FAQRepository.class);
    private final Logger logger = Logger.getLogger(FAQRepository.class);
    //Добавить вопрос/ответ
    public Uni<FAQ> addNewFAQ(String question, String answer, Integer themeId) {
        if (question.isBlank() || answer.isBlank() || Objects.toString(themeId,"").isBlank()) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("Invalid input data."));
        }

        return Panache.withTransaction(()-> Theme.<Theme>findById(themeId).onItem()
                .ifNull().failWith(()->new NotFoundException("Theme with Id = " + themeId + " not found"))
                .chain(theme -> {
                    FAQ faq = new FAQ();
                    faq.setQuestion(question);
                    faq.setAnswer(answer);
                    faq.setTheme(theme);
                    return faq.persistAndFlush();
                })
        );
    }

    public Uni<FAQ> updateFAQ(Long id, String question, String answer, Integer themeId) {
        return Panache.<FAQ>withTransaction(()-> FAQ.<FAQ>findById(id).onItem()
                .ifNull().failWith(()->new NotFoundException("FAQ with Id = " + id + " not found"))
                .chain(faq->Theme.<Theme>findById(themeId).chain(theme -> {
                    if(theme==null) {
                        return Uni.createFrom().failure(()->new NotFoundException("Theme with Id =  " + themeId + " not found"));
                    }
                    faq.setQuestion(question);
                    faq.setAnswer(answer);
                    faq.setTheme(theme);
                    return faq.persistAndFlush();
                })))
                .onFailure().recoverWithUni(failure->{
                   logger.warn("updateFAQ ошибка транзакции", failure);
                   return Uni.createFrom().failure(failure);
                });

    }

    //Удалить FAQ по id
    public Uni<Void> deleteFAQ(Long id) {
        if (id == null || id <= 0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("Id cannot be null or empty"));
        }
        return Panache.withTransaction(()->FAQ.deleteById(id).onItem()
                .transformToUni(wasDeleted->{
                    if(!wasDeleted){
                        return Uni.createFrom().failure(()->new NotFoundException("FAQ with Id = " + id + " not found"));
                    }
                    return Uni.createFrom().voidItem();
                })
        );
    }

    //Получить все FAQ по теме
    public Uni<List<FAQ>> findByThemeId(Integer themeId) {
        if (themeId == null || themeId<=0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("Theme Id cannot be null or negative"));
        }
        return Panache.withTransaction(()->Theme.<Theme>findById(themeId).onItem()
                .ifNull().failWith(()->new NotFoundException("Theme with Id = " + themeId + " not found"))
                .chain(theme-> FAQ.<FAQ>find("theme.id=?1",Sort.by("id"),themeId).list())
        );
    }

}
