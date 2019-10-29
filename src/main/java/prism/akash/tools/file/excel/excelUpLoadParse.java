package prism.akash.tools.file.excel;

import com.alibaba.fastjson.JSON;
import com.monitorjbl.xlsx.StreamingReader;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import prism.akash.api.BaseApi;
import prism.akash.container.BaseData;
import prism.akash.container.sqlEngine.engineEnum.conditionType;
import prism.akash.container.sqlEngine.engineEnum.groupType;
import prism.akash.container.sqlEngine.engineEnum.joinType;
import prism.akash.container.sqlEngine.engineEnum.queryType;
import prism.akash.container.sqlEngine.sqlEngine;
import prism.akash.tools.date.dateParse;
import prism.akash.tools.file.FileUpload;

import java.io.FileInputStream;
import java.io.Serializable;
import java.util.*;

@Service
public class excelUpLoadParse implements FileUpload, Serializable {

    private static final Logger log = LoggerFactory.getLogger(excelUpLoadParse.class);

    @Autowired
    BaseApi baseApi;

    @Autowired
    dateParse dateParse;

    //HSSText  Excel内部信息字段处理
    private Object getHSSTextString(Row row, int colNum) {
        Cell cell = row.getCell(colNum);
        if (null != cell) {
            switch (cell.getCellType()) {
                case HSSFCell.CELL_TYPE_NUMERIC:
                    return cell.getNumericCellValue();
                case HSSFCell.CELL_TYPE_STRING:
                    return cell.getStringCellValue().trim();
                case HSSFCell.CELL_TYPE_BLANK:
                    // 空值
                    return "";
                case HSSFCell.CELL_TYPE_ERROR:
                    // 故障
                    return "";
                case HSSFCell.CELL_TYPE_FORMULA:
                    try {
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        return cell.getStringCellValue();
                    } catch (IllegalStateException e) {
                        return cell.getNumericCellValue();
                    }
                default:
                    return "";
            }
        } else {
            return "";
        }
    }

    @Override
    public Map<String, String> importExcel(String fileUrl, BaseData bd, boolean isGenerateId, int patchNum) throws Exception {
        Map<String, String> result = new HashMap<>();
        result.put("result", "数据录入成功");

        if (bd.get("execute") != null) {
            String execute = bd.getString("execute");
            // TODO : 获取数据源字段
            List<BaseData> columns = baseApi.selectBase(new sqlEngine()
                    .execute("columnarray", "c")
                    .appointColumn("c", groupType.DEF, "")
                    .joinBuild("tablearray", "t", joinType.L)
                    .joinColunm("c", "tid", "id").joinFin()
                    .queryBuild(queryType.and, "t", "@code", conditionType.EQ, groupType.DEF, execute)
                    .selectFin(""));

//            new sqlEngine().execute("a","a").appointColumn("a",1groupType.DEF)
            // TODO : 获取全表字段
            StringBuffer cols = new StringBuffer();
            for (BaseData col : columns) {
                cols.append(",").append(col.get("code"));
            }
            String colAppend = cols.deleteCharAt(0).toString();
            // TODO : 获取Excel缓存数据
            FileInputStream in = new FileInputStream(fileUrl);
            Workbook wk = StreamingReader.builder()
                    .rowCacheSize(1000)  //缓存到内存中的行数，默认是10
                    .bufferSize(10240)  //读取资源时，缓存到内存的字节大小，默认是1024
                    .open(in);  //打开资源，必须，可以是InputStream或者是File，注意：只能打开XLSX格式的文件
            Sheet sheet = wk.getSheetAt(0);
            // TODO : 获取表头
            Row head = null;
            List<BaseData> addDatas = new ArrayList<>();
            // TODO ： 设定执行条数及批次
            int executeNum = 0;
            int countSum = 1;
            // TODO : 对缓存数据进行遍历
            for (Row row : sheet) {
                if(row.getRowNum() == 0){
                    head = row;
                }else{
                    BaseData addData = new BaseData();
                    if (isGenerateId) {
                        addData.put("id", UUID.randomUUID().toString().replaceAll("-", ""));
                    }
                    // TODO : 字段定向匹配
                    for (int j = 0; j < head.getPhysicalNumberOfCells(); j++) {
                        if (!getHSSTextString(row, j).toString().equals("")) {
                            for (BaseData col : columns) {
                                if (getHSSTextString(head, j).equals(col.getString("name"))) {
                                    // TODO : 类型匹配转换
                                    String type = col.getString("type");
                                    Object res = getHSSTextString(row, j);
                                    if (type.equals("int")) {
                                        res = Integer.parseInt(res + "");
                                    } else if (type.equals("double")) {
                                        res = Double.parseDouble(res + "");
                                    } else if (type.equals("date")) {
//                                        res = dateParse.parseDate(res.toString());
                                    } else if (type.equals("time")) {
//                                        res = dateParse.parseTime(res.toString());
                                    } else if (type.equals("datetime")) {
//                                        res = dateParse.parseDateTime(res.toString());
                                    } else {
                                        res = res.toString();
                                    }
                                    addData.put(col.getString("code"), res);
                                }
                            }
                        }
                    }
                    addDatas.add(addData);
                }
                executeNum++;
                if (executeNum == countSum*patchNum) {
                    int res = baseApi.execute(new sqlEngine().execute(execute, execute).
                            insertFetchPush(JSON.toJSONString(addDatas), colAppend).insertFin(""));
                    System.out.println("保存成功："+res+",当前数据保存节点:"+row.getRowNum()+",数据批次为:"+countSum);
                    addDatas.clear();
                    countSum++;
                }else if(executeNum > countSum*patchNum){
                    //余量数据处理
                    int res = baseApi.execute(new sqlEngine().execute(execute, execute).
                            insertFetchPush(JSON.toJSONString(addDatas), colAppend).insertFin(""));
                    System.out.println("保存成功："+res+",当前数据保存节点:"+row.getRowNum()+",数据批次为:"+countSum);
                    addDatas.clear();

                }
            }
        }else {
            result.put("result",
                    "⚠:未指定数据目标源表！");
            return result;
        }
        return result;
    }
}
