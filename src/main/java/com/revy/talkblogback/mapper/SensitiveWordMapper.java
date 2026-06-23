package com.revy.talkblogback.mapper;

import com.revy.talkblogback.pojo.SensitiveWord;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SensitiveWordMapper {

    List<SensitiveWord> findAll();

    int insert(SensitiveWord sensitiveWord);

    int deleteById(Long id);
}
