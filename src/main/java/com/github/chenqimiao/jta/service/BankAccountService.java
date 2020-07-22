package com.github.chenqimiao.jta.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.math.BigDecimal;

/**
 * @Description: 银行账户服务
 * @Author: Qimiao Chen
 * @Create: 2020-07-22 14:18
 **/
public class BankAccountService {


    private JdbcTemplate jdbcTemplate;

    public BankAccountService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void transfer(String fromAccountId, String toAccountId,
                         BigDecimal amount) {
        jdbcTemplate.update("update ACCOUNT set BALANCE=BALANCE-? where ID=?",
                amount, fromAccountId);
        jdbcTemplate.update("update ACCOUNT set BALANCE=BALANCE+? where ID=?",
                amount, toAccountId);
    }

    public BigDecimal balanceOf(String accountId) {
        return jdbcTemplate.query("select BALANCE from ACCOUNT where ID=?",
                new Object[]{accountId},
                (ResultSetExtractor<BigDecimal>) (rs) -> {
                    rs.next();
                    return new BigDecimal(rs.getDouble(1));
                });
    }
}
