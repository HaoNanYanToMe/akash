package prism.akash.dataInteraction;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Component;
import prism.akash.container.BaseData;

import java.util.List;

@Mapper
@Component
public interface BaseInteraction {

    @Update("insert into engines(id,name) value('1','测试查询1')")
    int update();

    @Select("${bd.select}")
    List<BaseData> select(@Param("bd")BaseData bd);
}
