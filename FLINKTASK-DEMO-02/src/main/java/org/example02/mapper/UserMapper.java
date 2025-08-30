package org.example02.mapper;

import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import org.example02.entity.User;

import java.util.List;

/**
 * 用户Mapper接口
 * 继承MyBatis-Flex的BaseMapper，提供基础的CRUD操作
 */
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    default User selectByUsername(String username) {
        return selectOneByQuery(
            QueryWrapper.create()
                .where(User::getUsername).eq(username)
                .and(User::getDeleted).eq(0)
        );
    }
    
    /**
     * 根据邮箱查询用户
     * @param email 邮箱
     * @return 用户信息
     */
    default User selectByEmail(String email) {
        return selectOneByQuery(
            QueryWrapper.create()
                .where(User::getEmail).eq(email)
                .and(User::getDeleted).eq(0)
        );
    }
    
    /**
     * 查询所有正常状态的用户
     * @return 用户列表
     */
    default List<User> selectActiveUsers() {
        return selectListByQuery(
            QueryWrapper.create()
                .where(User::getStatus).eq(1)
                .and(User::getDeleted).eq(0)
                .orderBy(User::getCreateTime, false)
        );
    }
    
    /**
     * 分页查询用户
     * @param page 页码
     * @param size 每页大小
     * @param status 用户状态（可选）
     * @return 分页结果
     */
    default Page<User> selectUsersByPage(int page, int size, Integer status) {
        QueryWrapper queryWrapper = QueryWrapper.create()
                .where(User::getDeleted).eq(0)
                .orderBy(User::getCreateTime, false);
        
        if (status != null) {
            queryWrapper.and(User::getStatus).eq(status);
        }
        
        return paginate(Page.of(page, size), queryWrapper);
    }
    
    /**
     * 根据年龄范围查询用户
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    default List<User> selectUsersByAgeRange(int minAge, int maxAge) {
        return selectListByQuery(
            QueryWrapper.create()
                .where(User::getAge).ge(minAge)
                .and(User::getAge).le(maxAge)
                .and(User::getDeleted).eq(0)
                .orderBy(User::getAge, true)
        );
    }
    
    /**
     * 统计正常状态的用户数量
     * @return 用户数量
     */
    default long countActiveUsers() {
        return selectCountByQuery(
            QueryWrapper.create()
                .where(User::getStatus).eq(1)
                .and(User::getDeleted).eq(0)
        );
    }
    
    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新数量
     */
    default int updateStatusBatch(List<Long> userIds, int status) {
        return updateByQuery(
            User.builder().status(status).build(),
            QueryWrapper.create()
                .where(User::getId).in(userIds)
                .and(User::getDeleted).eq(0)
        );
    }
}