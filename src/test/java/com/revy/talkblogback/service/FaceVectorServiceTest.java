package com.revy.talkblogback.service;

import com.revy.talkblogback.mapper.FaceVectorMapper;
import com.revy.talkblogback.pojo.FaceVector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * FaceVectorService 单元测试。
 * 验证服务层是否正确调用 Mapper。
 */
class FaceVectorServiceTest {

    private FaceVectorMapper mapper;
    private FaceVectorService service;

    @BeforeEach
    void setUp() {
        mapper = mock(FaceVectorMapper.class);
        service = new FaceVectorService();
        // 反射注入 mapper，避免修改生产代码构造器。
        try {
            var field = FaceVectorService.class.getDeclaredField("faceVectorMapper");
            field.setAccessible(true);
            field.set(service, mapper);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * 保存向量应调用 Mapper 插入。
     */
    @Test
    void saveFaceVector_shouldCallMapper() {
        FaceVector fv = new FaceVector();
        service.saveFaceVector(fv);
        verify(mapper).insertFaceVector(fv);
    }

    /**
     * 按用户查询应透传 Mapper 结果。
     */
    @Test
    void getByUserId_shouldReturnMapperValue() {
        FaceVector fv = new FaceVector();
        fv.setUserId(1L);
        when(mapper.getFaceVectorByUserId(1L)).thenReturn(fv);

        FaceVector result = service.getFaceVectorByUserId(1L);

        assertEquals(1L, result.getUserId());
    }

    /**
     * 相似向量查询应透传 Mapper 结果。
     */
    @Test
    void findClosest_shouldReturnMapperValue() {
        FaceVector fv = new FaceVector();
        fv.setVectorId(9L);
        when(mapper.findClosestFaceVector(any(float[].class))).thenReturn(fv);

        FaceVector result = service.findClosestFaceVector(new float[] { 0.1f, 0.2f });

        assertEquals(9L, result.getVectorId());
    }
}
