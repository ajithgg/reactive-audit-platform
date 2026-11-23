package io.auditplatform.userplatform

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableTransactionManagement
class ReactiveTransactionConfig {

    @Bean
    fun transactionalOperator(transactionManager: ReactiveTransactionManager): TransactionalOperator =
        TransactionalOperator.create(transactionManager)
}