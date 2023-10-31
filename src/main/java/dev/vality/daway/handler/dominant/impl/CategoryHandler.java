package dev.vality.daway.handler.dominant.impl;

import dev.vality.damsel.domain.CategoryObject;
import dev.vality.daway.dao.dominant.iface.DomainObjectDao;
import dev.vality.daway.dao.dominant.impl.CategoryDaoImpl;
import dev.vality.daway.domain.tables.pojos.Category;
import dev.vality.daway.handler.dominant.AbstractDominantHandler;
import org.springframework.stereotype.Component;

@Component
public class CategoryHandler extends AbstractDominantHandler<CategoryObject, Category, Integer> {

    private final CategoryDaoImpl categoryDao;

    public CategoryHandler(CategoryDaoImpl categoryDao) {
        this.categoryDao = categoryDao;
    }

    @Override
    protected DomainObjectDao<Category, Integer> getDomainObjectDao() {
        return categoryDao;
    }

    @Override
    protected CategoryObject getTargetObject() {
        return getDomainObject().getCategory();
    }

    @Override
    protected Integer getTargetObjectRefId() {
        return getTargetObject().getRef().getId();
    }

    @Override
    protected boolean acceptDomainObject() {
        return getDomainObject().isSetCategory();
    }

    @Override
    public Category convertToDatabaseObject(CategoryObject categoryObject, Long versionId, boolean current) {
        Category category = new Category();
        category.setVersionId(versionId);
        category.setCategoryRefId(getTargetObjectRefId());
        dev.vality.damsel.domain.Category data = categoryObject.getData();
        category.setName(data.getName());
        category.setDescription(data.getDescription());
        if (data.isSetType()) {
            category.setType(data.getType().name());
        }
        category.setCurrent(current);
        return category;
    }
}
