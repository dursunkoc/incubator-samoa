package com.yahoo.labs.samoa.examples;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2013 - 2014 Yahoo! Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javacliparser.Configurable;
import com.github.javacliparser.IntOption;
import com.github.javacliparser.StringOption;
import com.yahoo.labs.samoa.tasks.Task;
import com.yahoo.labs.samoa.topology.ComponentFactory;
import com.yahoo.labs.samoa.topology.Stream;
import com.yahoo.labs.samoa.topology.Topology;
import com.yahoo.labs.samoa.topology.TopologyBuilder;

/**
 * Example {@link Task} in SAMOA. This task simply sends events from a source
 * {@link HelloWorldSourceProcessor} to a destination
 * {@link HelloWorldDestinationProcessor}. The events are random integers
 * generated by the source and encapsulated in a {@link HelloWorldContentEvent}.
 * The destination prints the content of the event to standard output, prepended
 * by the processor id.
 * 
 * The task has 2 main options: the number of events the source will generate
 * (-i) and the parallelism level of the destination (-p).
 */
public class HelloWorldTask implements Task, Configurable {

  private static final long serialVersionUID = -5134935141154021352L;
  private static Logger logger = LoggerFactory.getLogger(HelloWorldTask.class);

  /** The topology builder for the task. */
  private TopologyBuilder builder;
  /** The topology that will be created for the task */
  private Topology helloWorldTopology;

  public IntOption instanceLimitOption = new IntOption("instanceLimit", 'i',
      "Maximum number of instances to generate (-1 = no limit).", 1000000, -1, Integer.MAX_VALUE);

  public IntOption helloWorldParallelismOption = new IntOption("parallelismOption", 'p',
      "Number of destination Processors", 1, 1, Integer.MAX_VALUE);

  public StringOption evaluationNameOption = new StringOption("evaluationName", 'n',
      "Identifier of the evaluation", "HelloWorldTask" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));

  @Override
  public void init() {
    // create source EntranceProcessor
    /* The event source for the topology. Implements EntranceProcessor */
    HelloWorldSourceProcessor sourceProcessor = new HelloWorldSourceProcessor(instanceLimitOption.getValue());
    builder.addEntranceProcessor(sourceProcessor);

    // create Stream
    Stream stream = builder.createStream(sourceProcessor);

    // create destination Processor
    /* The event sink for the topology. Implements Processor */
    HelloWorldDestinationProcessor destProcessor = new HelloWorldDestinationProcessor();
    builder.addProcessor(destProcessor, helloWorldParallelismOption.getValue());
    builder.connectInputShuffleStream(stream, destProcessor);

    // build the topology
    helloWorldTopology = builder.build();
    logger.debug("Successfully built the topology");
  }

  @Override
  public Topology getTopology() {
    return helloWorldTopology;
  }

  @Override
  public void setFactory(ComponentFactory factory) {
    // will be removed when dynamic binding is implemented
    builder = new TopologyBuilder(factory);
    logger.debug("Successfully instantiating TopologyBuilder");
    builder.initTopology(evaluationNameOption.getValue());
    logger.debug("Successfully initializing SAMOA topology with name {}", evaluationNameOption.getValue());
  }
}
