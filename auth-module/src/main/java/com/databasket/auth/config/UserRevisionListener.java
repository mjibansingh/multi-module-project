package com.databasket.auth.config;

import org.hibernate.envers.RevisionListener;

public class UserRevisionListener implements RevisionListener {
    @Override
    public void newRevision(Object revisionEntity) {
        UserRevEntity exampleRevEntity = (UserRevEntity) revisionEntity;
        exampleRevEntity.setUsername(SecurityContext.getCurrentUser() != null ? SecurityContext.getCurrentUser().getUsername() : "Anonymous");
    }
}