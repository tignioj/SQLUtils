# Usage
## Step1: Add annotation in your entity
### introduce
- replace int, double, boolean with Integer, Double, Boolean in you entity.
- add annotation`@TableInfo(tableName='String xxx')` to your entity, `xxx` should be the same in your database.
- If you have a foreign key associated with another entity class, 
    you should declare `ColumnInfo(referenceColumn='String xxx', referenceTable=Class YYY.class)` on the field.
    Where `xxx` represents the field that references the   foreign key entity class, 
    and `YYY.class` represents the foreign key entity class 
- `@ColumnInfo(value="String columnName")` where columnName must correspond to a field in the database. 
    default value is the name of the entity field name.
- `@ColumnInfo(isPrimaryKey='boolean isPK')'` must be declared on the primary key field of the entity class.
- Entity must have public get,set methods, and parameterless constructors
    
### example:
User.java
```java
@TableInfo(tableName = "user")
public class User {
    @ColumnInfo(isPrimaryKey = true)
    private String id;
    private String name;
    private Integer age;
    private Integer score;
    private Double salary;
    private Date birthday;

    @ColumnInfo(value = "address", referencedTable = Address.class, referencedColumn = "id1")
    private Address address;

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", age=" + age +
                ", score=" + score +
                ", salary=" + salary +
                ", birthday=" + birthday +
                ", address=" + address +
                '}';
    }

    public String getId() {
        return id;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Double getSalary() {
        return salary;
    }

    public void setSalary(Double salary) {
        this.salary = salary;
    }

    public Date getBirthday() {
        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public User() {
    }

}

```

Address.java
```java
@TableInfo(tableName = "address")
public class Address {

    @ColumnInfo(isPrimaryKey = true, value = "id1")
    private String id;

    private String name;


    @ColumnInfo(referencedTable = User.class, referencedColumn = "address")
    private ArrayList<User> users;


    @Override
    public String toString() {
        return "Address{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", users=" + users +
                '}';
    }

    public ArrayList<User> getUsers() {
        return users;
    }

    public void setUsers(ArrayList<User> users) {
        this.users = users;
    }
    public Address() {
    }


    public Address(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

## Step2: Inherited `BaseDaoImpl<T>` class
```java
public class UserDaoImpl extends BaseDaoImpl<User> {
}
```

## Step3: create `c3p0-config.xml` under src/main directory
```xml
<?xml version="1.0" encoding="UTF-8" ?>
<c3p0-config>
    <default-config>
        <!--database configuration-->
        <property name="jdbcUrl">jdbc:mysql://localhost:3306/mydb</property>
            <!--if you using mySQL database, uncomment the next line and comment the second line-->
<!--        <property name="driverClass">com.mysql.jdbc.Driver</property>-->
        <property name="driverClass">org.mariadb.jdbc.Driver</property>
        <property name="user">root</property>
        <property name="password">password</property>

        <!--thread pool configuration-->
        <property name="acquireIncrement">3</property>
        <property name="initialPoolSize">10</property>
        <property name="minPoolSize">2</property>
        <property name="maxPoolSize">20</property>
    </default-config>
</c3p0-config>

```
The mariadb driver is used by default, 
 If you are using mySQL, you should replace `org.mariadb.jdbc.Driver` with `com.mysql.jdbc.Driver`
```xml
<property name="driverClass">com.mysql.jdbc.Driver</property>
<!--<property name="driverClass">org.mariadb.jdbc.Driver</property>-->
```

## Step4: invoke method
```java
class TestSQLUtils {
    @Test
    public void testGetAllUser() throws SQLException {
        UserDaoImpl userDao = new UserDaoImpl();
        //note: true in getAll(false) mean query foreign key entity.
        List<User> users = userDao.getAll(true);
        for (User u : users) {
            System.out.println(u);
        }
    }
}
```
The output is as follows
```
User{id='1', name='zs', age=23, score=88, salary=111.1, birthday=2020-02-04, address=null}
User{id='2', name='ls', age=33, score=97, salary=3323.2, birthday=2020-01-27, address=null}
User{id='3', name='王五', age=55, score=95, salary=56252.0, birthday=2020-01-06, address=null}
```



