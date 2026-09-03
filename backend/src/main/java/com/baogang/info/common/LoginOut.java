package com.baogang.info.common;

import java.util.List;
import java.util.Map;

public class LoginOut {
    private String token;
    private Map<String,Object> user;
    private List<String> roles;
    private List<String> rights;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Map<String, Object> getUser() {
        return user;
    }

    public void setUser(Map<String, Object> user) {
        this.user = user;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public List<String> getRights() {
        return rights;
    }

    public void setRights(List<String> rights) {
        this.rights = rights;
    }

    public LoginOut(String token, Map<String, Object> user) {
        this.token = token;
        this.user = user;
    }

    public LoginOut() {
    }
}
