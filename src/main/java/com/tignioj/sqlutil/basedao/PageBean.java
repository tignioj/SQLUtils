package com.tignioj.sqlutil.basedao;

import java.util.List;
/**
 * 分页的优点：只查询一页
 * <p>
 * 2. 分页数据
 * 页码的数据都是由Servlet传递来的
 * Servlet:
 * 当前页: pageCode, pc;
 * > pc: 如果页面没有传递页面，那么Servlet默认是第一页，或者按页面传递的为准！
 * 总页数: totalPages, tp
 * > tp: 总记录数/每页记录数
 * 总记录数：totalRecord, tr
 * > tr: dao来获取，select count(*) from t_ccustomer;
 * 每页记录数：业务数据或叫系统数据！10行！
 * >
 * 当前页的数据：beanList
 * url
 * <p>
 * 3. 数据的传递
 * 这些分页数据总要在各层之间来回的传递！
 * 我们把这些分页数据封装到一个javabean中，它就叫分页Bean，例如PageBean
 */
public class PageBean<T> {
    private int currentPageNumber; //当前页码page code
    //    private int tp; //总页数total page;
    private int totalCount; //总记录数 total record
    private int itemPerPage; //每页记录数page size
    private List<T> currentPageList; //当前页的记录（数据库的每条记录）

    public int getCurrentPageNumber() {
        return currentPageNumber;
    }

    public void setCurrentPageNumber(int currentPageNumber) {
        this.currentPageNumber = currentPageNumber;
    }

    /**
     * 计算总页数
     * 公式：总记录/每页记录数
     * 如果101行总记录，每页显示10行，那么还多出1行要显示
     * 101/10  = 0;
     * 但是1行也是一页，所以一旦有余数就+1，否则就是0
     * totalCount % itemPerPage == 0 ? totalPage : totalPage + 1
     * @return
     */
    public int getTotalPage() {
        int tp = totalCount / itemPerPage;
        return totalCount % itemPerPage == 0 ? tp : tp + 1;
    }

    //该方法不允许外界设置，可以通过计算得到
//    public void setTp(int tp) {
//        this.tp = tp;
//    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getItemPerPage() {
        return itemPerPage;
    }

    public void setItemPerPage(int itemPerPage) {
        this.itemPerPage = itemPerPage;
    }

    public List<T> getCurrentPageList() {
        return currentPageList;
    }

    public void setCurrentPageList(List<T> currentPageList) {
        this.currentPageList = currentPageList;
    }
}

