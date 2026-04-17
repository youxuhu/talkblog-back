package com.revy.talkblogback.handler;

import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PgVectorTypeHandler 单元测试。
 * 验证 pgvector 与 float[] 的双向转换。
 */
class PgVectorTypeHandlerTest {

    private final PgVectorTypeHandler handler = new PgVectorTypeHandler();

    /**
     * 写入参数时应封装为 PGobject(vector)。
     */
    @Test
    void setNonNullParameter_shouldSetPgObject() throws SQLException {
        PreparedStatement ps = mock(PreparedStatement.class);

        handler.setNonNullParameter(ps, 1, new float[] { 0.1f, 0.2f, 0.3f }, null);

        verify(ps).setObject(any(Integer.class), any(PGobject.class));
    }

    /**
     * 应支持中括号格式解析。
     */
    @Test
    void getNullableResult_shouldParseBracketFormat() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString("face_vector")).thenReturn("[0.1,0.2,0.3]");

        float[] vector = handler.getNullableResult(rs, "face_vector");

        assertArrayEquals(new float[] { 0.1f, 0.2f, 0.3f }, vector);
    }

    /**
     * 应支持大括号格式解析。
     */
    @Test
    void getNullableResult_shouldParseBraceFormat() throws SQLException {
        ResultSet rs = mock(ResultSet.class);
        when(rs.getString(1)).thenReturn("{1,2,3}");

        float[] vector = handler.getNullableResult(rs, 1);

        assertArrayEquals(new float[] { 1f, 2f, 3f }, vector);
    }

    /**
     * 空字符串应返回 null。
     */
    @Test
    void getNullableResult_shouldReturnNull_whenEmpty() throws SQLException {
        CallableStatement cs = mock(CallableStatement.class);
        when(cs.getString(1)).thenReturn("");

        float[] vector = handler.getNullableResult(cs, 1);

        assertNull(vector);
    }
}
