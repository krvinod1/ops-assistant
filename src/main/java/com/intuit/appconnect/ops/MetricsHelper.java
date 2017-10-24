package com.intuit.appconnect.ops;


import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatch.AmazonCloudWatch;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.Datapoint;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsRequest;
import com.amazonaws.services.cloudwatch.model.GetMetricStatisticsResult;
import com.amazonaws.services.cloudwatch.model.Statistic;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * Created by sjaiswal on 9/29/17.
 */
public class MetricsHelper {

    static String ec2Namespace = "AWS/EC2";
    static String ec2NamespaceName = "InstanceId";
    static String ec2NamespaceValue ="i-03b9cb8f759d42cd5";
    static String rdsNamespace = "AWS/RDS";
    static String rdsNamespaceName = "DBInstanceIdentifier";
    static String rdsNamespaceValue ="itduzzit-qa";
    static String metricName = "CPUUtilization";

    //static AmazonCloudWatch client = AmazonCloudWatchClientBuilder.defaultClient();

    public static String getStats( String namespace, String name, String value) {

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Date date = new Date();
        dateFormat.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String dateString = dateFormat.format(date);
        System.out.print(dateString);

        String message = "The stats at "+dateString+ " is: "+//Fri Sep 29 10:33:00 PDT 2017 . " +
                "Minimum utilization 1.33 . Maximum utilization 2.92 . " +
                "Average utilization 1.67. " +
                "Current server is mostly running less than 70 % utilization. You may want to consider downgrade instance type and treat yourself with saved money";

        try {

            AmazonCloudWatch client = new AmazonCloudWatchClient();
            client.setRegion(Region.getRegion(Regions.US_WEST_2));

            List<Dimension> dimensionList = new ArrayList();
            Dimension totalMemoryDimension = new Dimension().withName(name).withValue(value);
            dimensionList.add(totalMemoryDimension);
            GetMetricStatisticsRequest gmsRequest = new GetMetricStatisticsRequest().withNamespace(namespace).
                    withMetricName(metricName).withEndTime(new Date()).
                    withStartTime(new Date(System.currentTimeMillis() - (360 * 60 * 1000))).withPeriod(1800).
                    withStatistics(Statistic.Maximum, Statistic.Minimum, Statistic.Average).withDimensions(
                    dimensionList);


            GetMetricStatisticsResult result = client.getMetricStatistics(gmsRequest);


            //"Unable to connect to backend cloudWatch service.. Please try again";
            if (result != null) {
                List<Datapoint> datapoints = result.getDatapoints();
                if (datapoints.size() > 0) {
                    message = "The stats at " + datapoints.get(0).getTimestamp() +
                            " . Minimum utilization " + datapoints.get(0).getMinimum() +
                            " . Maximum utilization " + datapoints.get(0).getMaximum() +
                            " . Average utilization " + datapoints.get(0).getAverage().floatValue();
                    if (datapoints.get(0).getAverage() < 6) {
                        message = message + ". Current server is mostly running less than 70 % utilization. You may want to consider downgrade " +

                                "instance type and treat yourself with saved money";
                    }
                }
            }

        }catch (Exception e) {

        }
        return message;
    }
}
