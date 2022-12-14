package com.ganga.config;

import com.ganga.sercurity.filter.LoginVerifyCodeFilter;
import com.ganga.service.LoginUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.RememberMeServices;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenBasedRememberMeServices;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;
import java.rmi.server.UID;
import java.util.UUID;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final LoginUserDetailsService userDetailsService;
    private final DataSource dataSource;
    @Autowired
    public SecurityConfig(LoginUserDetailsService userDetailsService,DataSource dataSource) {
        this.userDetailsService = userDetailsService;
        this.dataSource = dataSource;
    }



    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .antMatchers("/**/*.html").permitAll()
                .antMatchers("/**/*.css").permitAll()
                .antMatchers("/**/*.js").permitAll()
                .antMatchers("/**/*.jpg").permitAll()
                .mvcMatchers("/ico.jpg").permitAll()
                .mvcMatchers("/login").permitAll()
                .mvcMatchers("/doLogin").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/login")
                .passwordParameter("passwd")
                .usernameParameter("uname")
                .loginProcessingUrl("/doLogin")
                .and()
                .rememberMe()
                .rememberMeParameter("remember-me")
                .rememberMeServices(rememberMeServices())
                .tokenRepository(persistentTokenRepository())
                .and()
                .csrf().disable();

        //????????????
        http.addFilterAt(loginVerifyCodeFilter(), UsernamePasswordAuthenticationFilter.class);
    }


    /**
     * ?????????????????? formLogin??????  ??????????????????
     *
     * @return UsernamePasswordAuthenticationFilter ??????
     * @throws Exception
     */
    @Bean
    public LoginVerifyCodeFilter loginVerifyCodeFilter() throws Exception {
        LoginVerifyCodeFilter loginVerifyCodeFilter = new LoginVerifyCodeFilter();
        //????????????????????? ????????????
        loginVerifyCodeFilter.setFilterProcessesUrl("/doLogin");
        loginVerifyCodeFilter.setUsernameParameter("uname");
        loginVerifyCodeFilter.setPasswordParameter("passwd");
        loginVerifyCodeFilter.setVerifyCodeParameter("verifyCode");
        //?????? ???????????????
        loginVerifyCodeFilter.setAuthenticationManager(authenticationManagerBean());
        //rememberMe
        loginVerifyCodeFilter.setRememberMeServices(rememberMeServices());
        //????????????????????????
        loginVerifyCodeFilter.setAuthenticationSuccessHandler((req,resp,authentication)->{
            resp.sendRedirect("/index");
        });
        //????????????????????????
        loginVerifyCodeFilter.setAuthenticationFailureHandler((request, response, exception) -> {
            response.sendRedirect("/login");
        });

        //????????????
        return loginVerifyCodeFilter;
    }

    @Bean
    public RememberMeServices rememberMeServices(){
        PersistentTokenBasedRememberMeServices rememberMeServices
                = new PersistentTokenBasedRememberMeServices
                (UUID.randomUUID().toString(),userDetailsService,persistentTokenRepository());
        return rememberMeServices;
    }


    /**
     * RememberMe ?????????????????????
     * @return
     */
    @Bean
    public PersistentTokenRepository persistentTokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource); //???????????????
        jdbcTokenRepository.setCreateTableOnStartup(false); //????????? ???????????? ???true
        return jdbcTokenRepository;
    }


    /**
     * ????????? ???????????????
     * auth.userDetailsService(userDetailsService); ?????????????????????????????????
     *
     * @param auth
     * @throws Exception
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService);
    }


    /**
     * ??????????????? ??????????????? ??????????????????
     *
     * @return ???????????? AuthenticationManager
     * @throws Exception
     */
    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


}
