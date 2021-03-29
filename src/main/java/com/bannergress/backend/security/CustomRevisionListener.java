package com.bannergress.backend.security;

import org.hibernate.envers.RevisionListener;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.bannergress.backend.entities.Revision;

public class CustomRevisionListener implements RevisionListener {
	@Override
	public void newRevision(Object revisionEntity) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication != null) {
			((Revision) revisionEntity).setUserid(authentication.getName());
		}
	}
}