package org.grails.gorm.graphql.entity.dsl.helpers

import groovy.transform.CompileStatic
import org.grails.gorm.graphql.entity.arguments.ComplexArgument
import org.grails.gorm.graphql.entity.arguments.CustomArgument
import org.grails.gorm.graphql.entity.arguments.SimpleArgument

/**
 * Decorates a class with argument builders.
 *
 * @param <T> The implementing class
 * @author James Kleeh
 * @since 2.0.2
 */
@CompileStatic
trait Argued<T> extends Arguable {

    private void handleArgumentClosure(CustomArgument argument, Closure closure) {
        withDelegate(closure, (Object)argument)
        argument.validate()
        arguments.add(argument)
    }

    /**
     * Creates an argument to the operation that is a list of a simple type.
     * The list can not have more than 1 element and that element must be a class.
     *
     * @param name The name of the argument
     * @param type The returnType of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    T argument(String name, List<Class<?>> type, @DelegatesTo(value = SimpleArgument, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        CustomArgument argument = new SimpleArgument().name(name).returns(type)
        handleArgumentClosure(argument, closure)
        (T)this
    }

    /**
     * Creates an argument to the operation that is of the returnType provided.
     *
     * @param name The name of the argument
     * @param type The returnType of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    T argument(String name, Class<?> type, @DelegatesTo(value = SimpleArgument, strategy = Closure.DELEGATE_ONLY) Closure closure = null) {
        CustomArgument argument = new SimpleArgument().name(name).returns(type)
        handleArgumentClosure(argument, closure)
        (T)this
    }

    /**
     * Creates an argument to the operation that is a custom type.
     *
     * @param name The name of the argument
     * @param closure To provide additional data about the argument
     * @return The operation in order to chain method calls
     */
    T argument(String name, String typeName, @DelegatesTo(value = ComplexArgument, strategy = Closure.DELEGATE_ONLY) Closure closure) {
        CustomArgument argument = new ComplexArgument().name(name).typeName(typeName)
        handleArgumentClosure(argument, closure)
        (T)this
    }

}