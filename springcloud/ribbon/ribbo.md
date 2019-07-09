## 应用间通信

**负载均衡器：Ribbon**

* RestTemplate
* Feign
* Zuul

* 服务发现
* 服务选择规则
* 服务监听

组件：
* ServerList 获取可用服务表
* IRule 选择一个实例作为最终目标
* ServerListFilter 过滤一部分地址

源码分析（ctrl+alt+b进入源码实现部分

    ServiceInstance product = loadBalancerClient.choose("PRODUCT");

choose方法，其中getServer用于获取服务列表

    public ServiceInstance choose(String serviceId, Object hint) {
        Server server = this.getServer(this.getLoadBalancer(serviceId), hint);
        return server == null ? null : new RibbonLoadBalancerClient.RibbonServer(serviceId, server, this.isSecure(server, serviceId), this.serverIntrospector(serviceId).getMetadata(server));
    }

getServer方法，其中用ILoadBalancer寻找服务

    protected Server getServer(ILoadBalancer loadBalancer, Object hint) {
        return loadBalancer == null ? null : 
            loadBalancer.chooseServer(hint != null ? hint : "default");
    }
    
chooseServer方法，rule在类初始化时为默认规则（轮询规则）this.rule = DEFAULT_RULE;
    
    private static final IRule DEFAULT_RULE = new RoundRobinRule();
    
    public BaseLoadBalancer() {
        this.rule = DEFAULT_RULE;
            
    public Server chooseServer(Object key) {
        if (this.counter == null) {
            this.counter = this.createCounter();
        }
    
        this.counter.increment();
        if (this.rule == null) {
            return null;
        } else {
            try {
                return this.rule.choose(key);
            } catch (Exception var3) {
                logger.warn("LoadBalancer [{}]:  Error choosing server for key {}", new Object[]{this.name, key, var3});
                return null;
            }
        }
    }
    
ILoadBalancer的getALLServers

    public interface ILoadBalancer {
        void addServers(List<Server> var1);
    
        Server chooseServer(Object var1);
    
        void markServerDown(Server var1);
    
        List<Server> getReachableServers();
    
        List<Server> getAllServers();
    }

getAllServers

    public List<Server> getAllServers() {
        return Collections.unmodifiableList(this.allServerList);
    }
