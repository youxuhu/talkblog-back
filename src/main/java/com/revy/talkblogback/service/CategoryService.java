package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.CategoryMapper;
import com.revy.talkblogback.pojo.Category;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    public List<Category> getTree() {
        List<Category> all = categoryMapper.findAll();
        Map<Long, List<Category>> childrenByParentId = all.stream()
                .filter(c -> c.getParentId() != null)
                .collect(Collectors.groupingBy(Category::getParentId));

        List<Category> roots = all.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        for (Category root : roots) {
            attachChildren(root, childrenByParentId);
        }
        return roots;
    }

    public List<Category> getAll() {
        return categoryMapper.findAll();
    }

    public Category getById(Long id) {
        return categoryMapper.findById(id);
    }

    public Category create(Category category) {
        categoryMapper.insert(category);
        return category;
    }

    public Category update(Long id, Category category) {
        Category existing = categoryMapper.findById(id);
        if (existing == null) {
            throw new IllegalArgumentException("Category not found");
        }
        category.setId(id);
        categoryMapper.update(category);
        return categoryMapper.findById(id);
    }

    public boolean delete(Long id) {
        return categoryMapper.deleteById(id) > 0;
    }

    private void attachChildren(Category parent, Map<Long, List<Category>> childrenByParentId) {
        List<Category> children = childrenByParentId.get(parent.getId());
        if (children == null) return;
        parent.setChildren(children);
        for (Category child : children) {
            attachChildren(child, childrenByParentId);
        }
    }
}
