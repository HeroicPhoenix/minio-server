package com.lvwyh.minio.server.common.util;

import java.util.List;

/**
 * 通用分页结果。
 *
 * 字段名称直接面向前端分页组件：total 表示总数，pageNum 表示当前页，
 * pageSize 表示每页数量，list 表示当前页数据。
 */
public class PageResult<T> {

    /** 总记录数。 */
    private long total;
    /** 当前页码，从 1 开始。 */
    private int pageNum;
    /** 每页记录数。 */
    private int pageSize;
    /** 当前页记录列表。 */
    private List<T> list;

    public PageResult(long total, int pageNum, int pageSize, List<T> list) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.list = list;
    }

    public long getTotal() {
        return total;
    }

    public int getPageNum() {
        return pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public List<T> getList() {
        return list;
    }
}
