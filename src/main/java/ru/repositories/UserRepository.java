package ru.repositories;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.PersistenceException;
import jakarta.ws.rs.NotFoundException;
import ru.exceptions.AlreadyExistException;
import ru.entities.Role;
import ru.entities.User;

@ApplicationScoped
public class UserRepository {

    //Добавление юзера
    public Uni<User> addUser(Long id, String name) throws PersistenceException, AlreadyExistException {
        String trimmedName = name==null?null:name.trim();
        return Panache.withTransaction(()->
                User.<User>findById(id)
                .onItem().ifNotNull()
                .failWith(()->new AlreadyExistException("User already exists"))
                .chain(user->getDefaultUserRole().chain(role->{
                    User newUser = new User();
                    newUser.setId(id);
                    newUser.setMaxName(trimmedName);
                    newUser.setRole(role);
                    return newUser.persistAndFlush();
                }))
        );
    }

    //Получить роль юзера по id
    public Uni<Role> getUserRole(Long id) throws IllegalArgumentException, PersistenceException {
        if(id == null || id<=0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId must be positive and not null"));
        }

        return Panache.withTransaction(()->User.<User>findById(id).onItem()
                .ifNull().failWith(()->new NotFoundException("User not found"))
                .chain(user -> {
                    if(user.getRole()==null) {
                        return Uni.createFrom().failure(()->new NotFoundException("User has no role assigned"));
                    }
                    return Uni.createFrom().item(user.getRole());
                })
        );
    }

    //Повысить пользователя до админа
    public Uni<Void> promoteUser(Long id) throws IllegalArgumentException, PersistenceException, NotFoundException {
        if(id == null || id<=0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId must be positive and not null"));
        }

        return Panache.withTransaction(()->
                User.<User>findById(id).onItem().ifNull()
                 .failWith(()->new NotFoundException("User not found"))
                 .chain(user->getAdminUserRole().chain(role -> {
                     user.setRole(role);
                     return Uni.createFrom().voidItem();
                 }))
        );
    }

    //понизить пользователя
    public Uni<Void> demoteUser(Long id) throws IllegalArgumentException, PersistenceException, NotFoundException {
        if(id == null || id<=0) {
            return Uni.createFrom().failure(()-> new IllegalArgumentException("UserId must be positive and not null"));
        }
        return Panache.withTransaction(()->
                User.<User>findById(id).onItem().ifNull()
                 .failWith(()->new NotFoundException("Target user not found"))
                .chain(user->getDefaultUserRole().chain(role->{
                    user.setRole(role);
                    return Uni.createFrom().voidItem();
                }))
        );
    }

    //блокировать пользователя
    public Uni<Void> blockUser(Long id) throws IllegalArgumentException, PersistenceException, NotFoundException {
        if(id == null || id<=0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId must be positive and not null"));
        }
        return Panache.withTransaction(()->
                User.<User>findById(id).onItem().ifNull()
                        .failWith(()->new NotFoundException("Target user not found"))
                        .chain(user->getBlockedUserRole().chain(role->{
                            user.setRole(role);
                            return Uni.createFrom().voidItem();
                        }))
                );
    }

    //разблокировать пользователя
    public Uni<Void> unBlockUser(Long id) throws IllegalArgumentException, PersistenceException, NotFoundException {
        if(id == null || id<=0) {
            return Uni.createFrom().failure(()->new IllegalArgumentException("UserId must be positive and not null"));
        }
        return Panache.withTransaction(()->
                User.<User>findById(id).onItem().ifNull()
                        .failWith(()->new NotFoundException("Target user not found"))
                        .chain(user->getDefaultUserRole().chain(role->{
                            user.setRole(role);
                            return Uni.createFrom().voidItem();
                        }))
        );
    }
    private Uni<Role> getDefaultUserRole() {
        return Role.<Role>find("name","user").firstResult()
                .onItem().ifNull().failWith(()->new NotFoundException("Default user role not found"));
    }

    private Uni<Role> getAdminUserRole() {
        return Role.<Role>find("name","admin").firstResult()
                .onItem().ifNull().failWith(()->new NotFoundException("Admin user role not found"));
    }

    private Uni<Role> getBlockedUserRole() {
        return Role.<Role>find("name","blocked").firstResult()
                .onItem().ifNull().failWith(()->new NotFoundException("Blocked user role not found"));
    }
}
