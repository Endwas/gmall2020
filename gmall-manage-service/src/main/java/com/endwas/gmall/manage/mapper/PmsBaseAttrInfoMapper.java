package com.endwas.gmall.manage.mapper;

        import com.endwas.gmall.bean.PmsBaseAttrInfo;
        import org.apache.ibatis.annotations.Param;
        import tk.mybatis.mapper.common.Mapper;

        import java.util.List;

public interface PmsBaseAttrInfoMapper extends Mapper<PmsBaseAttrInfo> {
    List<PmsBaseAttrInfo> selectBaseAttrInfoById(@Param("attrId") String attrId);
}
