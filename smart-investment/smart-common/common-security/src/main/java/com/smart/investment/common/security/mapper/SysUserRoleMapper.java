package com.smart.investment.common.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.common.security.entity.SysUserRole;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户角色关联 Mapper
 */
@Mapper
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {
}
