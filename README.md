# 时间轮算法
从Kafka的Scala实现抄来的，根据Java语言特性做了一些不得已的修改

算法与实现介绍  
[https://www.confluent.io/blog/apache-kafka-purgatory-hierarchical-timing-wheels/](https://www.confluent.io/blog/apache-kafka-purgatory-hierarchical-timing-wheels/)

# 可商榷的地方
* `SystemTimer`的TaskExecutor默认是1个线程运行且不可修改，可能不太合理，依据项目需要可做修改
* `Kafka`中的`hiResClockMs()`方法被我直接修改为`System.currentTimeMillis()`