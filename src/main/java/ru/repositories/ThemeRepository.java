package ru.repositories;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.NotFoundException;
import ru.exceptions.AlreadyExistException;
import ru.Constants;
import ru.entities.Theme;
import java.util.List;

@ApplicationScoped
public class ThemeRepository {

    // Получить все темы по алфавиту
    public Uni<List<Theme>> getAllSorted() {
        return Panache.withTransaction(()->
                Theme.listAll(Sort.by("name"))
        );
    }

    //удалить тему по id
    public Uni<Void> deleteById(Integer id) throws NotFoundException, IllegalArgumentException {
        if(id==null || id<=0) {
            return Uni.createFrom().failure(new IllegalArgumentException("Id must be a positive number"));
        }

        return Panache.withTransaction(()->Theme.deleteById(id)
                    .onItem().transformToUni(wasdeleted->{
                        if (!wasdeleted) {
                            return Uni.createFrom().failure(()->new NotFoundException("Theme not found"));
                        }
                        return Uni.createFrom().voidItem();
                    })
        );
    }

    //Добавить тему
    public Uni<Theme> addTheme(String name) throws AlreadyExistException, IllegalArgumentException {
        if(name==null || name.isEmpty()) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("Name of theme can not be null or empty"));
        }
        if(name.length()> Constants.MAX_USERNAME_LENGTH) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("Name must be shorter than " + Constants.MAX_USERNAME_LENGTH + " characters"));
        }
        String trimmedName = name.trim();
        return Panache.withTransaction(()->
                Theme.<Theme>find("name", trimmedName).firstResult()
                .onItem().ifNotNull().failWith(()->new AlreadyExistException("Theme with such name already exist"))
                .chain(theme-> {
                   Theme newTheme = new Theme();
                   newTheme.setName(trimmedName);
                   return newTheme.persistAndFlush();
                }));
    }
}
