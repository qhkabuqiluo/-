package com.jdk.analyse.hash;

import org.springframework.util.StringUtils;

import java.util.*;

public class ConsistencyHash {
    //真实节点
    private List<String> nodes = new LinkedList<>();
    //虚拟节点
    private SortedMap<Integer, String> virtualNodes = new TreeMap<>();

    public TreeMap<String, Integer> countServer = new TreeMap<>();
    //默认真实节点对应的虚拟节点个数
    private int virtual_nodes = 10;

    public ConsistencyHash(List<String> nodes, int virtual_nodes) {
        this.nodes = nodes;
        this.virtual_nodes = virtual_nodes;
        for (String node : nodes) {
            for (int i = 0; i < virtual_nodes; i++) {
                String vnName = node + "&vn" + i;
                int hash = getHash(vnName);
                System.out.println("初始化虚拟节点：" + vnName + " 被添加，hash值为：" + hash);
                virtualNodes.put(hash, vnName);
            }
        }
    }

    public ConsistencyHash addNode(String node) {
        nodes.add(node);
        for (int i = 0; i < virtual_nodes; i++) {
            String vnName = node + "&vn" + i;
            int hash = getHash(vnName);
            System.out.println("虚拟节点：" + vnName + " 被添加，hash值为：" + hash);
            virtualNodes.put(hash, vnName);
        }
        return this;
    }

    /**
     * 获取服务节点的hash值
     *
     * @param server
     * @return
     */
    public int getHash(String server) {
        final int p = 16777619;
        int hash = (int) 2166136261L;
        for (int i = 0; i < server.length(); i++)
            hash = (hash ^ server.charAt(i)) * p;
        hash += hash << 13;
        hash ^= hash >> 7;
        hash += hash << 3;
        hash ^= hash >> 17;
        hash += hash << 5;
        if (hash < 0) {
            hash = Math.abs(hash);
        }
        return hash;
    }

    public String addCache(String key) {
        //1. 计算key的hash值
        int hash = getHash(key);
        //2. 获取大于该hash值的所有Map
        SortedMap<Integer, String> subMap = virtualNodes.tailMap(hash);
        String virtualNode;
        String node = null;
        if (subMap.isEmpty()) {
            //如果没有比该key的hash值大的，则从第一个node开始
            //返回对应的服务器
            virtualNode = virtualNodes.get(virtualNodes.firstKey());
        } else {
            //获取顺时针过去离node最近的那个结点，第一个key就是
            //返回对应的服务器
            virtualNode = subMap.get(subMap.firstKey());
        }
        //virtualNode虚拟节点名称要截取一下
        if (!StringUtils.isEmpty(virtualNode)) {
            node = virtualNode.substring(0, virtualNode.indexOf("&"));
        }
        //统计次数
        if (countServer.get(node) == null) {
            countServer.put(node, 1);
        } else {
            countServer.put(node, countServer.get(node) + 1);
        }


        return node;
    }

    /**
     * 计算标准差
     *
     * @return
     */
    public double calcStd() {
        Integer[] visitData = new Integer[countServer.size()];
        countServer.values().toArray(visitData);
        double avg = Arrays.stream(visitData).mapToInt(Integer::intValue).average().orElse(0d);
        double avgStd = Arrays.stream(visitData).map(count -> Math.pow(count - avg, 2)).mapToDouble(Double::doubleValue).average().orElse(0d);
        double std = Math.sqrt(avgStd);
        return std;
    }

    public static void main(String[] args) {
        List<String> nodes = new ArrayList<>();
        nodes.add("192.168.1.1:8080");
        nodes.add("192.168.1.2:8080");
        nodes.add("192.168.1.3:8080");
        nodes.add("192.168.1.4:8080");
        nodes.add("192.168.1.5:8080");
        nodes.add("192.168.1.6:8080");
        ConsistencyHash consistencyHash = new ConsistencyHash(nodes, 100);
        consistencyHash.addNode("192.168.1.7:8080").addNode("192.168.1.8:8080")
                .addNode("192.168.1.9:8080").addNode("192.168.1.10:8080");
        long start = System.currentTimeMillis();
        int times = 1000000;
        for (int i = 0; i < times; i++) {
            consistencyHash.addCache(UUID.randomUUID().toString());
        }
        System.out.println("总耗时：" + (System.currentTimeMillis() - start) + " 标准差：" + consistencyHash.calcStd());
        consistencyHash.countServer.forEach((k, v) -> {
            System.out.println(k + " - [ " + v + " - " + times / consistencyHash.countServer.size() + " = " + (v - times / consistencyHash.countServer.size()) + " ]");
        });
    }
}
