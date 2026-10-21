package com.srs.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.srs.modules.system.entity.SysRoleMenu;
import com.srs.modules.system.entity.SysUserCommunity;
import com.srs.modules.system.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * RBAC 关联关系查询
 */
@Mapper
public interface SysRbacMapper extends BaseMapper<SysUserRole> {

    @Select("""
            SELECT m.perms FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_user_role ur ON ur.role_id = rm.role_id
            WHERE ur.user_id = #{userId} AND m.perms IS NOT NULL AND m.perms <> '' AND m.status = 1
            """)
    List<String> selectPermsByUserId(@Param("userId") Long userId);

    @Select("""
            SELECT DISTINCT m.* FROM sys_menu m
            JOIN sys_role_menu rm ON rm.menu_id = m.id
            JOIN sys_user_role ur ON ur.role_id = rm.role_id
            WHERE ur.user_id = #{userId} AND m.menu_type IN ('M','C') AND m.status = 1 AND m.visible = 1
            ORDER BY m.order_num
            """)
    List<com.srs.modules.system.entity.SysMenu> selectMenusByUserId(@Param("userId") Long userId);

    @Select("SELECT r.role_key FROM sys_role r JOIN sys_user_role ur ON ur.role_id = r.id WHERE ur.user_id = #{userId}")
    List<String> selectRoleKeysByUserId(@Param("userId") Long userId);

    @Select("SELECT role_id FROM sys_user_role WHERE user_id = #{userId}")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    @Select("SELECT community_id FROM sys_user_community WHERE user_id = #{userId}")
    List<Long> selectCommunityIdsByUserId(@Param("userId") Long userId);
}
