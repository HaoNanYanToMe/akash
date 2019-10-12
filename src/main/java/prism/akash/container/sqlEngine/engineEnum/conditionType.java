package prism.akash.container.sqlEngine.engineEnum;

public enum conditionType {

    EQ(" = "),//等于
    LIKE(" LIKE "),//模糊查询
    LIKEBINARY(" LIKE BINARY "),//忽略中英文大小写匹配
    GT(" > "),//大于
    GTEQ("  >= "),//大于等于
    LT("  < "),//小于
    LTEQ("  <=  "),//小于等于
    NEQ(" <> "),//不等于
    IN(" IN "),//包含
    NOTIN(" NOT IN "),//不包含
    ISNULL(" IS NULL "),//为空
    NOTNULL(" IS NOT NULL "),//不为空
    BET(" BETWEEN ");//在……区间,Between and……


    private String value;

    conditionType(String value){
        this.value = value;
    }

    public String getconditionType(){
        return value;
    }

    //获取枚举对象
    public static conditionType getconditionType(String value){
        for (conditionType co:conditionType.values()) {
            if(value.equals(co.name())){
                return co;
            }
        }
        return null;
    }
}
