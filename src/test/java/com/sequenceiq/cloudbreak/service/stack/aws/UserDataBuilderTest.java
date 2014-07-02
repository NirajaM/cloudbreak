package com.sequenceiq.cloudbreak.service.stack.aws;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sequenceiq.cloudbreak.domain.CloudPlatform;
import com.sequenceiq.cloudbreak.service.stack.UserDataBuilder;
import com.sequenceiq.cloudbreak.util.FileReaderUtils;

public class UserDataBuilderTest {

    private static UserDataBuilder userDataBuilder;

    @BeforeClass
    public static void before() throws IOException {
        Map<CloudPlatform, String> userDataScripts = new HashMap<>();
        for (CloudPlatform cloudPlatform : CloudPlatform.values()) {
            userDataScripts.put(cloudPlatform, FileReaderUtils.readFileFromClasspath(String.format("%s-init.sh", cloudPlatform.getValue())));
        }
        userDataBuilder = new UserDataBuilder();
        userDataBuilder.setUserDataScripts(userDataScripts);
    }

    @Test
    public void testBuildUserDataEc2() throws IOException {
        String expectedScript = FileReaderUtils.readFileFromClasspath("ec2-init-test-expected.sh");
        Map<String, String> map = new HashMap<>();
        map.put("NODE_PREFIX", "testamb");
        map.put("MYDOMAIN", "test.kom");
        Assert.assertEquals(expectedScript, userDataBuilder.build(CloudPlatform.AWS, map));
    }

    @Test
    public void testBuildUserDataAzure() throws IOException {
        String expectedScript = FileReaderUtils.readFileFromClasspath("azure-init-test-expected.sh");
        Map<String, String> map = new HashMap<>();
        map.put("NODE_PREFIX", "testamb");
        map.put("MYDOMAIN", "test.kom");
        Assert.assertEquals(expectedScript, userDataBuilder.build(CloudPlatform.AZURE, map));
    }

}
