package com.sequenceiq.cloudbreak.service.cluster.flow.blueprint;

import static java.util.Collections.singletonMap;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.ResourceStatus;
import com.sequenceiq.cloudbreak.api.model.rds.RDSDatabase;
import com.sequenceiq.cloudbreak.api.model.rds.RdsType;
import com.sequenceiq.cloudbreak.blueprint.BlueprintProcessorFactory;
import com.sequenceiq.cloudbreak.blueprint.BlueprintTextProcessor;
import com.sequenceiq.cloudbreak.domain.Blueprint;
import com.sequenceiq.cloudbreak.domain.Cluster;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.domain.Stack;
import com.sequenceiq.cloudbreak.orchestrator.model.SaltPillarProperties;
import com.sequenceiq.cloudbreak.repository.ClusterRepository;
import com.sequenceiq.cloudbreak.repository.RdsConfigRepository;
import com.sequenceiq.cloudbreak.service.rdsconfig.RdsConfigService;
import com.sequenceiq.cloudbreak.util.PasswordUtil;

@Component
public class HiveConfigProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HiveConfigProvider.class);

    @Value("${cb.hive.database.user:hive}")
    private String hiveDbUser;

    @Value("${cb.hive.database.db:hive}")
    private String hiveDb;

    @Value("${cb.hive.database.port:5432}")
    private String hiveDbPort;

    @Inject
    private BlueprintProcessorFactory blueprintProcessorFactory;

    @Inject
    private RdsConfigRepository rdsConfigRepository;

    @Inject
    private RdsConfigService rdsConfigService;

    @Inject
    private ClusterRepository clusterRepository;

    public String getHiveDbUser() {
        return hiveDbUser;
    }

    public String getHiveDb() {
        return hiveDb;
    }

    public String getHiveDbPort() {
        return hiveDbPort;
    }

    public boolean isRdsConfigNeedForHiveMetastore(Blueprint blueprint) {
        BlueprintTextProcessor blueprintProcessor = blueprintProcessorFactory.get(blueprint.getBlueprintText());
        return blueprintProcessor.componentExistsInBlueprint("HIVE_METASTORE")
                && !blueprintProcessor.componentExistsInBlueprint("MYSQL_SERVER")
                && !blueprintProcessor.hiveDatabaseConfigurationExistsInBlueprint();
    }

    public void decorateServicePillarWithPostgresIfNeeded(Map<String, SaltPillarProperties> servicePillar, Stack stack, Cluster cluster) {
        if (isRdsConfigNeedForHiveMetastore(cluster.getBlueprint())) {
            Set<RDSConfig> rdsConfigs = createPostgresRdsConfigIfNeeded(stack, cluster);
            RDSConfig rdsConfig = rdsConfigs.stream().filter(rdsConfig1 -> rdsConfig1.getType().equalsIgnoreCase(RdsType.HIVE.name())).findFirst().get();
            Map<String, Object> postgres = new HashMap<>();
            postgres.put("database", hiveDb);
            postgres.put("user", hiveDbUser);
            postgres.put("password", rdsConfig.getConnectionPassword());
            servicePillar.put("postgresql-server", new SaltPillarProperties("/postgresql/postgre.sls", singletonMap("postgres", postgres)));
        }
    }

    public Set<RDSConfig> createPostgresRdsConfigIfNeeded(Stack stack, Cluster cluster) {
        Set<RDSConfig> rdsConfigs = rdsConfigRepository.findByClusterId(stack.getOwner(), stack.getAccount(), cluster.getId());
        if (isRdsConfigNeedForHiveMetastore(cluster.getBlueprint())
                && rdsConfigs.stream().noneMatch(rdsConfig -> rdsConfig.getType().equalsIgnoreCase(RdsType.HIVE.name()))) {
            LOGGER.info("Creating postgres RDSConfig");
            rdsConfigs = createHivePostgresRdsConf(stack, cluster, rdsConfigs, hiveDbUser, hiveDbPort, hiveDb);
        }
        return rdsConfigs;
    }

    private Set<RDSConfig> createHivePostgresRdsConf(Stack stack, Cluster cluster, Set<RDSConfig> rdsConfigs, String dbUserName, String dbPort, String dbName) {
        RDSConfig rdsConfig = new RDSConfig();
        rdsConfig.setName(stack.getName() + stack.getId());
        rdsConfig.setConnectionUserName(dbUserName);
        rdsConfig.setConnectionPassword(PasswordUtil.generatePassword());
        String primaryGatewayIp = stack.getPrimaryGatewayInstance().getPrivateIp();
        rdsConfig.setConnectionURL("jdbc:postgresql://" + primaryGatewayIp + ":" + dbPort + "/" + dbName);
        rdsConfig.setDatabaseEngine(RDSDatabase.POSTGRES.name());
        rdsConfig.setType(RdsType.HIVE.name());
        rdsConfig.setConnectionDriver(RDSDatabase.POSTGRES.getDbDriver());
        rdsConfig.setStatus(ResourceStatus.DEFAULT);
        rdsConfig.setCreationDate(new Date().getTime());
        rdsConfig.setOwner(stack.getOwner());
        rdsConfig.setAccount(stack.getAccount());
        rdsConfig.setClusters(Collections.singleton(cluster));
        rdsConfig = rdsConfigService.create(rdsConfig);

        if (rdsConfigs == null) {
            rdsConfigs = new HashSet<>();
        }
        rdsConfigs.add(rdsConfig);
        cluster.setRdsConfigs(rdsConfigs);
        clusterRepository.save(cluster);
        return rdsConfigs;
    }
}
