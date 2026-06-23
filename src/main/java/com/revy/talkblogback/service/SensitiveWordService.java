package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.SensitiveWordMapper;
import com.revy.talkblogback.pojo.SensitiveWord;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SensitiveWordService {

    private static final Logger log = LoggerFactory.getLogger(SensitiveWordService.class);

    private final SensitiveWordMapper sensitiveWordMapper;
    private final Map<String, String> wordMap = new ConcurrentHashMap<>();
    private Pattern combinedPattern = Pattern.compile("");

    public SensitiveWordService(SensitiveWordMapper sensitiveWordMapper) {
        this.sensitiveWordMapper = sensitiveWordMapper;
    }

    @PostConstruct
    public void init() {
        refresh();
    }

    public void refresh() {
        wordMap.clear();
        List<SensitiveWord> words = sensitiveWordMapper.findAll();
        for (SensitiveWord sw : words) {
            wordMap.put(sw.getWord().toLowerCase(), sw.getReplacement());
        }
        if (!wordMap.isEmpty()) {
            String pattern = wordMap.keySet().stream()
                    .map(Pattern::quote)
                    .collect(Collectors.joining("|"));
            combinedPattern = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
        } else {
            combinedPattern = Pattern.compile("");
        }
        log.info("Loaded {} sensitive words", wordMap.size());
    }

    public List<SensitiveWord> findAll() {
        return sensitiveWordMapper.findAll();
    }

    public void add(String word, String replacement) {
        if (replacement == null || replacement.isBlank()) {
            replacement = "***";
        }
        SensitiveWord sw = new SensitiveWord();
        sw.setWord(word);
        sw.setReplacement(replacement);
        try {
            sensitiveWordMapper.insert(sw);
        } catch (org.springframework.dao.DuplicateKeyException e) {
            throw new IllegalArgumentException("敏感词已存在");
        }
        refresh();
    }

    public void deleteById(Long id) {
        int affected = sensitiveWordMapper.deleteById(id);
        if (affected == 0) {
            throw new IllegalArgumentException("敏感词不存在");
        }
        refresh();
    }

    /**
     * 过滤敏感词，替换为 ***
     */
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return combinedPattern.matcher(text).replaceAll(match -> {
            String word = match.group().toLowerCase();
            return wordMap.getOrDefault(word, "***");
        });
    }

    /**
     * 检查是否包含敏感词
     */
    public boolean containsSensitiveWord(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return combinedPattern.matcher(text).find();
    }
}
