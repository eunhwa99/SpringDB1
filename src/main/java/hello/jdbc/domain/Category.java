package hello.jdbc.domain;

public enum Category {
    STUDY,
    SELF_DEVELOPMENT,
    LEISURE;

    public String getValue(){
        return this.name();
    }

    public static Category getCategory(String value){
        try{
            return Category.valueOf(value);
        }catch(IllegalArgumentException e){
            return null;
        }
    }
}
