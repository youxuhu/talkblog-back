package com.revy.talkblogback.handler;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.postgresql.util.PGobject;

import java.sql.*;

/**
 * 自定义 MyBatis TypeHandler，用于将 PostgreSQL 的 VECTOR 类型与 Java 的 float[] 类型进行映射。
 */
public class PgVectorTypeHandler extends BaseTypeHandler<float[]> {

    /**
     * 设置非空参数，将 Java 的 float[] 转换为 PostgreSQL 的 VECTOR 类型。
     *
     * @param ps        PreparedStatement 对象
     * @param i         参数索引
     * @param parameter Java 的 float[] 参数
     * @param jdbcType  JDBC 类型
     * @throws SQLException SQL 异常
     */
    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, float[] parameter, JdbcType jdbcType)
            throws SQLException {
        PGobject pgObject = new PGobject();
        pgObject.setType("vector");
        pgObject.setValue(toPgVectorLiteral(parameter));
        ps.setObject(i, pgObject);
    }

    /**
     * 从结果集中获取列值，将 PostgreSQL 的 VECTOR 类型转换为 Java 的 float[]。
     *
     * @param rs         结果集对象
     * @param columnName 列名
     * @return Java 的 float[]
     * @throws SQLException SQL 异常
     */
    @Override
    public float[] getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseVector(rs.getString(columnName));
    }

    /**
     * 从结果集中获取列值，将 PostgreSQL 的 VECTOR 类型转换为 Java 的 float[]。
     *
     * @param rs          结果集对象
     * @param columnIndex 列索引
     * @return Java 的 float[]
     * @throws SQLException SQL 异常
     */
    @Override
    public float[] getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseVector(rs.getString(columnIndex));
    }

    /**
     * 从 CallableStatement 中获取列值，将 PostgreSQL 的 VECTOR 类型转换为 Java 的 float[]。
     *
     * @param cs          CallableStatement 对象
     * @param columnIndex 列索引
     * @return Java 的 float[]
     * @throws SQLException SQL 异常
     */
    @Override
    public float[] getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseVector(cs.getString(columnIndex));
    }

    /**
     * 将 PostgreSQL 的 VECTOR 类型字符串解析为 Java 的 float[]。
     *
     * @param vectorString PostgreSQL VECTOR 类型的字符串
     * @return Java 的 float[]
     */
    private float[] parseVector(String vectorString) {
        if (vectorString == null || vectorString.isEmpty()) {
            return null;
        }
        String normalized = vectorString.trim()
                .replace("[", "")
                .replace("]", "")
                .replace("{", "")
                .replace("}", "");
        if (normalized.isEmpty()) {
            return new float[0];
        }
        String[] parts = normalized.split(",");
        float[] vector = new float[parts.length];
        for (int i = 0; i < parts.length; i++) {
            vector[i] = Float.parseFloat(parts[i].trim());
        }
        return vector;
    }

    /**
     * 将 float[] 序列化为 pgvector 的文本格式，例如 [0.1,0.2]。
     */
    private String toPgVectorLiteral(float[] values) {
        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int i = 0; i < values.length; i++) {
            if (i > 0) {
                builder.append(',');
            }
            builder.append(values[i]);
        }
        builder.append(']');
        return builder.toString();
    }
}