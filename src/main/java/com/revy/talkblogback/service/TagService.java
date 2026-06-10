package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.TagMapper;
import com.revy.talkblogback.pojo.Tag;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TagService {

    private final TagMapper tagMapper;

    public TagService(TagMapper tagMapper) {
        this.tagMapper = tagMapper;
    }

    public List<Tag> getAll() {
        return tagMapper.findAll();
    }

    public List<Tag> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return tagMapper.findAll();
        }
        return tagMapper.searchByName(keyword.trim());
    }

    public Tag getById(Long id) {
        return tagMapper.findById(id);
    }

    public Tag getOrCreate(String name) {
        Tag existing = tagMapper.findByName(name);
        if (existing != null) {
            return existing;
        }
        Tag tag = new Tag();
        tag.setName(name);
        tag.setSlug(name.toLowerCase().replaceAll("\\s+", "-"));
        tagMapper.insert(tag);
        return tag;
    }

    public Tag create(Tag tag) {
        tagMapper.insert(tag);
        return tag;
    }

    public boolean delete(Long id) {
        return tagMapper.deleteById(id) > 0;
    }
}
