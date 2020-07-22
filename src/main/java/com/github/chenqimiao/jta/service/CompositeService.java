package com.github.chenqimiao.jta.service;

import com.github.chenqimiao.jta.exception.TransferException;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * @Description: 组合服务
 * @Author: Qimiao Chen
 * @Create: 2020-07-22 14:19
 **/
public class CompositeService {


    // 银行账户服务
    private BankAccountService bankAccountService;
    // 银行审计服务
    private AuditService auditService;

    public CompositeService(BankAccountService bankAccountService, AuditService auditService) {
        this.bankAccountService = bankAccountService;
        this.auditService = auditService;
    }

    @Transactional(rollbackFor = TransferException.class)
    public void transferAndLog (String fromAccountId, String toAccountId, BigDecimal amount) {
        // 转账
        bankAccountService.transfer(fromAccountId, toAccountId, amount);

        // 记录日志
        auditService.log(fromAccountId, toAccountId, amount);

        // 查询 fromAccountId 的账户余额
        BigDecimal balance = bankAccountService.balanceOf(fromAccountId);

        // 抛出异常回滚转账和日志记录
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new TransferException("余额不足！");
        }
    }
}
