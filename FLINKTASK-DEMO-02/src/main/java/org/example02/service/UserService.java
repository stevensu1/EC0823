package org.example02.service;

import com.mybatisflex.core.paginate.Page;
import lombok.extern.slf4j.Slf4j;
import org.example02.config.DataSourceConfig;
import org.example02.entity.User;
import org.example02.mapper.UserMapper;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户服务类
 * 提供用户相关的业务操作
 */
@Slf4j
public class UserService {
    
    private final UserMapper userMapper;
    
    public UserService() {
        // 获取MyBatis-Flex的Mapper实例
        this.userMapper = DataSourceConfig.getBootstrap().getMapper(UserMapper.class);
    }
    
    /**
     * 创建用户
     * @param user 用户信息
     * @return 创建的用户ID
     */
    public Long createUser(User user) {
        try {
            // 设置创建时间和更新时间
            LocalDateTime now = LocalDateTime.now();
            user.setCreateTime(now);
            user.setUpdateTime(now);
            user.setDeleted(0);
            
            if (user.getStatus() == null) {
                user.setStatus(1); // 默认为正常状态
            }
            
            // 检查用户名是否已存在
            if (userMapper.selectByUsername(user.getUsername()) != null) {
                throw new RuntimeException("用户名已存在: " + user.getUsername());
            }
            
            // 检查邮箱是否已存在
            if (user.getEmail() != null && userMapper.selectByEmail(user.getEmail()) != null) {
                throw new RuntimeException("邮箱已存在: " + user.getEmail());
            }
            
            // 插入用户
            userMapper.insert(user);
            log.info("用户创建成功，ID: {}, 用户名: {}", user.getId(), user.getUsername());
            return user.getId();
        } catch (Exception e) {
            log.error("创建用户失败", e);
            throw new RuntimeException("创建用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户信息
     */
    public User getUserById(Long id) {
        try {
            User user = userMapper.selectOneById(id);
            if (user != null && user.getDeleted() == 0) {
                return user;
            }
            return null;
        } catch (Exception e) {
            log.error("查询用户失败，ID: {}", id, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户信息
     */
    public User getUserByUsername(String username) {
        try {
            return userMapper.selectByUsername(username);
        } catch (Exception e) {
            log.error("根据用户名查询用户失败，用户名: {}", username, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 获取所有正常状态的用户
     * @return 用户列表
     */
    public List<User> getAllActiveUsers() {
        try {
            return userMapper.selectActiveUsers();
        } catch (Exception e) {
            log.error("查询正常用户列表失败", e);
            throw new RuntimeException("查询用户列表失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 分页查询用户
     * @param page 页码
     * @param size 每页大小
     * @param status 用户状态（可选）
     * @return 分页结果
     */
    public Page<User> getUsersByPage(int page, int size, Integer status) {
        try {
            return userMapper.selectUsersByPage(page, size, status);
        } catch (Exception e) {
            log.error("分页查询用户失败，页码: {}, 每页大小: {}", page, size, e);
            throw new RuntimeException("分页查询用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 更新用户信息
     * @param user 用户信息
     * @return 是否更新成功
     */
    public boolean updateUser(User user) {
        try {
            if (user.getId() == null) {
                throw new IllegalArgumentException("用户ID不能为空");
            }
            
            // 设置更新时间
            user.setUpdateTime(LocalDateTime.now());
            
            int result = userMapper.update(user);
            boolean success = result > 0;
            if (success) {
                log.info("用户更新成功，ID: {}", user.getId());
            } else {
                log.warn("用户更新失败，可能用户不存在，ID: {}", user.getId());
            }
            return success;
        } catch (Exception e) {
            log.error("更新用户失败，ID: {}", user.getId(), e);
            throw new RuntimeException("更新用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 软删除用户
     * @param id 用户ID
     * @return 是否删除成功
     */
    public boolean deleteUser(Long id) {
        try {
            User user = User.builder()
                    .id(id)
                    .deleted(1)
                    .updateTime(LocalDateTime.now())
                    .build();
            
            int result = userMapper.update(user);
            boolean success = result > 0;
            if (success) {
                log.info("用户删除成功，ID: {}", id);
            } else {
                log.warn("用户删除失败，可能用户不存在，ID: {}", id);
            }
            return success;
        } catch (Exception e) {
            log.error("删除用户失败，ID: {}", id, e);
            throw new RuntimeException("删除用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 根据年龄范围查询用户
     * @param minAge 最小年龄
     * @param maxAge 最大年龄
     * @return 用户列表
     */
    public List<User> getUsersByAgeRange(int minAge, int maxAge) {
        try {
            return userMapper.selectUsersByAgeRange(minAge, maxAge);
        } catch (Exception e) {
            log.error("根据年龄范围查询用户失败，最小年龄: {}, 最大年龄: {}", minAge, maxAge, e);
            throw new RuntimeException("查询用户失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 统计正常状态的用户数量
     * @return 用户数量
     */
    public long countActiveUsers() {
        try {
            return userMapper.countActiveUsers();
        } catch (Exception e) {
            log.error("统计用户数量失败", e);
            throw new RuntimeException("统计用户数量失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 批量更新用户状态
     * @param userIds 用户ID列表
     * @param status 新状态
     * @return 更新数量
     */
    public int updateUserStatusBatch(List<Long> userIds, int status) {
        try {
            int result = userMapper.updateStatusBatch(userIds, status);
            log.info("批量更新用户状态成功，更新数量: {}", result);
            return result;
        } catch (Exception e) {
            log.error("批量更新用户状态失败", e);
            throw new RuntimeException("批量更新用户状态失败: " + e.getMessage(), e);
        }
    }
}