package org.grails.gorm.graphql.entity;

import graphql.language.Field;
import graphql.language.Selection;
import graphql.language.SelectionSet;
import graphql.schema.DataFetchingEnvironment;
import groovy.transform.CompileStatic;
import org.codehaus.groovy.runtime.StringGroovyMethods;
import org.grails.datastore.mapping.model.PersistentEntity;
import org.grails.datastore.mapping.model.types.Association;
import org.grails.datastore.mapping.model.types.ToMany;
import org.grails.datastore.mapping.model.types.ToOne;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Helper class to determine which properties should be eagerly
 * fetched based on the fields in a {@link DataFetchingEnvironment}.
 *
 * @author James Kleeh
 * @since 1.0.0
 */
public class EntityFetchOptions {

    private Map<String, Association> associations = new LinkedHashMap<>();
    protected PersistentEntity entity;
    protected Set<String> associationNames;
    protected String propertyName;

    public EntityFetchOptions(PersistentEntity entity) {
        this(entity, null);
    }

    public EntityFetchOptions(PersistentEntity entity, String projectionName) {
        if (entity == null) {
            throw new IllegalArgumentException("Cannot retrieve fetch options for a null entity. Is GORM initialized?");
        }

        this.entity = entity;
        this.propertyName = projectionName;
        for (Association association : entity.getAssociations()) {
            associations.put(association.getName(), association);
        }

        associationNames = associations.keySet();
    }

    public Map<String, Association> getAssociations() {
        return associations;
    }

    protected boolean isForeignKeyInChild(Association association) {
        return association instanceof ToOne && ((ToOne) association).isForeignKeyInChild() || association instanceof ToMany;
    }

    @SuppressWarnings({"NestedBlockDepth"})
    protected void handleField(String parentName, Field selectedField, Set<String> joinProperties) {
        String resolvedName;

        if (parentName != null) {
            resolvedName = parentName + "." + selectedField.getName();
        } else {
            resolvedName = selectedField.getName();
        }

        Association association = associations.get(selectedField.getName());
        PersistentEntity entity = association.getAssociatedEntity();

        if (entity == null) {
            joinProperties.add(resolvedName);
            return;
        }

        final SelectionSet set = selectedField.getSelectionSet();
        List<Selection> selections = (set == null ? new ArrayList<>() : set.getSelections());

        if (!association.isEmbedded()) {
            if (isForeignKeyInChild(association)) {
                joinProperties.add(resolvedName);
            }
            else if (selections.size() == 1 && selections.get(0) instanceof Field) {
                Field field = (Field) selections.get(0);
                if (!entity.isIdentityName(field.getName())) {
                    joinProperties.add(resolvedName);
                }
            }
            else {
                joinProperties.add(resolvedName);
            }
        }

        List<Field> fields = new ArrayList<Field>();

        selections.parallelStream()
                .filter(Field.class::isInstance)
                .map(Field.class::cast)
                .forEach((Field field) -> {
                    if (field.getName().equals(association.getReferencedPropertyName())) {
                        if (field.getSelectionSet() != null) {

                            List<Field> nestedFields = field
                                    .getSelectionSet()
                                    .getSelections()
                                    .parallelStream()
                                    .filter(Field.class::isInstance)
                                    .map(Field.class::cast)
                                    .collect(Collectors.toList());

                                    joinProperties.addAll(getJoinProperties(nestedFields));
                        }
                    }
                    else {
                        fields.add(field);
                    }
                });

        joinProperties.addAll(new EntityFetchOptions(entity, resolvedName).getJoinProperties(fields));
    }

    public Set<String> getJoinProperties(List<Field> fields) {
        Set<String> joinProperties = new HashSet<>();

        if (fields != null) {
            fields.parallelStream()
                    .filter(field -> associationNames.contains(field.getName()))
                    .forEach(field -> handleField(propertyName, field, joinProperties));
        }

        return joinProperties;
    }

    public Set<String> getJoinProperties(DataFetchingEnvironment environment) {
        List<Field> fields = new ArrayList<>();
        List<Field> environmentFields = environment.getFields();

        if (environmentFields != null) {
            fields = environmentFields.parallelStream()
                    .filter(field -> field.getSelectionSet() != null)
                    .flatMap(field -> field.getSelectionSet().getSelections().parallelStream())
                    .filter(Field.class::isInstance)
                    .map(Field.class::cast)
                    .collect(Collectors.toList());
        }

        return getJoinProperties(fields);
    }

}
