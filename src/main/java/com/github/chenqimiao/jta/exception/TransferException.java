package com.github.chenqimiao.jta.exception;

/**
 * @Description: 转账异常
 * @Author: Qimiao Chen
 * @Create: 2020-07-22 14:30
 **/
public class TransferException extends RuntimeException {
    public TransferException(String message) {
        super(message);
    }
}
