package ru.wrappers;

import ru.entities.User;

public class UserResponseWrapper extends GenericResponseWrapper<User> {

    public UserResponseWrapper(User user) {
        super(user);
    }

    public UserResponseWrapper(String message) {
        super(message);
    }
}
