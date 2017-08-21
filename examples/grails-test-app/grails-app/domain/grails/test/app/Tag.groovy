package grails.test.app

import graphql.schema.DataFetchingEnvironment
import org.grails.gorm.graphql.entity.EntityFetchOptions
import org.grails.gorm.graphql.entity.dsl.GraphQLMapping
import org.grails.gorm.graphql.fetcher.impl.ClosureDataFetchingEnvironment

class Tag {

    String name

    static constraints = {
    }

    Set<Post> getPosts(Map queryArgs) {
        Long tagId = this.id
        Post.where { tags { id == tagId } }.list(queryArgs)
    }

    static graphql = GraphQLMapping.build {
        add('posts', [Post]) {
            input false
            dataFetcher { Tag tag, ClosureDataFetchingEnvironment env ->
                tag.getPosts(env.fetchArguments)
            }
        }
    }
}
