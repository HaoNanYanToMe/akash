package prism.akash.tools.annocation.checked;


public enum AccessType {

    NON("NON"),//无权限
    LOGIN("LOGIN"),//登陆访问
    SEL("SEL"),//查询
    UPD("UPD"),//更新
    ADD("ADD"),//新增
    DEL("DEL"),//删除
    DOWN("DOWN"),//下载
    UPLOAD("UPLOAD");//上传

    private String value;

    AccessType(String value) {
        this.value = value;
    }

    public String getAccessType() {
        return value;
    }

    //获取枚举对象
    public static AccessType getAccessType(String value) {
        if (value != null) {
            for (AccessType ec : AccessType.values()) {
                if (value.equals(ec.name())) {
                    return ec;
                }
            }
        }
        return NON;
    }
}
