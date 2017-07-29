package grails.test.app

import org.grails.gorm.graphql.entity.dsl.GraphQLMapping

class Tag {

    String name

    static constraints = {
    }

    Set<Post> getPosts() {
        Post.executeQuery("select p from Post p join fetch p.tags t where t.id = ${id}")
    }

    static graphql = GraphQLMapping.build {
        add('posts', [Post]) {
            input false
        }
    }
}
