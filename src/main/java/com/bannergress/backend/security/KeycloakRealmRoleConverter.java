package com.bannergress.backend.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, Collection<GrantedAuthority>> {
    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        @SuppressWarnings("unchecked")
        final Map<String, Object> realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
        @SuppressWarnings("unchecked")
        List<String> roleList = (List<String>) realmAccess.get("roles");
        return roleList.stream() //
            .map(t -> new SimpleGrantedAuthority("ROLE_" + t)) //
            .collect(Collectors.toList());
    }
}
