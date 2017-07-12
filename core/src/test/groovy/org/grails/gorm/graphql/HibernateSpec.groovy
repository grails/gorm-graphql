package org.grails.gorm.graphql

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.interceptor.DefaultTransactionAttribute
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@CompileStatic
abstract class HibernateSpec extends Specification {

    @Shared @AutoCleanup HibernateDatastore hibernateDatastore
    @Shared PlatformTransactionManager transactionManager

    void setupSpec() {
        List<Class> domainClasses = getDomainClasses()

        hibernateDatastore = new HibernateDatastore(
                DatastoreUtils.createPropertyResolver(getConfiguration()),
                domainClasses as Class[])

        transactionManager = hibernateDatastore.getTransactionManager()
    }

    /**
     * The transaction status
     */
    TransactionStatus transactionStatus

    void setup() {
        transactionStatus = transactionManager.getTransaction(new DefaultTransactionAttribute())
    }

    void cleanup() {
        transactionManager.rollback(transactionStatus)
    }

    /**
     * @return The configuration
     */
    Map getConfiguration() {
        Collections.singletonMap(Settings.SETTING_DB_CREATE, "create-drop")
    }

    /**
     * @return The domain classes
     */
    List<Class> getDomainClasses() { [] }
}
