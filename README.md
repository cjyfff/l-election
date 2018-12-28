# l-election
基于zookeeper和spring boot的分布式选主sdk

## 主要功能
1. 实现集群选举以及分片，在选举完成后可以得到集群的分片信息以及自身的选举状态。
2. 处理了例如网络分区，进程被kill等导致的主从切换情况。
3. 可以由集成此sdk的程序自定义选举变为完成（或选举状态变为未完成)前后的需要执行的逻辑。例如master需要在宣告选举完成前对数据重新分片，集成方可以实现指定的类，sdk会自动注入类里面的逻辑，在master需要在宣告选举完成前执行。

## 集成方法
1. 下载项目代码，进入代码目录，执行mvn install把项目安装到本地仓库，然后在pom中添加依赖：
```
<dependency>
  <groupId>com.cjyfff</groupId>
  <artifactId>l-election</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```
2. 由于这个sdk是基于spring boot，为了让sdk的bean能够注入到项目中，需要在项目的配置类中添加`@ComponentScan(basePackages={"com.cjyfff.election"})`
3. 在项目中注入`com.cjyfff.election.core.Election`，然后执行`election.start()`。此方法一般都是在项目启动时执行，可以考虑添加一个Listerner在spring boot的ApplicationReadyEvent事件触发时执行。
4. （可选）自定义选举变为完成（或选举状态变为未完成)前后的需要执行的逻辑。实现下面所指定的类，并且定义为spring bean，sdk将会在选举状态变更时执行自定义的逻辑。例如我想在master宣告选举完成前执行一些逻辑，那么我就定义下面这样一个类：
```
@Component
public class MasterBeforeUpdateElectionFinishBiz implements ElectionBiz {
  @Override
  public void run() {
    // do sth here
  }
}
```
sdk是通过bean名称从ioc容器中获取对应的自定义业务逻辑，因此类的名称必须定义为`MasterBeforeUpdateElectionFinishBiz`，或者用`@Qualifier`定义为`masterBeforeUpdateElectionFinishBiz`，不然sdk将无法获取到这个类。
详细的逻辑类名如下：
```
// master宣告选举状态变为完成前执行的逻辑
MasterBeforeUpdateElectionFinishBiz

// master宣告选举状态变为完成后执行的逻辑
MasterAfterUpdateElectionFinishBiz

// master宣告选举状态变为未完成前执行的逻辑
MasterBeforeUpdateElectionNotYetBiz

// master宣告选举状态变为未完成后执行的逻辑
MasterAfterUpdateElectionNotYetBiz

// slave宣告选举状态变为完成前执行的逻辑
SlaveBeforeUpdateElectionFinishBiz

// slave宣告选举状态变为完成后执行的逻辑
SlaveAfterUpdateElectionFinishBiz

// slave宣告选举状态变为未完成前执行的逻辑
SlaveBeforeUpdateElectionNotYetBiz

// slave宣告选举状态变为未完成后执行的逻辑
SlaveAfterUpdateElectionNotYetBiz
```
