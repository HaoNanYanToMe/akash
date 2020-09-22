package prism.akash.schema.core;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import prism.akash.schema.BaseSchema;

/**
 * 核心基础数据管理（逻辑）类
 * ※主要用于数据表及数据字段的新增、编辑及删除
 *
 * TODO : 系统·基础核心逻辑 （独立）
 *
 * @author HaoNan Yan
 */
@Service("coreBaseSchema")
@Transactional(readOnly = true)
public class coreBaseSchema extends BaseSchema {


}
