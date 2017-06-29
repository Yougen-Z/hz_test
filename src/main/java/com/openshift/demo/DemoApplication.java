package com.openshift.demo;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.xbill.DNS.ExtendedResolver;
import org.xbill.DNS.Lookup;
import org.xbill.DNS.Record;
import org.xbill.DNS.TextParseException;
import org.xbill.DNS.Type;

import com.hazelcast.config.Config;
import com.hazelcast.config.DiscoveryStrategyConfig;
import com.hazelcast.config.JoinConfig;
import com.hazelcast.config.NetworkConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.kubernetes.HazelcastKubernetesDiscoveryStrategyFactory;
import com.hazelcast.spi.discovery.DiscoveryStrategyFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
public class DemoApplication {
	private static final Logger LOGGER = LoggerFactory.getLogger(DemoApplication.class);

    public static void main(String[] args) {
        checkWork();
        
        SpringApplication.run(DemoApplication.class, args);
    }

    @Bean
    public CommandLineRunner commandLineRunner(ApplicationContext ctx) {
        return args -> {

            System.out.println("Let's inspect the beans provided by Spring Boot:");
            LOGGER.warn("================== Start Here ====================");

            String[] beanNames = ctx.getBeanDefinitionNames();
            Arrays.sort(beanNames);
            for (String beanName : beanNames) {
                System.out.println(beanName);
            }
            createHZInstance();
        };
    }

    public static HazelcastInstance createHZInstance() {
        
    	Config config = new Config();
    	config.setProperty("hazelcast.discovery.enabled", "true");
        config.setProperty("hazelcast.rest.enabled","true");
        JoinConfig joinConfig = new JoinConfig();
        joinConfig.getMulticastConfig().setEnabled(false);
        joinConfig.getTcpIpConfig().setEnabled(false);

        final DiscoveryStrategyFactory factory = new HazelcastKubernetesDiscoveryStrategyFactory();

        final DiscoveryStrategyConfig strategyConfig = new DiscoveryStrategyConfig(factory);
        strategyConfig.addProperty("service-dns", "hazelcast.yg.svc.cluster.local"); //"kubernetes.default.svc.cluster.local"); // SERVICE_DNS_NAME);
        strategyConfig.addProperty("service-dns-timeout", "10");
//        strategyConfig.addProperty("service-name", "hazelcast");
//        strategyConfig.addProperty("service-label-name", "application");
//        strategyConfig.addProperty("service-label-value", "zplatform");
//        strategyConfig.addProperty("namespace", "yg");
//        strategyConfig.addProperty("kubernetes-master", "https://kubernetes.default.svc.cluster.local");
//        strategyConfig.addProperty("api-token", "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJrdWJlcm5ldGVzL3NlcnZpY2VhY2NvdW50Iiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9uYW1lc3BhY2UiOiJ5ZyIsImt1YmVybmV0ZXMuaW8vc2VydmljZWFjY291bnQvc2VjcmV0Lm5hbWUiOiJkZWZhdWx0LXRva2VuLTVkeDNiIiwia3ViZXJuZXRlcy5pby9zZXJ2aWNlYWNjb3VudC9zZXJ2aWNlLWFjY291bnQubmFtZSI6ImRlZmF1bHQiLCJrdWJlcm5ldGVzLmlvL3NlcnZpY2VhY2NvdW50L3NlcnZpY2UtYWNjb3VudC51aWQiOiJlMWU0ZjE2OS00ZmMwLTExZTctYWRhNy0wMDBkM2FmNDQ3NzMiLCJzdWIiOiJzeXN0ZW06c2VydmljZWFjY291bnQ6eWc6ZGVmYXVsdCJ9.IfVd-TdxIofT-dmFhJnaLrM42C11pgPEpAiK8Jjp-2hp27nNhM_p_Kz0yOv11i8av2IUznxZG84DvpDzfszTQ9C_dNMqgAmrzm5t30Ju3txo7dzcPZkUPRAPKjRWsNY9CLMycBio7iYzUFxK7vTEfEsmci6rKw109Q6V4iUxT8-bAitEVWAs8g4sqWpFWYFsofLNmjSsFWncQZekZWVkB2Nl8EfR5KHOA27yqHph0moUr5PiKtFIHb3U_duKXDeoqJKzuMUJte2QojmQZFhixukcuuELQxLn1BinhmvRrXJpV2ENmvywVuVmqbdF5Xvr8JZdm0R8Z9sV_AC7khR_VQ");

        joinConfig.getDiscoveryConfig().addDiscoveryStrategyConfig(strategyConfig);
        
        NetworkConfig networkConfig = new NetworkConfig();
        networkConfig.setJoin(joinConfig);

        networkConfig.setPort(5701).setPortAutoIncrement(false);
        networkConfig.setPortCount(100);
        
        return Hazelcast.newHazelcastInstance(config);
    }
    
    public static void checkWork() {
        LOGGER.warn("================== Start Here ====================");
        try {
            InetAddress dnsresult[];
            try {
                dnsresult = InetAddress.getAllByName("yrouterip.canadaeast.cloudapp.azure.com");
                if (dnsresult != null) {
                    for (int i=0; i<dnsresult.length; i++)
                    	LOGGER.warn(dnsresult[i].toString());
                }

                dnsresult = InetAddress.getAllByName("www.google.com");
                if (dnsresult != null) {
                    for (int i=0; i<dnsresult.length; i++)
                    	LOGGER.warn(dnsresult[i].toString());
                }
            } catch (final UnknownHostException e) {
                e.printStackTrace();
            }
            checkAddress("www.google.com");
            checkAddress("yrouterip.canadaeast.cloudapp.azure.com");
            checkAddress("kubernetes.default.svc.cluster.local");
            checkAddress("etcd.yg.svc.cluster.local");
            checkAddress("zplatform.yg.svc.cluster.local");
            checkAddress("yg.svc.cluster.local");
            checkAddress("svc.cluster.local");
        } catch (final TextParseException e) {
            e.printStackTrace();
        }
    }

    public static void checkAddress(final String addr) throws TextParseException {
        ExtendedResolver resolver;
        try {
            resolver = new ExtendedResolver();
            resolver.setTimeout(15);
            Lookup lookup = new Lookup(addr.trim(), Type.A);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            Record[] records2 = lookup.run();
            LOGGER.warn("DNS lookup " + addr + " Type A: " + Integer.toString(lookup.getResult()));

            lookup = new Lookup(addr, Type.SRV);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            records2 = lookup.run();
            LOGGER.warn("DNS lookup " + addr + " Type SRV: " + Integer.toString(lookup.getResult()));

            lookup = new Lookup(addr, Type.ANY);
            lookup.setResolver(resolver);
            lookup.setCache(null);
            records2 = lookup.run();
            LOGGER.warn("DNS lookup " + addr + " Type ANY: " + Integer.toString(lookup.getResult()));
        } catch (final UnknownHostException e1) {
            e1.printStackTrace();
        }
    }

}