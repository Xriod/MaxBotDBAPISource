package ru.wrappers;

import ru.entities.Role;

public class RoleResponseWrapper extends GenericResponseWrapper<Role>{
    public RoleResponseWrapper(Role role) {
        super(role);
    }
    public RoleResponseWrapper(String message) {
        super(message);
    }
}
