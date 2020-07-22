package com.github.chenqimiao.jta;

import bitronix.tm.BitronixTransactionManager;
import com.github.chenqimiao.jta.service.AuditService;
import com.github.chenqimiao.jta.service.BankAccountService;
import com.github.chenqimiao.jta.service.CompositeService;
import org.hsqldb.jdbc.pool.JDBCXADataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.jta.bitronix.BitronixXADataSourceWrapper;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @Description: Java Transaction Demo (XA)
 * @Author: Qimiao Chen
 * @Create: 2020-07-21 20:20
 **/
@Configuration
@EnableTransactionManagement
@EnableAutoConfiguration
public class JtaDemo {

    public static void main(String[] args) {
        // 注册 JtaDemo，并启动应用上下文
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(JtaDemo.class);

        // 拿到组合服务
        CompositeService compositeService = context.getBean(CompositeService.class);

        // 银行账户服务
        BankAccountService bankAccountService = context.getBean(BankAccountService.class);

        String fromAccountId = "a0000001";

        String toAccountId = "a0000002";

        BigDecimal amount = new BigDecimal(10000);

        System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>转账前 账户[%s]的余额为 %s, 账户[%s]的余额为 %s \n", fromAccountId, bankAccountService.balanceOf(fromAccountId).toPlainString(),
                toAccountId, bankAccountService.balanceOf(toAccountId).toPlainString());

        try {
            // 转账并记录日志
            compositeService.transferAndLog(fromAccountId, toAccountId, amount);

        }catch (Exception e) {
            System.out.println(">>>>>>>>>>>>>>>>>>>>>转账失败原因: " + e.getMessage());
        }

        System.out.printf(">>>>>>>>>>>>>>>>>>>>>>>>转账后 账户[%s]的余额为 %s, 账户[%s]的余额为 %s \n", fromAccountId, bankAccountService.balanceOf(fromAccountId).toPlainString(),
                toAccountId, bankAccountService.balanceOf(toAccountId).toPlainString());

        // 关闭应用上下文
        context.close();
    }

    // 银行账户数据源
    @Bean("dataSourceAccount")
    public DataSource dataSource() throws Exception {
        return createHsqlXADatasource("jdbc:hsqldb:mem:accountDb");
    }

    // 银行审计数据源
    @Bean("dataSourceAudit")
    public DataSource dataSourceAudit() throws Exception {
        return createHsqlXADatasource("jdbc:hsqldb:mem:auditDb");
    }

    // 银行账户服务
    @Bean
    public AuditService auditService(@Qualifier("jdbcTemplateAudit") JdbcTemplate jdbcTemplate) {
        return new AuditService(jdbcTemplate);
    }

    // 银行审计服务
    @Bean
    public BankAccountService bankAccountService(@Qualifier("jdbcTemplateAccount") JdbcTemplate jdbcTemplate) {
        return new BankAccountService(jdbcTemplate);
    }

    @Bean
    public CompositeService compositeService(BankAccountService bankAccountService, AuditService auditService) {
        return new CompositeService(bankAccountService, auditService);
    }

    @Bean("jdbcTemplateAccount")
    public JdbcTemplate jdbcTemplate(
            @Qualifier("dataSourceAccount") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean("jdbcTemplateAudit")
    public JdbcTemplate jdbcTemplateAudit(
            @Qualifier("dataSourceAudit") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }


    // 初始化银行账户数据库
    @Autowired
    public void runAccountDbInit(@Qualifier("dataSourceAccount") DataSource dataSource) throws SQLException {
        runScript("account.sql", dataSource);
    }

    // 初始化审计数据库
    @Autowired
    public void runAuditDbInit( @Qualifier("dataSourceAudit") DataSource dataSource) throws SQLException {
        runScript("audit.sql", dataSource);
    }

    private DataSource createHsqlXADatasource(String connectionUrl)
            throws Exception {
        JDBCXADataSource dataSource = new JDBCXADataSource();
        dataSource.setUrl(connectionUrl);
        dataSource.setUser("sa");
        BitronixXADataSourceWrapper wrapper = new BitronixXADataSourceWrapper();
        return wrapper.wrapDataSource(dataSource);
    }

    // 运行数据库脚本
    private void runScript(String scriptName, DataSource dataSource)
            throws SQLException {
        DefaultResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource script = resourceLoader.getResource(scriptName);
        try (Connection con = dataSource.getConnection()) {
            ScriptUtils.executeSqlScript(con, script);
        }
    }

}
