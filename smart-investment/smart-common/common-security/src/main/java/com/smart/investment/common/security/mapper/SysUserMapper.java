package com.smart.investment.common.security.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.smart.investment.common.security.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统用户 Mapper
 */
@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
}
