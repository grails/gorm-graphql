package org.grails.gorm.graphql

import groovy.transform.CompileStatic
import org.grails.datastore.mapping.config.Settings
import org.grails.datastore.mapping.core.DatastoreUtils
import org.grails.orm.hibernate.HibernateDatastore
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

@CompileStatic
class HibernateSpec extends Specification {

    @Shared @AutoCleanup HibernateDatastore hibernateDatastore
    @Shared HibernateMappingContext mappingContext

    void setupSpec() {
        hibernateDatastore = new HibernateDatastore(
                DatastoreUtils.createPropertyResolver(configuration),
                domainClasses as Class[])
        mappingContext = hibernateDatastore.mappingContext
    }

    /**
     * @return The configuration
     */
    Map getConfiguration() {
        Collections.singletonMap(Settings.SETTING_DB_CREATE, 'create-drop')
    }

    /**
     * @return The domain classes
     */
    List<Class> getDomainClasses() { [] }
}
