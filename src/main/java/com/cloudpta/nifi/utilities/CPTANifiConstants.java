/*

Copyright 2017-2019 Advanced Products Limited, 
Copyright 2021-2022 Liquid Markets Limited, 
github.com/dannyb2018

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

*/
package com.cloudpta.nifi.utilities;

/**
 *
 * @author Danny
 */
public interface CPTANifiConstants
{
    public static String NIFI_API_BASE_URL = "/nifi-api";
    public static String NIFI_API_PROCESS_GROUPS_SUBURL = "/process-groups";
    public static String NIFI_API_PROCESSORS_SUBURL = "/processors";
    public static String NIFI_API_OUTPUT_PORTS_SUBURL = "/output-ports";
    public static String NIFI_API_INPUT_PORTS_SUBURL = "/input-ports";
    public static String NIFI_API_RUN_STATUS_SUBURL = "/run-status";
    public static String NIFI_API_GET_CONTROLLER_SERVICES_LIST_SUBURL = "/flow/controller/controller-services";
    public static String NIFI_API_PROCESS_GROUP_CHANGE_STATE_SUBURL = "/flow/process-groups/";
    public static String NIFI_API_CONTROLLER_SERVICES_SUBURL = "/controller-services";
    public static String NIFI_API_SYSTEM_DIAGNOSTICS_SUBURL = "/system-diagnostics";

    public static String NIFI_API_PARAMETER_CONTEXTS_SUBURL = "/parameter-contexts";
    public static String NIFI_API_GET_ALL_PARAMETER_CONTEXTS_SUBURL = "/flow/parameter-contexts";
    
    public static String STATUS_ENABLED = "ENABLED";
    public static String STATUS_DISABLED = "DISABLED";
    public static String STATUS_RUNNING = "RUNNING";
    public static String STATUS_STOPPED = "STOPPED";
    
    public static String RELATIONSHIP_AUTOTERMINATE = "autoTerminate";
    
    public static String CSV_RECORD_READER_CONTROLLER_SKIP_HEADER_PROPERTY="Skip Header Line";
    public static String CSV_RECORD_READER_CONTROLLER_SKIP_HEADER_PROPERTY_DEFAULT_VALUE = "true";

    //"********"

    public static String JSON_RECORD_WRITER_CONTROLLER_SERVICE_TYPE="org.apache.nifi.json.JsonRecordSetWriter";
    public static String CSV_RECORD_WRITER_CONTROLLER_SERVICE_TYPE ="org.apache.nifi.csv.CSVRecordSetWriter";
    public static String CSV_READER_CONTROLLER_SERVICE_TYPE ="org.apache.nifi.csv.CSVReader";
    
    public static String GET_FILE_PROCESSOR_TYPE="org.apache.nifi.processors.standard.GetFile";
    public static String PUBLISH_KAFKA_PROCESSOR_TYPE="org.apache.nifi.processors.kafka.pubsub.PublishKafkaRecord_2_0";
    public static String GENERATE_FLOW_FILE_TYPE="org.apache.nifi.processors.standard.GenerateFlowFile";
    
    
    public static String GET_FILE_PROCESSOR_INPUT_DIRECTORY="Input Directory";
    
    public static String KAFKA_PROCESSOR_PROPERTY_RECORD_READER = "record-reader";
    public static String KAFKA_PROCESSOR_PROPERTY_RECORD_WRITER = "record-writer";
    public static String KAFKA_PROCESSOR_PROPERTY_BROKERS = "bootstrap.servers";
    public static String KAFKA_PROCESSOR_PROPERTY_TOPIC = "topic";
    public static String KAFKA_PROCESSOR_PROPERTY_USE_TRANSACTIONS = "use-transactions";
    public static String KAFKA_PROCESSOR_PROPERTY_PARTITIONER_CLASS = "partitioner.class";
    public static String KAFKA_PROCESSOR_PROPERTY_DELIVERY_GUARANTY = "acks";
    public static String KAFKA_PROCESSOR_DELIVERY_GUARANTY_SINGLE_NODE_GUARANTEE = "1";
    public static String KAFKA_PROCESSOR_DELIVERY_GUARANTY_BEST_EFFORT = "0";
    public static String KAFKA_PROCESSOR_DELIVERY_GUARANTY_MULTIPLE_NODE_GUARANTEE = "all";
    
    public static String[] SCHEDULER_TYPE_TEXT = {"TIMER_DRIVEN", "CRON_DRIVEN"};
    public static int SCHEDULER_TYPE_TIMER_DRIVEN=0;
    public static int SCHEDULER_TYPE_CRON_DRIVEN=1;
}
