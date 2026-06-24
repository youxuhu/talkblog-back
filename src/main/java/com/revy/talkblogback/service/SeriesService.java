package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.SeriesMapper;
import com.revy.talkblogback.pojo.Blog;
import com.revy.talkblogback.pojo.Series;
import com.revy.talkblogback.pojo.response.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SeriesService {

    private final SeriesMapper seriesMapper;

    public SeriesService(SeriesMapper seriesMapper) {
        this.seriesMapper = seriesMapper;
    }

    public Series getById(Long id) {
        if (id == null) return null;
        return seriesMapper.findById(id);
    }

    public PageResult<Series> list(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        List<Series> list = seriesMapper.findList(offset, safeSize);
        long total = seriesMapper.count();
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public List<Series> getAll() {
        return seriesMapper.findAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public Series create(Series series) {
        if (series == null) throw new IllegalArgumentException("Series is required");
        if (!hasText(series.getName())) throw new IllegalArgumentException("Name is required");
        if (series.getAuthorId() == null) throw new IllegalArgumentException("Author is required");

        seriesMapper.insert(series);
        return series;
    }

    @Transactional(rollbackFor = Exception.class)
    public Series update(Long id, Series series) {
        if (id == null) throw new IllegalArgumentException("Series ID is required");
        Series existing = seriesMapper.findById(id);
        if (existing == null) throw new IllegalArgumentException("Series not found");

        if (hasText(series.getName())) existing.setName(series.getName());
        if (series.getDescription() != null) existing.setDescription(series.getDescription());

        seriesMapper.update(existing);
        return seriesMapper.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public boolean delete(Long id) {
        if (id == null) return false;
        return seriesMapper.deleteById(id) > 0;
    }

    public PageResult<Series> getUserSeries(Long authorId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        List<Series> list = seriesMapper.findByAuthorId(authorId, offset, safeSize);
        long total = seriesMapper.countByAuthorId(authorId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    public long countByAuthorId(Long authorId) {
        return seriesMapper.countByAuthorId(authorId);
    }

    public PageResult<Blog> getBlogs(Long seriesId, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;
        List<Blog> list = seriesMapper.findBlogsBySeriesId(seriesId, offset, safeSize);
        long total = seriesMapper.countBlogsBySeriesId(seriesId);
        return new PageResult<>(list, total, safePage, safeSize);
    }

    private boolean hasText(String str) {
        return str != null && !str.trim().isEmpty();
    }
}
