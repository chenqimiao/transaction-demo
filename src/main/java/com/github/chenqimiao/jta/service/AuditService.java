package com.github.chenqimiao.jta.service;

import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;

/**
 * @Description: 审计服务
 * @Author: Qimiao Chen
 * @Create: 2020-07-21 20:44
 **/
public class AuditService {

    private JdbcTemplate jdbcTemplate;

    public AuditService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public void log(String fromAccount, String toAccount, BigDecimal amount) {
        jdbcTemplate.update(
                "insert into AUDIT_LOG(FROM_ACCOUNT, TO_ACCOUNT, AMOUNT) values ?,?,?",
                fromAccount, toAccount, amount);
    }




}
