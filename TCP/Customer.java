package TCP;

import java.io.Serializable;

public class Customer implements Serializable {
    private static final long serialVersionUID = 20170711L;
    private int id;
    private String code;
    private String name;
    private String dayOfBirth;
    private String userName;
    Customer(int id, String code, String name, String dayOfBirth, String userName) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.dayOfBirth = dayOfBirth;
        this.userName = userName;
    }
    public void setId(int id) {
        this.id = id;
    }
    public int getId() {
        return id;
    }
    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getName() {
        return name;
    }
    public void setDayOfBirth(String dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }
    public String getDayOfBirth() {
        return dayOfBirth;
    }
    public void setUserName(String userName) {
        this.userName = userName;
    }
    public String getUserName() {
        return userName;
    }
    @Override 
    public String toString() {
        return "Customer{" + "id=" + id + ", name=" + name + ", dayOfBirth=" + dayOfBirth + ", userName=" + userName + '}';
    }
}
