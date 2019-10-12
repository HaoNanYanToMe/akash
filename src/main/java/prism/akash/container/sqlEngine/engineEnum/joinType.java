package prism.akash.container.sqlEngine.engineEnum;

/**
 * Enum Join Pattern (表关系枚举)
 */
public enum joinType {

    R(" RIGHT JOIN "),
    L(" LEFT JOIN "),
    I(" INNER JOIN "),
    C(" JOIN "),
    S(" STRAIGHT_JOIN "),
    RO(" RIGHT OUTRE JOIN"),
    LO(" LEFT  OUTRE JOIN");

    private String value;

    joinType(String value){
        this.value = value;
    }

    public String getJoinType(){
        return value;
    }

    //获取枚举对象
    public static joinType getJoinType(String value){
        for (joinType jo:joinType.values()) {
            if(value.equals(jo.name())){
                return jo;
            }
        }
        return null;
    }
}
