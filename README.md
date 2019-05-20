Non-Intrusive Mybatis Sharding Plugin, source from [code.google.com/p/shardbatis](https://code.google.com/archive/p/shardbatis/)

What is shardbatis?

>Shardbatis的名称由shard(ing)+mybatis组合得到，诣在为mybatis实现数据水平切分的功能。数据的水平切分包括多数据库的切分和多表的数据切分，目前shardbatis只实现了单数据库的数据多表水平切分。Shardbatis2.0以插件的方式和mybatis3.x进行整合，对mybatis的代码无侵入，不改变用户对mybatis的使用习惯。

# shardbatis2.x使用指南

### 运行环境
jdk 8.0
mybatis 3.5.1

### 下载 & 安装

git clone https://github.com/colddew/shardbatis.git

a）2.0.1以后的版本直接引入maven依赖即可

```xml
<dependency>
    <groupId>org.shardbatis</groupId>
    <artifactId>shardbatis</artifactId>
    <version>2.0.1</version>
</dependency>
```

b）2.0.0B及以前的版本需要将repository目录下的shardbatis和jsqlparser导入maven本地仓库或者公司的二方库

```
mvn install:install-file -Dfile=./repository/org/shardbatis/shardbatis/2.0.0B/shardbatis-2.0.0B.jar -DgroupId=org.shardbatis -DartifactId=shardbatis -Dversion=2.0.0B -Dpackaging=jar -DgeneratePom=true -DcreateChecksum=true
```

```xml
<dependency>
    <groupId>org.shardbatis</groupId>
    <artifactId>shardbatis</artifactId>
    <version>2.0.0B</version>
</dependency>

<!-- 由于googlecode已关闭远程仓库，已不可用 -->
<repository>
    <id>shardbaits</id>
    <name>shardbaits repository</name>
    <url>http://shardbatis.googlecode.com/svn/trunk/repository</url>
    <snapshots>
        <enabled>false</enabled>
    </snapshots>
</repository>
```

### 配置

在项目的classpath中添加sharding配置文件shard_config.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE shardingConfig PUBLIC "-//shardbatis.googlecode.com//DTD Shardbatis 2.0//EN" "http://shardbatis.googlecode.com/dtd/shardbatis-config.dtd">
<shardingConfig>
    <!--
        ignoreList可选配：ignoreList配置的mapperId会被分表参加忽略解析,不会对sql进行修改
    -->
    <ignoreList>
        <value>com.google.code.shardbatis.test.mapper.AppTestMapper.insertNoShard</value>
    </ignoreList>
    <!-- 
        parseList可选配置：如果配置了parseList,只有在parseList范围内并且不再ignoreList内的sql才会被解析和修改
    -->
    <parseList>
        <value>com.google.code.shardbatis.test.mapper.AppTestMapper.insert</value>
    </parseList>
    <!-- 
        配置分表策略
    -->
    <strategy tableName="APP_TEST" strategyClass="com.google.code.shardbatis.strategy.impl.AppTestShardStrategyImpl"/>
</shardingConfig>
```

a）在代码中添加插件配置

```java
@Configuration
@MapperScan(basePackages = "xxx.xxx.mapper", sqlSessionFactoryRef = "sqlSessionFactory")
public class DatasourceConfig {

    // 省略dataSource配置等相关代码

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) {
        
    	try {
            final SqlSessionFactoryBean sessionFactory = new SqlSessionFactoryBean();
            sessionFactory.setDataSource(dataSource);
            sessionFactory.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath:mapper/*Mapper.xml"));
            sessionFactory.setPlugins(new Interceptor[] { getShardPlugin() });
            
            return sessionFactory.getObject();
        } catch (Exception e) {
        	throw new RuntimeException("sqlSessionFactory configuration error", e);
        }
    }
    
    private Interceptor getShardPlugin() {
    	
    	Properties properties = new Properties();
    	properties.setProperty("shardingConfig", "shard_config.xml");
    	
    	ShardPlugin shardPlugin = new ShardPlugin();
    	shardPlugin.setProperties(properties);
    	
    	return shardPlugin;
    }
}
```

b）或者在mybatis配置文件中添加插件配置

```xml
<plugins>
    <plugin interceptor="com.google.code.shardbatis.plugin.ShardPlugin">
        <property name="shardingConfig" value="shard_config.xml"/>
    </plugin>
</plugins>
```

### 实现自定义sharding策略

实现ShardStrategy接口，参考实现 [`com.google.code.shardbatis.strategy.impl.AppTestShardStrategyImpl`](https://github.com/colddew/shardbatis/blob/master/src/test/java/com/google/code/shardbatis/strategy/impl/AppTestShardStrategyImpl.java)

```java
/**
* 分表策略接口
*/
public interface ShardStrategy {
    /**
     * 得到实际表名
     * @param baseTableName 逻辑表名,一般是没有前缀或者是后缀的表名
     * @param params mybatis执行某个statement时使用的参数
     * @param mapperId mybatis配置的statement id
     * @return 分表表名
     */
    String getTargetTableName(String baseTableName,Object params,String mapperId);
}

public class XXXShardStrategy implements ShardStrategy {
	
	@Override
	public String getTargetTableName(String baseTableName, Object params, String mapperId) {
		return baseTableName + getTableNameSuffix(params);
	}
	
	private String getTableNameSuffix(Object params) {
		// 可以根据用户id求余或者hash等策略获取分表表名
	}
}
```

### 代码中使用shardbatis

因为shardbatis2.x使用插件方式对mybatis功能进行增强，代码无侵入，因此使用配置了shardbatis的mybatis3和使用原生的mybatis3没有区别

```java
SqlSession session = sqlSessionFactory.openSession();
try {
    AppTestMapper mapper = session.getMapper(AppTestMapper.class);
    mapper.insert(testDO);
    session.commit();
} finally {
    session.close();
}
```

#### 使用注意事项

2.x版本中inser、update、delete语句中的子查询语句中的表不支持sharding

select语句中如果进行多表关联，请务必为每个表名加上别名，例如原始sql语句：`SELECT a.* FROM ANTIQUES a, ANTIQUEOWNERS b, mytable c where a.id=b.id and b.id=c.id`
经过转换后的结果可能为：`SELECT a.* FROM ANTIQUES_0 AS a, ANTIQUEOWNERS_1 AS b, mytable_1 AS c WHERE a.id = b.id AND b.id = c.id`	

shardbatis对sql的解析基于jsqlparser，目前已经支持大部分sql语句的解析，已经测试通过的语句可以查看测试用例：

```sql
select * from test_table1
select * from test_table1 where col_1='123'
select * from test_table1 where col_1='123' and col_2=8
select * from test_table1 where col_1=?
select col_1,max(col_2) from test_table1 where col_4='t1' group by col_1
select col_1,col_2,col_3 from test_table1 where col_4='t1' order by col_1
select col_1,col_2,col_3 from test_table1 where id in (?,?,?,?,?,?,?,?,?) limit ?,?
select a.*  from test_table1 a,test_table2 b where a.id=b.id and a.type='xxxx'
select a.col_1,a.col_2,a.col_3 from test_table1 a where a.id in (select aid from test_table2 where col_1=1 and col_2=?) order by id desc
select col_1,col_2 from test_table1 where type is not null and col_3 is null order by id
select count(*),col_1 from test_table2 group by col_1 having count(*)>1
select a.col_1,a.col_2,b.col_1 from test_table1 a,t_table b where a.id=b.id
insert into test_table1 (col_1,col_2,col_3,col_4) values (?,?,?,?)
SELECT EMPLOYEEIDNO FROM test_table1 WHERE POSITION = 'Manager' AND SALARY > 60000 OR BENEFITS > 12000
SELECT EMPLOYEEIDNO FROM test_table1 WHERE POSITION = 'Manager' AND (SALARY > 50000 OR BENEFIT > 10000)
SELECT EMPLOYEEIDNO FROM test_table1 WHERE LASTNAME LIKE 'L%'
SELECT DISTINCT SELLERID, OWNERLASTNAME, OWNERFIRSTNAME FROM test_table1, test_table2 WHERE SELLERID = OWNERID ORDER BY OWNERLASTNAME, OWNERFIRSTNAME, OWNERID
SELECT OWNERFIRSTNAME, OWNERLASTNAME FROM test_table1 WHERE EXISTS (SELECT * FROM test_table2 WHERE ITEM = ?)
SELECT BUYERID, ITEM FROM test_table1 WHERE PRICE >= ALL (SELECT PRICE FROM test_table2)
SELECT BUYERID FROM test_table1 UNION SELECT BUYERID FROM test_table2
SELECT OWNERID, 'is in both Orders & Antiques' FROM test_table1 a, test_table2 b WHERE a.OWNERID = b.BUYERID and a.type in (?,?,?)
SELECT DISTINCT SELLERID, OWNERLASTNAME, OWNERFIRSTNAME FROM test_table1, noconvert_table WHERE SELLERID = OWNERID ORDER BY OWNERLASTNAME, OWNERFIRSTNAME, OWNERID
SELECT a.* FROM test_table1 a, noconvert_table b WHERE a.SELLERID = b.OWNERID 
update test_table1 set col_1=123 ,col_2=?,col_3=? where col_4=?
update test_table1 set col_1=?,col_2=col_2+1 where id in (?,?,?,?)
delete from test_table2 where id in (?,?,?,?,?,?) and col_1 is not null
INSERT INTO test_table1 VALUES (21, 01, 'Ottoman', ?,?)
INSERT INTO test_table1 (BUYERID, SELLERID, ITEM) VALUES (01, 21, ?)
```

### 测试

安装并启动h2数据库
```
curl -O http://www.h2database.com/h2-2019-03-13.zip
<h2-home>/bin/h2.sh
```

初始化h2数据库的schema以及测试数据

`mvn -Pinitdb initialize`

运行测试

`mvn clean test`

### 未来计划
1）升级依赖，替换jsqlparser本地依赖  
2）发布jar包到maven公共仓库