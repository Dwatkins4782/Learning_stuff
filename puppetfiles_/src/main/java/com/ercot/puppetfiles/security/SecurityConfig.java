package com.ercot.puppetfiles.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.ldap.DefaultSpringSecurityContextSource;

@Configuration
@EnableWebSecurity
@PropertySource(value = {"classpath:application.properties", "classpath:puppetfiles.properties", "file:/etc/opt/ercot/puppetfiles/configuration/application.properties", "file:/etc/opt/ercot/puppetfiles/secrets/puppetfiles.properties"}, ignoreResourceNotFound = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    @Value("${com.ercot.puppetfiles.secure.endpoints}")
    private String secureEndpoints;

    @Value("${com.ercot.puppetfiles.ldap.userBase}")
    private String userSearchBase;

    @Value("${com.ercot.puppetfiles.ldap.filter}")
    private String userSearchFilter;

    @Value("${com.ercot.puppetfiles.ldap.url}")
    private String url;

    @Value("${com.ercot.puppetfiles.ldap.rootDn}")
    private String rootDn;

    @Value("${com.ercot.puppetfiles.ldap.managerDn}")
    private String managerDn;

    @Value("${com.ercot.puppetfiles.ldap.managerPw}")
    private String managerPassword;

    @Override
    protected void configure(final HttpSecurity http) throws Exception {
        final String[] endPointsArray = secureEndpoints.split(",");

        http.csrf()
            .disable()
            .requestMatchers()
            .antMatchers(endPointsArray)
            .and()
            .authorizeRequests()
            .antMatchers(endPointsArray)
            .authenticated()
            .and()
            .httpBasic()
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }

    @Override
    protected void configure(final AuthenticationManagerBuilder auth) throws Exception {
        try {
            final DefaultSpringSecurityContextSource ctx = new DefaultSpringSecurityContextSource(url + "/" + rootDn);
            ctx.setUserDn(managerDn);
            ctx.setPassword(managerPassword);
            ctx.setReferral("follow");
            ctx.afterPropertiesSet();

            auth.ldapAuthentication()
                .userSearchFilter(userSearchFilter)
                .userSearchBase(userSearchBase)
                .contextSource(ctx);
        }
        catch (Exception ex) {
           logger.error("Error attempting to configure LDAP authentication.");
            throw ex;
        }
    }
}