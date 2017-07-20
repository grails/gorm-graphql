package org.grails.gorm.graphql.binding.manager

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.binding.GraphQLDataBinder
import org.springframework.beans.MutablePropertyValues
import org.springframework.validation.DataBinder

/**
 * A default implementation of {@link GraphQLDataBinderManager} that
 * will also return a result of the class requested is a subclass
 * of a class that exists in the registry
 *
 * @author James Kleeh
 * @since 1.0.0
 */
@CompileStatic
class DefaultGraphQLDataBinderManager implements GraphQLDataBinderManager {

    protected final Map<Class, GraphQLDataBinder> dataBinders = [:]

    /**
     * Registers a default data binder for the Object class
     */
    DefaultGraphQLDataBinderManager() {
        //Create the default data binder
        register(Object, new GraphQLDataBinder() {
            @Override
            void bind(Object object, Map data) {
                DataBinder dataBinder = new DataBinder(object)
                dataBinder.bind(new MutablePropertyValues(data))
            }
        })
    }

    /**
     * Registers a the data binder provided for the Object class
     */
    DefaultGraphQLDataBinderManager(GraphQLDataBinder defaultDataBinder) {
        register(Object, defaultDataBinder)
    }

    /**
     * @see GraphQLDataBinderManager#register
     */
    void register(Class clazz, GraphQLDataBinder dataBinder) {
        dataBinders.put(clazz, dataBinder)
    }

    /**
     * @see GraphQLDataBinderManager#getDataBinder
     */
    GraphQLDataBinder getDataBinder(Class clazz) {
        if (dataBinders.containsKey(clazz)) {
            return dataBinders.get(clazz)
        }
        List<Class> keys = dataBinders.keySet().toList()
        keys.reverse(true)
        for (Class key: keys) {
            if (key.isAssignableFrom(clazz)) {
                return dataBinders.get(key)
            }
        }
        null
    }
}
