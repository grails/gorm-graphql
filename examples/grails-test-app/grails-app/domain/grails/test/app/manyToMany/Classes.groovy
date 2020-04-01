package grails.test.app.manyToMany

import org.grails.gorm.graphql.entity.dsl.GraphQLMapping

class Classes {
    String title
    String description

    static hasMany = [students: Student]

    static graphQL = GraphQLMapping.build {
        operations.get.enabled false
        operations.list.enabled false
        operations.count.enabled false
        operations.create.enabled false
        operations.delete.enabled false
        operations.update.enabled false
    }
}
