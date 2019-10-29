package prism.akash.tools.file;

import prism.akash.container.BaseData;

import java.util.Map;

public interface FileUpload {

    /**
     * 上传并解析Excel内数据
     * -- * TODO 仅支持简单数据表的解析及处理！
     *
     * @param fileUrl      excel文件地址
     * @param bd           对应的附加信息（如用户或其他待处理字段）
     * @param isGenerateId 是否需要生成主键编号
     * @return
     * @throws Exception
     */
    public Map<String, String> importExcel(String fileUrl, BaseData bd, boolean isGenerateId, int patchNum) throws Exception;
}
