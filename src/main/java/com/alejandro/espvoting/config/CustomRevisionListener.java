package com.alejandro.espvoting.config;

import com.alejandro.espvoting.model.CustomRevisionEntity;
import org.hibernate.envers.RevisionListener;

/**
 * Custom revision listener for Hibernate Envers.
 * This class is used to capture additional information about the revisions,
 * such as the user who made the change.
 */
public class CustomRevisionListener implements RevisionListener {

    /**
     * This method is called when a new revision is created.
     * It can be used to set additional information on the revision entity.
     *
     * @param revisionEntity the revision entity
     */
    @Override
    public void newRevision(Object revisionEntity) {
        CustomRevisionEntity customRevisionEntity = (CustomRevisionEntity) revisionEntity;
        
        // Here you can set additional information on the revision entity
        // For example, you could set the username of the current user
        // customRevisionEntity.setUsername(SecurityContextHolder.getContext().getAuthentication().getName());
    }
}